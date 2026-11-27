package com.alikuxac.humannature.compat.jei;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import com.alikuxac.humannature.client.FoodNutritionClientData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

public record FoodNutritionRecipe(ResourceLocation itemId) {
    private static final NutritionValues ZERO_GAINS = new NutritionValues(0, 0, 0);

    public NutritionValues gains() {
        return FoodNutritionClientData.snapshot().getOrDefault(itemId, ZERO_GAINS);
    }

    public static List<FoodNutritionRecipe> visible(Map<ResourceLocation, NutritionValues> values) {
        return values.entrySet().stream()
                .filter(entry -> entry.getValue().calcium() > 0 || entry.getValue().carbohydrates() > 0 || entry.getValue().vitamins() > 0)
                .filter(entry -> BuiltInRegistries.ITEM.containsKey(entry.getKey())
                        && BuiltInRegistries.ITEM.get(entry.getKey()) != Items.AIR)
                .map(entry -> new FoodNutritionRecipe(entry.getKey()))
                .sorted((left, right) -> left.itemId().toString().compareTo(right.itemId().toString()))
                .toList();
    }
}
