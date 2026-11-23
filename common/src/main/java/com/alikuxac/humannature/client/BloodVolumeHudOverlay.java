package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class BloodVolumeHudOverlay {
    private static final int BAR_WIDTH = 64;
    private static final int BAR_HEIGHT = 3;
    private static final int BAR_COLOR = 0xFFDC143C;

    private BloodVolumeHudOverlay() {}

    public static float ratioForBloodVolume(short bloodVolume) {
        return Math.max(0, Math.min(PlayerPhysiologyData.MAX_BLOOD_VOLUME, bloodVolume))
                / (float) PlayerPhysiologyData.MAX_BLOOD_VOLUME;
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.isDeadOrDying() || minecraft.options.hideGui || minecraft.screen != null) return;
        if (!ClientConfig.ENABLE_HUD_OVERLAY.get() || !PhysiologyModule.isBloodSystemEnabled()) return;

        float ratio = ratioForBloodVolume(PhysiologyModule.getBloodVolume(player.getUUID()));
        int percent = Math.round(ratio * 100.0F);
        int x = 10;
        int labelY = guiGraphics.guiHeight() - 56;
        int barY = labelY + 10;

        guiGraphics.drawString(minecraft.font, Component.translatable("hud.humannature.blood_volume"), x, labelY, 0xFFD0D0D0, false);
        guiGraphics.drawString(minecraft.font, percent + "%", x + BAR_WIDTH + 4, labelY, BAR_COLOR, false);
        guiGraphics.fill(x, barY, x + BAR_WIDTH, barY + BAR_HEIGHT, 0xA0202020);
        int filledWidth = Math.round(BAR_WIDTH * ratio);
        if (filledWidth > 0) {
            guiGraphics.fill(x, barY, x + filledWidth, barY + BAR_HEIGHT, BAR_COLOR);
        }
    }
}
