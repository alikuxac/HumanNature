package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.config.CommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class InjuryEventHandler {
    public static final ResourceLocation FRACTURE_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_speed");
    private static final AttributeModifier FRACTURE_SPEED_MODIFIER = new AttributeModifier(
            FRACTURE_SPEED_MODIFIER_ID,
            -0.45,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    public static void onLivingFall(LivingEntity entity, float fallDistance, float damageMultiplier) {
        if (!CommonConfig.ENABLE_INJURY.get()) return;
        if (!(entity instanceof Player player)) return;

        float calculatedDamage = (fallDistance - 3.0F) * damageMultiplier;

        if (fallDistance >= 5.0F || calculatedDamage > 4.0F) {
            applyBoneFracture(player);
        }
    }

    public static void applyBoneFracture(Player player) {
        // Lock sprinting
        player.setSprinting(false);

        // Apply -45% movement speed penalty
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null && !speedAttr.hasModifier(FRACTURE_SPEED_MODIFIER_ID)) {
            speedAttr.addTransientModifier(FRACTURE_SPEED_MODIFIER);
        }

        // Trigger bone crack sound
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SKELETON_HURT,
                SoundSource.PLAYERS,
                1.0F,
                0.8F
        );
    }
}

