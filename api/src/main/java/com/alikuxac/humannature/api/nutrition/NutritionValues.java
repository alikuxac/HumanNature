package com.alikuxac.humannature.api.nutrition;

public record NutritionValues(int calcium, int carbohydrates, int vitamins) {
    public NutritionValues {
        calcium = clamp(calcium);
        carbohydrates = clamp(carbohydrates);
        vitamins = clamp(vitamins);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(INutritionAccessor.MAX_NUTRIENT, value));
    }
}
