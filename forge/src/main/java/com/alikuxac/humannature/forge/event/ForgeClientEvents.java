package com.alikuxac.humannature.forge.event;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.client.AimTremorHandler;
import com.alikuxac.humannature.client.ModKeyMappings;
import com.alikuxac.humannature.client.ShiveringCameraHandler;
import com.alikuxac.humannature.client.SwappableHudOverlay;
import com.alikuxac.humannature.client.gui.DiagnosticScreen;
import com.alikuxac.humannature.client.gui.DiagnosticTabButton;
import com.alikuxac.humannature.modules.injury.CameraLimpingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HumanNatureCommon.MOD_ID, value = Dist.CLIENT)
public class ForgeClientEvents {

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (event.getCamera().getEntity() instanceof Player player) {
            float partialTick = (float) event.getPartialTick();
            CameraLimpingHandler.CameraOffset offset = CameraLimpingHandler.calculateCameraOffset(player, partialTick);
            AimTremorHandler.AimOffset aimOffset = AimTremorHandler.calculateAimOffset(player, partialTick);
            ShiveringCameraHandler.ShiveringOffset shiver = ShiveringCameraHandler.calculateShiveringOffset(player, partialTick);
            event.setPitch(event.getPitch() + offset.pitch + aimOffset.pitch() + shiver.pitch());
            event.setRoll(event.getRoll() + offset.roll + shiver.roll());
            event.setYaw(event.getYaw() + aimOffset.yaw() + shiver.yaw());
        }
    }

    private static DiagnosticTabButton tabButton = null;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen invScreen) {
            int leftPos = (invScreen.width - 176) / 2;
            int topPos = (invScreen.height - 166) / 2;

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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (ModKeyMappings.OPEN_DIAGNOSTICS.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new DiagnosticScreen());
            } else if (mc.screen instanceof DiagnosticScreen) {
                mc.screen.onClose();
            }
        }
        while (ModKeyMappings.CYCLE_HUD.consumeClick()) {
            SwappableHudOverlay.cycleHudMode(mc.player);
        }
        SwappableHudOverlay.onClientTick(mc.player);
    }
}

