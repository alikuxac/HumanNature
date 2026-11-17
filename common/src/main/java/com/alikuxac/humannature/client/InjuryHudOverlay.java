package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
        if (ClientConfig.AUTO_HIDE_HEALTHY_HUD.get() && SwappableHudOverlay.isPlayerFullyHealthy(player)) return;
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

        String timeStr = String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds);
        Component statusComponent;
        if (isMoving) {
            statusComponent = Component.translatable("hud.humannature.injury.healing_paused", timeStr).withStyle(ChatFormatting.RED);
        } else if (isResting) {
            statusComponent = Component.translatable("hud.humannature.injury.healing_resting", timeStr).withStyle(ChatFormatting.YELLOW);
        } else {
            statusComponent = Component.translatable("hud.humannature.injury.healing_standing", timeStr).withStyle(ChatFormatting.AQUA);
        }

        MutableComponent injuredLimbs = Component.empty();
        boolean hasInjured = false;
        for (LimbType limb : LimbType.values()) {
            byte sev = injuryData.getSeverity(limb);
            if (sev > 0) {
                if (hasInjured) injuredLimbs.append(", ");
                hasInjured = true;
                injuredLimbs.append(Component.translatable(limb.getTranslationKey())).append(" (T" + sev + ")");
            }
        }

        Component limbsTitle = !hasInjured
                ? Component.translatable("hud.humannature.injury.trauma_status").withStyle(ChatFormatting.GOLD)
                : Component.translatable("hud.humannature.injury.injured_prefix", injuredLimbs.withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.RED);

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int x = ClientConfig.HUD_X.get();
        int y = ClientConfig.HUD_Y.get();

        if (x < 4) x = 4;
        if (x > screenWidth - 100) x = Math.max(4, screenWidth - 100);

        int maxWrapWidth = Math.max(120, Math.min(240, screenWidth - x - 16));
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        wrappedLines.addAll(mc.font.split(limbsTitle, maxWrapWidth));
        wrappedLines.addAll(mc.font.split(statusComponent, maxWrapWidth));

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
