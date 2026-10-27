package com.alikuxac.humannature.init;

import com.alikuxac.humannature.HumanNatureCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HumanNatureCommon.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HUMAN_NATURE_TAB =
            CREATIVE_MODE_TABS.register("human_nature_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.humannature"))
                    .icon(() -> new ItemStack(ModItems.REINFORCED_SPLINT.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.WOODEN_SPLINT.get());
                        output.accept(ModItems.REINFORCED_SPLINT.get());
                        output.accept(ModItems.GOLDEN_SPLINT.get());
                        output.accept(ModItems.DIAMOND_SPLINT.get());
                        output.accept(ModItems.NETHERITE_SPLINT.get());
                        output.accept(ModItems.BONE_BROTH.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.WOODEN_SPLINT.get());
            event.accept(ModItems.REINFORCED_SPLINT.get());
            event.accept(ModItems.GOLDEN_SPLINT.get());
            event.accept(ModItems.DIAMOND_SPLINT.get());
            event.accept(ModItems.NETHERITE_SPLINT.get());
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.BONE_BROTH.get());
        }
    }
}
