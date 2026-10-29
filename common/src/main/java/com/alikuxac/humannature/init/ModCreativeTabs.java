package com.alikuxac.humannature.init;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModCreativeTabs {
    public static Supplier<CreativeModeTab> HUMAN_NATURE_TAB = () -> null;

    public static CreativeModeTab.Builder createTabBuilder() {
        return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.humannature"))
                .icon(() -> new ItemStack(ModItems.REINFORCED_SPLINT.get()))
                .displayItems((parameters, output) -> {
                    output.accept(ModItems.WOODEN_SPLINT.get());
                    output.accept(ModItems.REINFORCED_SPLINT.get());
                    output.accept(ModItems.GOLDEN_SPLINT.get());
                    output.accept(ModItems.DIAMOND_SPLINT.get());
                    output.accept(ModItems.NETHERITE_SPLINT.get());
                    output.accept(ModItems.BONE_BROTH.get());
                });
    }

    public static void acceptToolsAndUtilities(Consumer<Item> consumer) {
        consumer.accept(ModItems.WOODEN_SPLINT.get());
        consumer.accept(ModItems.REINFORCED_SPLINT.get());
        consumer.accept(ModItems.GOLDEN_SPLINT.get());
        consumer.accept(ModItems.DIAMOND_SPLINT.get());
        consumer.accept(ModItems.NETHERITE_SPLINT.get());
    }

    public static void acceptFoodAndDrinks(Consumer<Item> consumer) {
        consumer.accept(ModItems.BONE_BROTH.get());
    }
}

