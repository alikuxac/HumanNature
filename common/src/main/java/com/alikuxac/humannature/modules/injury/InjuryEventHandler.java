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

    public static void onLivingFall(LivingEntity entity, float fallDistance, float damageMultiplier) {
        if (!CommonConfig.ENABLE_INJURY.get()) return;
        if (!(entity instanceof Player player)) return;

        float calculatedDamage = (fallDistance - 3.0F) * damageMultiplier;

        if (fallDistance >= 5.0F || calculatedDamage > 4.0F) {
            applyBoneFracture(player);
        }
    }

    public static void applyBoneFracture(Player player) {
        player.setSprinting(false);

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (speedAttr == null) return;

        boolean hasSplint = CommonConfig.ENABLE_SPLINT.get() && CuriosCompat.hasActiveSplint(player);

        int currentFractures = getFractureCount(player);

        if (hasSplint) {
            // With splint: apply reduced speed penalty only once
            if (!speedAttr.hasModifier(SPLINT_SPEED_MODIFIER_ID)) {
                speedAttr.addTransientModifier(SPLINT_SPEED_MODIFIER);
            }
            // Remove any existing fracture modifiers
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID);
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID);
            if (jumpAttr != null) {
                jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG1_ID);
                jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID);
            }
        } else {
            // Without splint: normal fracture logic
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

        // Lower pitch sound when 2nd leg breaks (only without splint)
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
    }
}
