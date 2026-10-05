package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.compat.CuriosCompat;
import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.item.SplintItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;

public class InjuryEventHandler {
    public static final ResourceLocation FRACTURE_SPEED_MODIFIER_LEG1_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_speed_leg1");
    public static final ResourceLocation FRACTURE_SPEED_MODIFIER_LEG2_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_speed_leg2");

    public static final ResourceLocation FRACTURE_JUMP_MODIFIER_LEG1_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_jump_leg1");
    public static final ResourceLocation FRACTURE_JUMP_MODIFIER_LEG2_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_jump_leg2");

    public static final ResourceLocation SPLINT_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "splint_speed_reduction");

    private static final AttributeModifier FRACTURE_SPEED_MODIFIER_LEG1 = new AttributeModifier(
            FRACTURE_SPEED_MODIFIER_LEG1_ID,
            -0.45,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static final AttributeModifier FRACTURE_SPEED_MODIFIER_LEG2 = new AttributeModifier(
            FRACTURE_SPEED_MODIFIER_LEG2_ID,
            -0.45,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static final AttributeModifier FRACTURE_JUMP_MODIFIER_LEG1 = new AttributeModifier(
            FRACTURE_JUMP_MODIFIER_LEG1_ID,
            0.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static final AttributeModifier FRACTURE_JUMP_MODIFIER_LEG2 = new AttributeModifier(
            FRACTURE_JUMP_MODIFIER_LEG2_ID,
            -1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static AttributeModifier SPLINT_SPEED_MODIFIER = new AttributeModifier(
            SPLINT_SPEED_MODIFIER_ID,
            -0.10,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    public static void updateSplintModifier() {
        double reduction = CommonConfig.SPLINT_SPEED_REDUCTION.get();
        SPLINT_SPEED_MODIFIER = new AttributeModifier(
                SPLINT_SPEED_MODIFIER_ID,
                -reduction,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    public static int getFractureCount(Player player) {
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return 0;
        int count = 0;
        if (speedAttr.hasModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID)) count++;
        if (speedAttr.hasModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID)) count++;
        return count;
    }

    public static void applyBoneFracture(Player player) {
        player.setSprinting(false);

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (speedAttr == null) return;

        boolean hasSplint = CommonConfig.ENABLE_SPLINT.get() && CuriosCompat.hasActiveSplint(player);

        int currentFractures = getFractureCount(player);

        if (hasSplint) {
            if (!speedAttr.hasModifier(SPLINT_SPEED_MODIFIER_ID)) {
                speedAttr.addTransientModifier(SPLINT_SPEED_MODIFIER);
            }
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID);
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID);
            if (jumpAttr != null) {
                jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG1_ID);
                jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID);
            }
        } else {
            if (currentFractures == 0) {
                speedAttr.addTransientModifier(FRACTURE_SPEED_MODIFIER_LEG1);
                if (jumpAttr != null && !jumpAttr.hasModifier(FRACTURE_JUMP_MODIFIER_LEG1_ID)) {
                    jumpAttr.addTransientModifier(FRACTURE_JUMP_MODIFIER_LEG1);
                }
            } else if (currentFractures == 1) {
                speedAttr.addTransientModifier(FRACTURE_SPEED_MODIFIER_LEG2);
                if (jumpAttr != null && !jumpAttr.hasModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID)) {
                    jumpAttr.addTransientModifier(FRACTURE_JUMP_MODIFIER_LEG2);
                }
            }
        }

        float pitch = hasSplint ? 0.9F : (currentFractures == 0 ? 0.8F : 0.6F);

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SKELETON_HURT,
                SoundSource.PLAYERS,
                1.0F,
                pitch
        );
        
        InjuryModule.startFractureTimer(player.getUUID());
    }

    public static void reduceFractureTimer(Player player, int amount) {
        InjuryModule.reduceFractureTimer(player.getUUID(), amount);
    }

    public static int getRemainingFractureTime(Player player) {
        return InjuryModule.getRemainingFractureTime(player.getUUID());
    }

    public static boolean isFractureActive(Player player) {
        return InjuryModule.isFractureActive(player.getUUID());
    }

    public static void clearFractureTimer(Player player) {
        InjuryModule.clearFractureTimer(player.getUUID());

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);

        if (speedAttr != null) {
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID);
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID);
            speedAttr.removeModifier(SPLINT_SPEED_MODIFIER_ID);
        }

        if (jumpAttr != null) {
            jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG1_ID);
            jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID);
        }
    }

    public static void handleItemConsumption(Player player, ItemStack stack) {
        if (!isFractureActive(player)) return;

        double reductionPercent = 0.0;

        if (stack.is(Items.MILK_BUCKET)) {
            reductionPercent = 0.50;
        } else if (stack.is(Items.RABBIT_STEW) ||
                 stack.is(Items.MUSHROOM_STEW) ||
                 stack.is(Items.BEETROOT_SOUP) ||
                 stack.is(Items.SUSPICIOUS_STEW)) {
            reductionPercent = 0.30;
        } else if (stack.is(Items.GOLDEN_APPLE) ||
                 stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            reductionPercent = 0.75;
        } else if (stack.getItem() instanceof PotionItem) {
            reductionPercent = 0.40;
        }

        if (reductionPercent > 0.0) {
            int currentTime = getRemainingFractureTime(player);
            int reducedTime = (int) (currentTime * reductionPercent);
            reduceFractureTimer(player, reducedTime);

            if (!isFractureActive(player)) {
                clearFractureTimer(player);
            }

            player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BONE_MEAL_USE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            );
        }
    }

    public static void onLivingFall(LivingEntity entity, float distance, float damageMultiplier) {
        if (!(entity instanceof Player player)) return;
        if (!CommonConfig.ENABLE_INJURY.get()) return;

        if (distance > 3.0F && !player.isCreative() && !player.isSpectator()) {
            applyBoneFracture(player);
        }
    }
}
