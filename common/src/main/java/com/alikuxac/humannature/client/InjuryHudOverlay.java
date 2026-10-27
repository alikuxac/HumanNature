package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.injury.LimbType;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class InjuryHudOverlay {

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;
        if (!ServerConfig.ENABLE_INJURY.get() || !ClientConfig.ENABLE_HUD_OVERLAY.get()) return;
        if (ClientConfig.ENABLE_SWAPPABLE_HUD.get() && SwappableHudOverlay.getCurrentMode() != SwappableHudOverlay.HudMode.INJURY) return;

        PlayerInjuryData injuryData = InjuryModule.getOrCreateInjuryData(player.getUUID());
        boolean isActive = InjuryEventHandler.isFractureActive(player) || injuryData.hasAnyInjury();
        if (!isActive) return;

        int remainingTicks = InjuryEventHandler.getRemainingFractureTime(player);
        int totalSeconds = remainingTicks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        boolean isMoving = player.walkDist - player.walkDistO > 0.001F;
        boolean isResting = player.isSleeping() || (player.isCrouching() && !isMoving);

        String statusText;
        if (isMoving) {
            statusText = String.format("§cHealing: %02d:%02d (Paused)", minutes, seconds);
        } else if (isResting) {
            statusText = String.format("§eHealing: %02d:%02d (Resting 2x)", minutes, seconds);
        } else {
            statusText = String.format("§bHealing: %02d:%02d (Standing Still)", minutes, seconds);
        }

        StringBuilder injuredLimbs = new StringBuilder();
        for (LimbType limb : LimbType.values()) {
            byte sev = injuryData.getSeverity(limb);
            if (sev > 0) {
                if (!injuredLimbs.isEmpty()) injuredLimbs.append(", ");
                injuredLimbs.append(limb.getDisplayName()).append(" (T").append(sev).append(")");
            }
        }

        String limbsTitle = injuredLimbs.isEmpty() ? "§6Trauma Status" : "§cInjured: §f" + injuredLimbs;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int x = ClientConfig.HUD_X.get();
        int y = ClientConfig.HUD_Y.get();

        if (x < 4) x = 4;
        if (x > screenWidth - 100) x = Math.max(4, screenWidth - 100);

        int maxWrapWidth = Math.max(120, Math.min(240, screenWidth - x - 16));
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        if (mc.font.width(limbsTitle) <= maxWrapWidth) {
            wrappedLines.add(Component.literal(limbsTitle).getVisualOrderText());
        } else {
            wrappedLines.addAll(mc.font.split(Component.literal(limbsTitle), maxWrapWidth));
        }

        if (mc.font.width(statusText) <= maxWrapWidth) {
            wrappedLines.add(Component.literal(statusText).getVisualOrderText());
        } else {
            wrappedLines.addAll(mc.font.split(Component.literal(statusText), maxWrapWidth));
        }

        int maxLineWidth = 0;
        for (FormattedCharSequence fcs : wrappedLines) {
            int w = mc.font.width(fcs);
            if (w > maxLineWidth) maxLineWidth = w;
        }

        int boxWidth = Math.max(120, maxLineWidth + 8);
        int boxHeight = wrappedLines.size() * 11 + 6;

        if (x + boxWidth > screenWidth - 4) {
            x = Math.max(4, screenWidth - boxWidth - 4);
        }
        if (y + boxHeight > screenHeight - 4) {
            y = Math.max(4, screenHeight - boxHeight - 4);
        }

        guiGraphics.fill(x - 4, y - 4, x + boxWidth, y + boxHeight, 0x90000000);
        guiGraphics.renderOutline(x - 4, y - 4, boxWidth + 4, boxHeight + 4, 0xFF444444);

        int curLineY = y;
        for (FormattedCharSequence fcs : wrappedLines) {
            guiGraphics.drawString(mc.font, fcs, x + 2, curLineY, 0xFFFFFF, false);
            curLineY += 11;
        }
    }
}
