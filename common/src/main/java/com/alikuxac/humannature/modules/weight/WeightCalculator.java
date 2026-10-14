package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.config.ServerConfig;

public class WeightCalculator {
    public static float calculateWeight(Object player) {
        if (!ServerConfig.ENABLE_WEIGHT.get()) return 0.0f;
        // Tag-based evaluation (#humannature:heavy_blocks)
        return 0.0f;
    }
}
