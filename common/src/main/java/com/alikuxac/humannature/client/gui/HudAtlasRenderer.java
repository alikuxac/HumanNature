package com.alikuxac.humannature.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class HudAtlasRenderer {
    private HudAtlasRenderer() {}

    public static void icon(GuiGraphics gui, DiagnosticAssets.Uv uv, int x, int y) {
        if (uv == null) return;
        gui.blit(DiagnosticAssets.HUD_ATLAS, x, y, uv.width(), uv.height(),
                uv.u(), uv.v(), uv.width(), uv.height(), 64, 64);
    }

    public static void bar(GuiGraphics gui, int x, int y, int width, float fraction, int fillColor) {
        DiagnosticAssets.Uv frame = DiagnosticAssets.BAR_FRAME;
        int midWidth = Math.max(0, width - 2);

        gui.blit(DiagnosticAssets.HUD_ATLAS, x, y, 1, frame.height(),
                frame.u(), frame.v(), 1, frame.height(), 64, 64);
        if (midWidth > 0) {
            gui.blit(DiagnosticAssets.HUD_ATLAS, x + 1, y, midWidth, frame.height(),
                    frame.u() + 1, frame.v(), frame.width() - 2, frame.height(), 64, 64);
        }
        gui.blit(DiagnosticAssets.HUD_ATLAS, x + width - 1, y, 1, frame.height(),
                frame.u() + frame.width() - 1, frame.v(), 1, frame.height(), 64, 64);

        float clamped = Math.max(0.0f, Math.min(1.0f, fraction));
        int fillWidth = (int) (midWidth * clamped);
        if (fillWidth > 0) {
            gui.fill(x + 1, y + 1, x + 1 + fillWidth, y + frame.height() - 1, fillColor);
        }
    }

    public static void tintedBlit(GuiGraphics gui, ResourceLocation texture, int x, int y,
                                  int width, int height, int textureWidth, int textureHeight,
                                  float r, float g, float b) {
        RenderSystem.setShaderColor(r, g, b, 1.0f);
        gui.blit(texture, x, y, width, height, 0.0f, 0.0f, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
