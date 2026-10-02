package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.config.CommonConfig;

public class WeightCalculator {
    public static float calculateWeight(Object player) {
        if (!CommonConfig.ENABLE_WEIGHT.get()) return 0.0f;
        // Tag-based evaluation (#humannature:heavy_blocks)
        return 0.0f;
    }
}
