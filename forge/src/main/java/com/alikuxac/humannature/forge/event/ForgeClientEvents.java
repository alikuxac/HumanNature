package com.alikuxac.humannature.forge.event;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.client.InjuryHudOverlay;
import com.alikuxac.humannature.client.ModKeyMappings;
import com.alikuxac.humannature.client.gui.DiagnosticScreen;
import com.alikuxac.humannature.client.gui.DiagnosticTabButton;
import com.alikuxac.humannature.modules.injury.CameraLimpingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

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

    private static DiagnosticTabButton tabButton = null;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen invScreen) {
            int leftPos = (invScreen.width - 176) / 2;
            int topPos = (invScreen.height - 166) / 2;

            // Tab attached flush to left border of inventory GUI (x = leftPos - 27, y = topPos + 8)
            tabButton = new DiagnosticTabButton(
                    leftPos - 27,
                    topPos + 8,
                    btn -> {
                        Minecraft.getInstance().setScreen(new DiagnosticScreen());
                    }
            );
            event.addListener(tabButton);
        } else {
            tabButton = null;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (ModKeyMappings.OPEN_DIAGNOSTICS.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new DiagnosticScreen());
            } else if (mc.screen instanceof DiagnosticScreen) {
                mc.screen.onClose();
            }
        }
    }
}
