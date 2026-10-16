package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class PhysiologyEventHandler {

    public static void onPlayerTick(Player player, boolean isMoving, boolean isSleeping) {
        if (!PhysiologyModule.isSleepDeprivationEnabled()) return;
        if (player.isCreative() || player.isSpectator()) return;

        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());

        if (isSleeping) {
            return;
        }

        data.incrementAwakeTicks();

        if (data.isBlinkEligible()) {
            if (data.getBlinkCooldown() > 0) {
                data.decrementBlinkCooldown();
            } else if (isMoving && !player.hasEffect(MobEffects.DARKNESS)) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 0, false, false, true));
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.WARDEN_HEARTBEAT, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 0.7F);

                int minInterval = ServerConfig.SLEEP_FATIGUE_BLINK_MIN_INTERVAL != null ?
                        ServerConfig.SLEEP_FATIGUE_BLINK_MIN_INTERVAL.get() : 300;
                int maxInterval = ServerConfig.SLEEP_FATIGUE_BLINK_MAX_INTERVAL != null ?
                        ServerConfig.SLEEP_FATIGUE_BLINK_MAX_INTERVAL.get() : 800;
                if (maxInterval < minInterval) {
                    maxInterval = minInterval;
                }

                int intervalRange = Math.max(1, maxInterval - minInterval + 1);
                int nextCooldown = minInterval + player.getRandom().nextInt(intervalRange);
                data.setBlinkCooldown(nextCooldown);
            }
        }
    }

    public static void onPlayerWakeUp(Player player, int consecutiveSleepTicks) {
        if (!PhysiologyModule.isSleepDeprivationEnabled()) return;

        boolean sleptThroughNight = consecutiveSleepTicks >= 90 || player.level().isDay();
        if (player instanceof ServerPlayer serverPlayer) {
            int timeSinceRest = serverPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            if (timeSinceRest < 100) {
                sleptThroughNight = true;
            }
        }

        if (sleptThroughNight) {
            PhysiologyModule.resetSleepDeprivation(player.getUUID());
        }
    }

    public static void onPlayerDeath(Player player) {
        if (!PhysiologyModule.isSleepDeprivationEnabled()) return;
        PhysiologyModule.resetSleepDeprivation(player.getUUID());
    }

    public static void onPlayerRespawn(Player player) {
        if (!PhysiologyModule.isSleepDeprivationEnabled()) return;
        PhysiologyModule.resetSleepDeprivation(player.getUUID());
    }
}
