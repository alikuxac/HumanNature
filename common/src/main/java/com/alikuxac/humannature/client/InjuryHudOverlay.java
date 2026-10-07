package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class InjuryHudOverlay {

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;
        if (!CommonConfig.ENABLE_INJURY.get()) return;

        boolean isActive = InjuryEventHandler.isFractureActive(player);
        int fractures = InjuryEventHandler.getFractureCount(player);
        if (!isActive && fractures == 0) return;

        int remainingTicks = InjuryEventHandler.getRemainingFractureTime(player);
        int totalSeconds = remainingTicks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        boolean isResting = player.isCrouching() || player.isSleeping();
        boolean isMoving = player.walkDist - player.walkDistO > 0.001F && !isResting;

        String title;
        if (fractures == 2) {
            title = "Fracture: Both Legs";
        } else if (fractures == 1) {
            title = "Fracture: 1 Leg";
        } else {
            title = "Bone Recovery";
        }

        String statusText;
        int statusColor;
        if (isResting) {
            statusText = String.format("Healing: %02d:%02d (Resting 2x)", minutes, seconds);
            statusColor = 0xFFFFFF55; // Yellow
        } else if (isMoving) {
            statusText = String.format("Healing: %02d:%02d (Paused)", minutes, seconds);
            statusColor = 0xFFFF5555; // Light red
        } else {
            statusText = String.format("Healing: %02d:%02d (Standing Still)", minutes, seconds);
            statusColor = 0xFF55FFFF; // Cyan
        }

        int x = 10;
        int y = 10;

        // Draw translucent dark background box
        guiGraphics.fill(x - 4, y - 4, x + 160, y + 26, 0x90000000);
        guiGraphics.renderOutline(x - 4, y - 4, 164, 30, 0xFF444444);

        // Render text
        guiGraphics.drawString(mc.font, title, x, y, 0xFFFFAA00, true);
        guiGraphics.drawString(mc.font, statusText, x, y + 12, statusColor, true);
    }
}
