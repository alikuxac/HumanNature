package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WeightCalculator {
    public static final TagKey<Item> COMMON_HEAVY_BLOCKS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "heavy_blocks"));
    public static final TagKey<Item> COMMON_METALLIC_ITEMS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "metallic_items"));
    public static final TagKey<Item> COMMON_LIGHT_ITEMS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "light_items"));

    public static final TagKey<Item> MOD_HEAVY_BLOCKS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "heavy_blocks"));
    public static final TagKey<Item> MOD_METALLIC_ITEMS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "metallic_items"));
    public static final TagKey<Item> MOD_LIGHT_ITEMS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "light_items"));

    public static float calculateWeight(Player player) {
        if (!ServerConfig.ENABLE_WEIGHT.get() || player == null) return 0.0f;

        float total = 0.0f;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                total += calculateStackWeight(stack);
            }
        }
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                total += calculateStackWeight(stack);
            }
        }
        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack stack = player.getInventory().offhand.get(i);
            if (!stack.isEmpty()) {
                total += calculateStackWeight(stack);
            }
        }
        return total;
    }

    public static float calculateStackWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0.0f;

        float baseWeight;
        if (stack.is(COMMON_HEAVY_BLOCKS) || stack.is(MOD_HEAVY_BLOCKS)) {
            baseWeight = ServerConfig.WEIGHT_HEAVY_BLOCK_BASE.get().floatValue();
        } else if (stack.is(COMMON_METALLIC_ITEMS) || stack.is(MOD_METALLIC_ITEMS)) {
            baseWeight = ServerConfig.WEIGHT_METALLIC_ITEM_BASE.get().floatValue();
        } else if (stack.is(COMMON_LIGHT_ITEMS) || stack.is(MOD_LIGHT_ITEMS)) {
            baseWeight = ServerConfig.WEIGHT_LIGHT_ITEM_BASE.get().floatValue();
        } else {
            baseWeight = ServerConfig.WEIGHT_DEFAULT_ITEM_BASE.get().floatValue();
        }
        return stack.getCount() * baseWeight;
    }
}
