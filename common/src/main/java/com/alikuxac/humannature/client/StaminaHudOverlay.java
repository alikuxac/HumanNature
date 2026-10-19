package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class StaminaHudOverlay {
    private static long lastFullTimestamp = 0L;
    private static boolean wasDepleted = false;

    private static final float ARC_START_DEG = -55.0f;
    private static final float ARC_END_DEG = 55.0f;
    private static final float ARC_TOTAL_DEG = ARC_END_DEG - ARC_START_DEG;
    private static final float DEG_STEP = 2.0f;

    private static final int R_BORDER_IN = 18;
    private static final int R_FILL_IN = 19;
    private static final int R_FILL_OUT = 23;
    private static final int R_BORDER_OUT = 24;

    public static void reset() {
        lastFullTimestamp = 0L;
        wasDepleted = false;
    }

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || player.isDeadOrDying() || player.isCreative() || player.isSpectator()) {
            reset();
            return;
        }
        if (!ServerConfig.ENABLE_PHYSIOLOGY.get() || !ServerConfig.ENABLE_PHYSICAL_EXERTION.get()) return;
        if (!ClientConfig.ENABLE_STAMINA_HUD_OVERLAY.get()) return;

        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        float exertion = data.getExertionScore();
        boolean isFailure = data.isMuscleFailure();
        boolean isCramped = data.isSwimmingCramped();

        float stamina = Math.max(0.0f, Math.min(100.0f, 100.0f - exertion));
        long now = System.currentTimeMillis();

        float alpha = 0.0f;
        if (stamina < 99.5f || isFailure || isCramped) {
            wasDepleted = true;
            lastFullTimestamp = 0L;
            alpha = 0.95f;
        } else if (wasDepleted) {
            if (lastFullTimestamp == 0L) {
                lastFullTimestamp = now;
            }
            long elapsed = now - lastFullTimestamp;
            if (elapsed < 1500L) {
                alpha = 0.95f * (1.0f - ((float) elapsed / 1500.0f));
            } else {
                wasDepleted = false;
                lastFullTimestamp = 0L;
                return;
            }
        } else {
            return;
        }

        if (alpha <= 0.01f) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int cx = screenWidth / 2;
        int cy = screenHeight / 2;

        int fillColor;
        if (isFailure) {
            float pulse = 0.7f + 0.3f * (float) Math.sin((now % 1000) * (Math.PI * 2.0 / 400.0));
            int red = (int) (255 * pulse);
            fillColor = 0xFF000000 | (red << 16) | (0x20 << 8) | 0x20;
        } else if (isCramped) {
            fillColor = 0xFF00E5FF;
        } else if (stamina < 25.0f) {
            fillColor = 0xFFFF7043;
        } else {
            fillColor = 0xFFFFD23F;
        }

        int trackColor = isFailure ? 0x60800000 : 0x80101010;
        int borderColor = 0xB0000000;

        int alphaTrack = applyAlpha(trackColor, alpha);
        int alphaBorder = applyAlpha(borderColor, alpha);
        int alphaFill = applyAlpha(fillColor, alpha);

        float fillFraction = stamina / 100.0f;
        float filledStartDeg = ARC_END_DEG - (ARC_TOTAL_DEG * fillFraction);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) cx, (float) cy, 0.0f);

        for (float deg = ARC_START_DEG; deg <= ARC_END_DEG; deg += DEG_STEP) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(deg));

            guiGraphics.fill(R_BORDER_IN, -1, R_BORDER_OUT, 1, alphaBorder);
            guiGraphics.fill(R_FILL_IN, -1, R_FILL_OUT, 1, alphaTrack);

            if (deg >= filledStartDeg && fillFraction > 0.001f) {
                guiGraphics.fill(R_FILL_IN, -1, R_FILL_OUT, 1, alphaFill);
            }

            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();
    }

    private static int applyAlpha(int argb, float alpha) {
        int a = (int) (((argb >>> 24) & 0xFF) * alpha);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
