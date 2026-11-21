package com.alikuxac.humannature.client;

import com.alikuxac.humannature.client.gui.DiagnosticScreen;
import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.init.ModSounds;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

public final class HeartbeatAudioHandler {
    private static int cooldown = 0;

    private HeartbeatAudioHandler() {
    }

    public static int intervalTicks(short bloodVolume, int bpm) {
        float severity = Math.max(0.0f, Math.min(1.0f, (4000 - bloodVolume) / 2000.0f));
        float ticks = 20.0f - 12.0f * severity + (bpm - 68) * 0.03f;
        return Math.max(6, Math.min(24, Math.round(ticks)));
    }

    public static void onClientTick(Player player) {
        if (player == null || !ClientConfig.ENABLE_HEARTBEAT_AUDIO.get()) return;
        if (player.isDeadOrDying() || player.isCreative() || player.isSpectator()) return;
        if (!PhysiologyModule.isBloodSystemEnabled()) return;

        SoundEvent sound = ModSounds.HEARTBEAT.get();
        if (sound == null) return;

        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        short volume = data.getBloodVolume();
        if (volume >= 4000) {
            cooldown = 0;
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        int bpm = DiagnosticScreen.calculateHeartRateBpm(player, data);
        cooldown = intervalTicks(volume, bpm);
        float severity = Math.max(0.0f, Math.min(1.0f, (4000 - volume) / 2000.0f));
        player.playSound(sound, 0.35f + 0.45f * severity, 0.9f + 0.35f * severity);
    }
}
