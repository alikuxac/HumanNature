package com.alikuxac.humannature.forge.client;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.client.InjuryHudOverlay;
import com.alikuxac.humannature.client.BloodVolumeHudOverlay;
import com.alikuxac.humannature.client.ModKeyMappings;
import com.alikuxac.humannature.client.StaminaHudOverlay;
import com.alikuxac.humannature.client.SwappableHudOverlay;
import com.alikuxac.humannature.client.ToxinHudOverlay;
import com.alikuxac.humannature.forge.init.ForgeModItems;
import com.alikuxac.humannature.item.BloodBagItem;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.gui.LayeredDraw.Layer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HumanNatureCommon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ForgeModItems.BLOOD_BAG.get(),
                ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "blood_ratio"),
                (stack, level, entity, seed) -> BloodBagItem.fillRatioForStored(BloodBagItem.getData(stack).storedMl())
        ));
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.OPEN_DIAGNOSTICS);
        event.register(ModKeyMappings.CYCLE_HUD);
    }

    @SubscribeEvent
    public static void onAddGuiOverlayLayers(AddGuiOverlayLayersEvent event) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "overlays");
        Layer layer = (guiGraphics, deltaTracker) -> {
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
            try {
                InjuryHudOverlay.render(guiGraphics, partialTick);
            } catch (Throwable ignored) {}
            try {
                ToxinHudOverlay.render(guiGraphics, partialTick);
            } catch (Throwable ignored) {}
            try {
                StaminaHudOverlay.render(guiGraphics, partialTick);
            } catch (Throwable ignored) {}

            try {
                SwappableHudOverlay.render(guiGraphics, partialTick);
            } catch (Throwable ignored) {}
            BloodVolumeHudOverlay.render(guiGraphics);
        };

        ForgeLayeredDraw layeredDraw = event.getLayeredDraw();
        var preSleepOpt = layeredDraw.locateStack(ForgeLayeredDraw.PRE_SLEEP_STACK);
        if (preSleepOpt.isPresent()) {
            ForgeLayeredDraw preSleep = preSleepOpt.get();
            preSleep.addAbove(id, ForgeLayeredDraw.HOTBAR, layer);
            if (preSleep.getLayer(id) == null) {
                preSleep.add(id, layer);
            }
        } else {
            layeredDraw.add(id, layer);
        }
    }
}

