package com.alikuxac.humannature.item;

import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.advancement.HumanNatureAdvancements;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class SplintItem extends Item {
    public static final int CHANNEL_DURATION = 40;
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
        if (!ServerConfig.ENABLE_SPLINT.get()) {
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

        if (!level.isClientSide && ServerConfig.ENABLE_INJURY.get() && ServerConfig.ENABLE_SPLINT.get()
                && InjuryEventHandler.isFractureActive(player)) {
            applySplintTreatment(player, stack);
            HumanNatureAdvancements.trigger(player, HumanNatureAdvancements.SPLINT_APPLIED);

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
        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        LimbType targetLimb = null;

        if (data.getSeverity(LimbType.LEFT_LEG) > 0 && !data.isSplinted(LimbType.LEFT_LEG)) {
            targetLimb = LimbType.LEFT_LEG;
        } else if (data.getSeverity(LimbType.RIGHT_LEG) > 0 && !data.isSplinted(LimbType.RIGHT_LEG)) {
            targetLimb = LimbType.RIGHT_LEG;
        } else {
            int maxSev = 0;
            for (LimbType limb : LimbType.values()) {
                if (data.getSeverity(limb) > maxSev && !data.isSplinted(limb)) {
                    maxSev = data.getSeverity(limb);
                    targetLimb = limb;
                }
            }
        }

        InjuryEventHandler.applySplintHealing(player, tier);
        if (targetLimb != null && InjuryEventHandler.isFractureActive(player)) {
            InjuryEventHandler.applySplintToLimb(player, targetLimb, tier);
        }

        if (tier.getPrimaryBuffDuration() > 0) {
            switch (tier) {
                case REINFORCED -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, tier.getPrimaryBuffDuration(), 0));
                case GOLDEN -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + tier.getAbsorptionAmount());
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, tier.getPrimaryBuffDuration(), 0));
                }
                case DIAMOND -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, tier.getPrimaryBuffDuration(), 1));
                    AttributeInstance kbAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                    if (kbAttr != null) {
                        ResourceLocation diamondKbId = ResourceLocation.fromNamespaceAndPath("humannature", "diamond_splint_knockback");
                        kbAttr.removeModifier(diamondKbId);
                        kbAttr.addTransientModifier(new AttributeModifier(diamondKbId, 0.5, AttributeModifier.Operation.ADD_VALUE));
                    }
                }
                case NETHERITE -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, tier.getPrimaryBuffDuration(), 1));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, tier.getPrimaryBuffDuration(), 0));
                }
                default -> {}
            }
        }
    }

    public static boolean isValidSplint(ItemStack stack) {
        return stack.getItem() instanceof SplintItem && stack.getDamageValue() < stack.getMaxDamage();
    }
}
