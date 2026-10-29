package com.alikuxac.humannature.forge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.init.ModCreativeTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ForgeCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HumanNatureCommon.MOD_ID);

    public static final RegistryObject<CreativeModeTab> HUMAN_NATURE_TAB =
            CREATIVE_MODE_TABS.register("human_nature_tab", () -> ModCreativeTabs.createTabBuilder().build());

    public static void register(IEventBus eventBus) {
        ModCreativeTabs.HUMAN_NATURE_TAB = HUMAN_NATURE_TAB;
        CREATIVE_MODE_TABS.register(eventBus);
    }

    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            ModCreativeTabs.acceptToolsAndUtilities(event::accept);
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            ModCreativeTabs.acceptFoodAndDrinks(event::accept);
        }
    }
}
