package com.alikuxac.humannature.item;

import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.physiology.PhysiologyEventHandler;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ClothBandageItem extends Item {
    public static final int CHANNEL_DURATION = 30;

    public ClothBandageItem(Properties properties) {
        super(properties.stacksTo(16));
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
            if (count % 6 == 0) {
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        SoundSource.PLAYERS,
                        0.5F,
                        1.2F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F
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
        if (!ServerConfig.ENABLE_CLOTH_BANDAGE.get()) {
            return InteractionResultHolder.pass(stack);
        }

        if (PhysiologyModule.getBleedingTier(player.getUUID()) > 0) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide()) {
                PhysiologyEventHandler.applyClothBandageTreatment(player);
                stack.consume(1, player);
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
        }
        return stack;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!ServerConfig.ENABLE_CLOTH_BANDAGE.get()) {
            return InteractionResult.PASS;
        }

        if (interactionTarget instanceof Player targetPlayer) {
            if (PhysiologyModule.getBleedingTier(targetPlayer.getUUID()) > 0) {
                if (!player.level().isClientSide()) {
                    PhysiologyEventHandler.applyClothBandageTreatment(targetPlayer);
                    stack.consume(1, player);
                    player.level().playSound(
                            null,
                            targetPlayer.getX(),
                            targetPlayer.getY(),
                            targetPlayer.getZ(),
                            SoundEvents.ARMOR_EQUIP_LEATHER,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide());
            }
        }
        return InteractionResult.PASS;
    }
}

