package com.alikuxac.humannature.client;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import com.alikuxac.humannature.compat.jei.FoodNutritionRecipe;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FoodNutritionRecipeTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void onlyKnownFoodsWithPositiveGainsAreListedInStableOrder() {
        var recipes = FoodNutritionRecipe.visible(Map.of(
                ResourceLocation.parse("minecraft:milk_bucket"), new NutritionValues(25, 0, 0),
                ResourceLocation.parse("minecraft:apple"), new NutritionValues(0, 8, 20),
                ResourceLocation.parse("minecraft:bread"), new NutritionValues(0, 0, 0),
                ResourceLocation.parse("missing:food"), new NutritionValues(100, 100, 100)));
        assertEquals(2, recipes.size());
        assertEquals(ResourceLocation.parse("minecraft:apple"), recipes.getFirst().itemId());
        assertEquals(ResourceLocation.parse("minecraft:milk_bucket"), recipes.getLast().itemId());
    }
}
