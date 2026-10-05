package com.alikuxac.humannature.forge.event;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.modules.injury.CameraLimpingHandler;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import com.alikuxac.humannature.client.InjuryHudOverlay;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = HumanNatureCommon.MOD_ID, value = Dist.CLIENT)
public class ForgeClientEvents {

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (event.getCamera().getEntity() instanceof Player player) {
            CameraLimpingHandler.CameraOffset offset = CameraLimpingHandler.calculateCameraOffset(player, (float) event.getPartialTick());
            event.setPitch(event.getPitch() + offset.pitch);
            event.setRoll(event.getRoll() + offset.roll);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        InjuryHudOverlay.render(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(true));
    }
}
