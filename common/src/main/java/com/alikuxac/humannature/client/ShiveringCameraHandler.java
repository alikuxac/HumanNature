package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ShiveringCameraHandler {

    public record ShiveringOffset(float pitch, float yaw, float roll) {}

    public static ShiveringOffset calculateShiveringOffset(Player player, float partialTick) {
        if (!ServerConfig.ENABLE_TEMPERATURE.get() || !ClientConfig.ENABLE_SHIVERING_CAMERA.get()) {
            return new ShiveringOffset(0.0f, 0.0f, 0.0f);
        }
        if (player == null || player.isCreative() || player.isSpectator()) {
            return new ShiveringOffset(0.0f, 0.0f, 0.0f);
        }

        int ticksFrozen = player.getTicksFrozen();
        int requiredTicks = player.getTicksRequiredToFreeze();
        int freezeThresh = (int) Math.round(requiredTicks * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get());
        if (ticksFrozen < freezeThresh) {
            return new ShiveringOffset(0.0f, 0.0f, 0.0f);
        }

        float severity = Math.min(1.0f, (ticksFrozen - freezeThresh) / (float) Math.max(1, requiredTicks - freezeThresh + 40));
        float time = (player.tickCount + partialTick) * 1.8f;

        float jitterPitch = (Mth.sin(time * 3.1f) + Mth.cos(time * 5.7f)) * 0.18f * severity;
        float jitterYaw = (Mth.cos(time * 2.9f) + Mth.sin(time * 4.3f)) * 0.22f * severity;
        float jitterRoll = Mth.sin(time * 3.7f) * 0.15f * severity;

        return new ShiveringOffset(jitterPitch, jitterYaw, jitterRoll);
    }
}
