package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

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

    public static float getItemWeight(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0f;

        float baseWeight;
        if (stack.is(COMMON_HEAVY_BLOCKS) || stack.is(MOD_HEAVY_BLOCKS)) {
            baseWeight = (float) ServerConfig.WEIGHT_HEAVY_BLOCK_BASE;
        } else if (stack.is(COMMON_METALLIC_ITEMS) || stack.is(MOD_METALLIC_ITEMS)) {
            baseWeight = (float) ServerConfig.WEIGHT_METALLIC_ITEM_BASE;
        } else if (stack.is(COMMON_LIGHT_ITEMS) || stack.is(MOD_LIGHT_ITEMS)) {
            baseWeight = (float) ServerConfig.WEIGHT_LIGHT_ITEM_BASE;
        } else {
            baseWeight = (float) ServerConfig.WEIGHT_DEFAULT_ITEM_BASE;
        }
        return baseWeight;
    }

    public static float getContentsWeight(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0f;
        float rawContents = 0.0f;

        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            for (ItemStack content : container.nonEmptyItems()) {
                rawContents += calculateStackWeight(content);
            }
        }

        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            for (ItemStack content : bundle.items()) {
                rawContents += calculateStackWeight(content);
            }
        }

        if (rawContents <= 0.0f) return 0.0f;

        double ratio = ServerConfig.WEIGHT_CONTAINER_RATIO;
        float contents = (float) (rawContents * ratio);

        int loadBearingLvl = getEnchantmentLevel(stack, com.alikuxac.humannature.init.ModEnchantments.LOAD_BEARING);
        if (loadBearingLvl > 0) {
            double reductionPerLvl = ServerConfig.LOAD_BEARING_CONTAINER_REDUCTION;
            double reduction = Math.min(1.0, loadBearingLvl * reductionPerLvl);
            contents *= (float) (1.0 - reduction);
        }

        return contents * stack.getCount();
    }

    public static float calculateStackWeight(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0f;
        float total = stack.getCount() * getItemWeight(stack);
        total += getContentsWeight(stack);
        return total;
    }

    public static float calculateMaxCapacity(Player player) {
        double base = ServerConfig.WEIGHT_MAX_CAPACITY != null ? ServerConfig.WEIGHT_MAX_CAPACITY.get() : 500.0;
        if (player == null) return (float) base;

        double bonus = 0.0;

        if (com.alikuxac.humannature.init.ModEffects.BURDEN_BEARER != null) {
            Holder<net.minecraft.world.effect.MobEffect> burdenHolder = com.alikuxac.humannature.init.ModEffects.BURDEN_BEARER.get();
            if (burdenHolder != null) {
                MobEffectInstance burden = player.getEffect(burdenHolder);
                if (burden != null) {
                    double perLvl = ServerConfig.BURDEN_BEARER_CAPACITY_BONUS;
                    bonus += (burden.getAmplifier() + 1) * perLvl;
                }
            }
        }

        double loadBearingBonus = ServerConfig.LOAD_BEARING_CAPACITY_BONUS;
        EquipmentSlot[] armorSlots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : armorSlots) {
            ItemStack armor = player.getItemBySlot(slot);
            int lvl = getEnchantmentLevel(armor, com.alikuxac.humannature.init.ModEnchantments.LOAD_BEARING);
            if (lvl > 0) {
                bonus += lvl * loadBearingBonus;
            }
        }

        return (float) Math.max(50.0, base + bonus);
    }

    public static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantmentKey) {
        if (stack == null || stack.isEmpty()) return 0;
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) return 0;
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (holder.is(enchantmentKey)) {
                return enchantments.getLevel(holder);
            }
        }
        return 0;
    }
}
