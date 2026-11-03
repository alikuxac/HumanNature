package com.alikuxac.humannature.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModCreativeTabs {
    public static Supplier<CreativeModeTab> HUMAN_NATURE_TAB = () -> null;

    public static CreativeModeTab.Builder createTabBuilder() {
        return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.humannature"))
                .icon(() -> new ItemStack(ModItems.REINFORCED_SPLINT.get()))
                .displayItems((parameters, output) -> {
                    // Splints
                    output.accept(ModItems.WOODEN_SPLINT.get());
                    output.accept(ModItems.REINFORCED_SPLINT.get());
                    output.accept(ModItems.GOLDEN_SPLINT.get());
                    output.accept(ModItems.DIAMOND_SPLINT.get());
                    output.accept(ModItems.NETHERITE_SPLINT.get());

                    output.accept(ModItems.BONE_BROTH.get());

                    if (ModPotions.BURDEN_BEARER != null && ModPotions.BURDEN_BEARER.get() != null) {
                        output.accept(PotionContents.createItemStack(Items.POTION, ModPotions.BURDEN_BEARER.get()));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.BURDEN_BEARER.get()));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.BURDEN_BEARER.get()));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, ModPotions.BURDEN_BEARER.get()));
                    }
                    if (ModPotions.LONG_BURDEN_BEARER != null && ModPotions.LONG_BURDEN_BEARER.get() != null) {
                        output.accept(PotionContents.createItemStack(Items.POTION, ModPotions.LONG_BURDEN_BEARER.get()));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.LONG_BURDEN_BEARER.get()));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.LONG_BURDEN_BEARER.get()));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, ModPotions.LONG_BURDEN_BEARER.get()));
                    }
                    if (ModPotions.STRONG_BURDEN_BEARER != null && ModPotions.STRONG_BURDEN_BEARER.get() != null) {
                        output.accept(PotionContents.createItemStack(Items.POTION, ModPotions.STRONG_BURDEN_BEARER.get()));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.STRONG_BURDEN_BEARER.get()));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.STRONG_BURDEN_BEARER.get()));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, ModPotions.STRONG_BURDEN_BEARER.get()));
                    }
                    
                    parameters.holders().lookup(Registries.ENCHANTMENT).ifPresent(enchantments -> {
                        enchantments.get(ModEnchantments.LOAD_BEARING).ifPresent(holder -> {
                            output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, 1)));
                            output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, 2)));
                            output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, 3)));
                        });
                    });
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
