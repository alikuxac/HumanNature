package com.alikuxac.humannature.client;

import com.alikuxac.humannature.client.gui.DiagnosticAssets;
import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.temperature.PlayerTemperatureData;
import com.alikuxac.humannature.modules.temperature.TemperatureModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class PhysiologyVignetteOverlay {
    private PhysiologyVignetteOverlay() {}

    public static void render(GuiGraphics gui) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || player.isDeadOrDying()) return;
        if (!ClientConfig.ENABLE_HUD_OVERLAY.get() || !ServerConfig.ENABLE_TEMPERATURE.get()) return;

        PlayerTemperatureData data = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
        int frozen = player.getTicksFrozen();
        int threshold = Math.max(1, (int) Math.round(player.getTicksRequiredToFreeze()
                * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get()));

        float frost = 0.0f;
        if (frozen > 0 || data.getWetnessBuffer() >= 0.5f) {
            frost = Math.max(0.06f, Math.min(0.18f,
                    (frozen / (float) threshold) * 0.14f + data.getWetnessBuffer() * 0.07f));
        }
        float heat = data.isNearHeatSource() ? 0.12f : 0.0f;

        if (frost <= 0.0f && heat <= 0.0f) return;

        int width = gui.guiWidth();
        int height = gui.guiHeight();
        if (frost > 0.0f) {
            draw(gui, DiagnosticAssets.FROST_VIGNETTE, width, height, frost);
        }
        if (heat > 0.0f) {
            draw(gui, DiagnosticAssets.HEAT_VIGNETTE, width, height, heat);
        }
    }

    private static void draw(GuiGraphics gui, net.minecraft.resources.ResourceLocation texture,
                             int width, int height, float alpha) {
        int corner = Math.max(48, Math.min(96, Math.min(width, height) / 3));
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        gui.blit(texture, 0, 0, corner, corner, 0.0f, 0.0f, 32, 32, 64, 64);
        gui.blit(texture, width - corner, 0, corner, corner, 32.0f, 0.0f, 32, 32, 64, 64);
        gui.blit(texture, 0, height - corner, corner, corner, 0.0f, 32.0f, 32, 32, 64, 64);
        gui.blit(texture, width - corner, height - corner, corner, corner, 32.0f, 32.0f, 32, 32, 64, 64);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
