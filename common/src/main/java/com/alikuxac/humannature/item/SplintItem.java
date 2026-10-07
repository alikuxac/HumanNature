package com.alikuxac.humannature.item;

import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class SplintItem extends Item {
    public static final int CHANNEL_DURATION = 40; // 2 seconds (40 ticks)
    private final SplintTier tier;

    public SplintItem(SplintTier tier) {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(tier.getMaxDurability())
                .fireResistant());
        this.tier = tier;
    }

    public SplintTier getTier() {
        return tier;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CHANNEL_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (entity instanceof Player player) {
            // Play leather rustle sound every 5 ticks
            if (count % 5 == 0) {
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        SoundSource.PLAYERS,
                        0.6F,
                        1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F
                );
            }

            // Spawn white wool item particles on the player positioning
            if (level.isClientSide()) {
                double px = player.getX() + (level.random.nextDouble() - 0.5D) * 0.4D;
                double py = player.getY() + 0.8D + (level.random.nextDouble() - 0.5D) * 0.3D;
                double pz = player.getZ() + (level.random.nextDouble() - 0.5D) * 0.4D;
                double vx = (level.random.nextDouble() - 0.5D) * 0.05D;
                double vy = level.random.nextDouble() * 0.05D;
                double vz = (level.random.nextDouble() - 0.5D) * 0.05D;

                level.addParticle(
                        new ItemParticleOption(
                                ParticleTypes.ITEM,
                                new ItemStack(Items.WHITE_WOOL)
                        ),
                        px, py, pz, vx, vy, vz
                );
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!CommonConfig.ENABLE_SPLINT.get()) {
            return InteractionResultHolder.pass(stack);
        }

        if (InjuryEventHandler.isFractureActive(player)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return stack;
        }

        if (!level.isClientSide && InjuryEventHandler.isFractureActive(player)) {
            applySplintTreatment(player, stack);

            stack.hurtAndBreak(1, player, player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        return stack;
    }

    private void applySplintTreatment(Player player, ItemStack stack) {
        switch (tier) {
            case WOODEN:
                // Instantly relieves fracture state to Splinted status
                InjuryEventHandler.clearFractureTimer(player);
                break;
            case REINFORCED:
                // Cuts remaining fracture timer by 50% & gives Resistance I for 30s
                int time = InjuryModule.getRemainingFractureTime(player.getUUID());
                InjuryModule.reduceFractureTimer(player.getUUID(), (int) (time * 0.5));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, tier.getPrimaryBuffDuration(), 0));
                InjuryEventHandler.updatePlayerAttributesAndPose(player);
                break;
            case GOLDEN:
                // Reduces remaining fracture timer by 67% (3x faster heal), gives Absorption & Regen
                int goldenTime = InjuryModule.getRemainingFractureTime(player.getUUID());
                InjuryModule.reduceFractureTimer(player.getUUID(), (int) (goldenTime * 0.67));
                player.setAbsorptionAmount(player.getAbsorptionAmount() + tier.getAbsorptionAmount());
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, tier.getPrimaryBuffDuration(), 0));
                InjuryEventHandler.updatePlayerAttributesAndPose(player);
                break;
            case DIAMOND:
                // Instantly heals fractures, grants Resistance II (amplifier 1) for 20s & Absorption
                InjuryEventHandler.clearFractureTimer(player);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, tier.getPrimaryBuffDuration(), 1));
                player.setAbsorptionAmount(player.getAbsorptionAmount() + 4.0F);
                break;
            case NETHERITE:
                // Instantly heals all fractures & grants Adrenaline (Speed II + Resistance I for 45s)
                InjuryEventHandler.clearFractureTimer(player);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, tier.getPrimaryBuffDuration(), 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, tier.getPrimaryBuffDuration(), 0));
                break;
        }
    }

    public static boolean isValidSplint(ItemStack stack) {
        return stack.getItem() instanceof SplintItem && stack.getDamageValue() < stack.getMaxDamage();
    }
}