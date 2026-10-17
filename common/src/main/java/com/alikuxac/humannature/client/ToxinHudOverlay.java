package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.modules.physiology.ToxinType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class ToxinHudOverlay {
    private static long lastZeroTimestamp = 0L;
    private static boolean wasNonZero = false;

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;
        if (!ServerConfig.ENABLE_PHYSIOLOGY.get() || !ServerConfig.ENABLE_TOXIN_BUILDUP.get()) return;
        if (!ClientConfig.ENABLE_TOXIN_HUD_OVERLAY.get()) return;

        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        float buildup = data.getToxinBuildup();
        long now = System.currentTimeMillis();

        float alpha = 0.0f;
        if (buildup > 0.0f) {
            wasNonZero = true;
            lastZeroTimestamp = 0L;
            alpha = 0.95f;
        } else if (wasNonZero) {
            if (lastZeroTimestamp == 0L) {
                lastZeroTimestamp = now;
            }
            long elapsed = now - lastZeroTimestamp;
            if (elapsed < 1000L) {
                alpha = 0.95f * (1.0f - ((float) elapsed / 1000.0f));
            } else {
                wasNonZero = false;
                lastZeroTimestamp = 0L;
                return;
            }
        } else {
            return;
        }

        if (alpha <= 0.01f) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barWidth = 140;
        int barHeight = 3;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight - 31;

        ToxinType type = data.getLastToxinType();
        int fillColor = type == ToxinType.WITHER ? 0xFFBA68C8 : 0xFF00E676;
        int bgColor = 0x90101010;
        int outlineColor = 0xC0000000;

        int alphaBg = applyAlpha(bgColor, alpha);
        int alphaOutline = applyAlpha(outlineColor, alpha);
        int alphaFill = applyAlpha(fillColor, alpha);

        // Render sleek background & 1px dark border
        guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, alphaOutline);
        guiGraphics.fill(x, y, x + barWidth, y + barHeight, alphaBg);

        // Render filled progress
        int filledWidth = (int) (barWidth * (Math.min(100.0f, Math.max(0.0f, buildup)) / 100.0f));
        if (filledWidth > 0) {
            guiGraphics.fill(x, y, x + filledWidth, y + barHeight, alphaFill);
        }
    }

    private static int applyAlpha(int argb, float alpha) {
        int a = (int) (((argb >>> 24) & 0xFF) * alpha);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
