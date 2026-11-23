package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;

import java.util.function.Supplier;

public final class NutritionBuffs {

    private static final byte MAX_RESERVE = 100;

    private NutritionBuffs() {
    }

    public static double knittingMultiplier(byte calcium) {
        return 1.0 + config(ServerConfig.NUTRITION_CALCIUM_KNITTING_BONUS, 0.25) * ratio(calcium);
    }

    public static float fallBonusBlocks(byte calcium) {
        return (float) (config(ServerConfig.NUTRITION_CALCIUM_FALL_BONUS_BLOCKS, 2.0) * ratio(calcium));
    }

    public static float fallDamageMultiplier(byte calcium) {
        return (float) Math.max(0.0, 1.0 - config(ServerConfig.NUTRITION_CALCIUM_FALL_DAMAGE_REDUCTION, 0.25) * ratio(calcium));
    }

    public static float staminaCapacity(byte carbohydrates) {
        return (float) (MAX_RESERVE + config(ServerConfig.NUTRITION_CARBS_STAMINA_BONUS, 50.0) * ratio(carbohydrates));
    }

    public static float coldAbsorptionMultiplier(byte vitamins) {
        return (float) Math.max(0.0, 1.0 - config(ServerConfig.NUTRITION_VITAMIN_COLD_REDUCTION, 0.50) * ratio(vitamins));
    }

    public static float toxinEliminationMultiplier(byte vitamins) {
        return (float) (1.0 + config(ServerConfig.NUTRITION_VITAMIN_TOXIN_BONUS, 1.0) * ratio(vitamins));
    }

    private static double ratio(byte reserve) {
        return Math.max(0.0, Math.min(1.0, reserve / (double) MAX_RESERVE));
    }

    private static double config(Supplier<Double> supplier, double fallback) {
        return supplier != null ? Math.max(0.0, supplier.get()) : fallback;
    }
}
