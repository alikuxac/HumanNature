package com.alikuxac.humannature.client;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public final class BloodScreenEffectHandler {
    private static final ResourceLocation CHAIN_ID = ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "shaders/post/blood_vision.json");

    private static PostChain chain;
    private static int lastWidth = -1;
    private static int lastHeight = -1;
    private static int failures;
    private static long disabledUntil;

    private BloodScreenEffectHandler() {
    }

    public static float intensityFor(short bloodVolume) {
        if (bloodVolume >= 3000) return 0.0f;
        float ramp = Math.max(0.0f, Math.min(1.0f, (3000 - bloodVolume) / 1000.0f));
        double multiplier = ClientConfig.BLOOD_SCREEN_EFFECT_INTENSITY.get();
        return ramp * (float) Math.max(0.0, Math.min(1.0, multiplier));
    }

    public static void process(float partialTick) {
        if (!ClientConfig.ENABLE_BLOOD_SCREEN_EFFECTS.get()) return;
        if (System.currentTimeMillis() < disabledUntil) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        float intensity = intensityFor(PhysiologyModule.getOrCreatePhysiologyData(mc.player.getUUID()).getBloodVolume());
        if (intensity <= 0.0f) return;

        RenderTarget mainTarget = mc.getMainRenderTarget();
        if (mainTarget == null) return;

        try {
            if (chain == null) {
                chain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mainTarget, CHAIN_ID);
            }
            int width = mc.getWindow().getWidth();
            int height = mc.getWindow().getHeight();
            if (width != lastWidth || height != lastHeight) {
                chain.resize(width, height);
                lastWidth = width;
                lastHeight = height;
            }
            chain.setUniform("Intensity", intensity);
            chain.process(partialTick);
            mainTarget.bindWrite(true);
            failures = 0;
        } catch (Exception ignored) {
            if (chain != null) {
                chain.close();
                chain = null;
            }
            lastWidth = -1;
            lastHeight = -1;
            if (++failures >= 3) {
                disabledUntil = System.currentTimeMillis() + 30_000L;
                failures = 0;
            }
        }
    }
}
