package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.config.CommonConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class CameraLimpingHandler {

    public static class CameraOffset {
        public final float pitch;
        public final float roll;

        public CameraOffset(float pitch, float roll) {
            this.pitch = pitch;
            this.roll = roll;
        }
    }

    public static CameraOffset calculateCameraOffset(Player player, float partialTick) {
        if (!CommonConfig.ENABLE_INJURY.get()) {
            return new CameraOffset(0.0F, 0.0F);
        }

        if (player == null || !player.onGround()) {
            return new CameraOffset(0.0F, 0.0F);
        }

        InjuryTier tier = InjuryEventHandler.getFractureTier(player);
        if (tier == InjuryTier.NONE) {
            return new CameraOffset(0.0F, 0.0F);
        }

        float walkDist = Mth.lerp(partialTick, player.walkDistO, player.walkDist);
        float stepDelta = player.walkDist - player.walkDistO;

        if (stepDelta <= 0.001F) {
            return new CameraOffset(0.0F, 0.0F);
        }

        float cycle = walkDist * (float) Math.PI * 2.0F;

        if (tier == InjuryTier.TIER_1_SINGLE_LEG) {
            // Single leg broken: asymmetrical limp
            float rollOffset = Mth.sin(cycle) * 0.6F;
            float pitchOffset = Math.max(0.0F, Mth.sin(cycle)) * 0.3F;
            return new CameraOffset(pitchOffset, rollOffset);
        } else {
            // Both legs broken: severe double-dip & heavier wobble
            float rollOffset = Mth.sin(cycle * 2.0F) * 1.0F;
            float pitchOffset = Math.abs(Mth.sin(cycle)) * 0.6F;
            return new CameraOffset(pitchOffset, rollOffset);
        }
    }
}
