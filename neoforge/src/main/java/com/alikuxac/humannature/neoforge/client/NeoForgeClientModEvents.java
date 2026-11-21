package com.alikuxac.humannature.neoforge.client;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.client.ModKeyMappings;
import com.alikuxac.humannature.item.BloodBagItem;
import com.alikuxac.humannature.neoforge.init.NeoForgeModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = HumanNatureCommon.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class NeoForgeClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                NeoForgeModItems.BLOOD_BAG.get(),
                ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "blood_ratio"),
                (stack, level, entity, seed) -> BloodBagItem.fillRatioForStored(BloodBagItem.getData(stack).storedMl())
        ));
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.OPEN_DIAGNOSTICS);
        event.register(ModKeyMappings.CYCLE_HUD);
    }
}
