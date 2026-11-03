package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.config.ServerConfig;

public enum EncumbranceTier {
    NORMAL("Normal", "§a", 0.0, 0.0, 1.0),
    LIGHT("Light", "§e", 0.10, 0.015, 1.25),
    MEDIUM("Medium", "§6", 0.25, 0.035, 1.50),
    OVERBURDENED("Overburdened", "§c", 0.50, 0.065, 2.00);

    private final String displayName;
    private final String colorCode;
    private final double defaultSpeedPenalty;
    private final double defaultSinkPull;
    private final double defaultFallDamageMultiplier;

    EncumbranceTier(String displayName, String colorCode, double defaultSpeedPenalty, double defaultSinkPull, double defaultFallDamageMultiplier) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.defaultSpeedPenalty = defaultSpeedPenalty;
        this.defaultSinkPull = defaultSinkPull;
        this.defaultFallDamageMultiplier = defaultFallDamageMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getSpeedPenalty() {
        return switch (this) {
            case LIGHT -> ServerConfig.WEIGHT_LIGHT_SPEED_PENALTY != null ? ServerConfig.WEIGHT_LIGHT_SPEED_PENALTY.get() : defaultSpeedPenalty;
            case MEDIUM -> ServerConfig.WEIGHT_MEDIUM_SPEED_PENALTY != null ? ServerConfig.WEIGHT_MEDIUM_SPEED_PENALTY.get() : defaultSpeedPenalty;
            case OVERBURDENED -> ServerConfig.WEIGHT_OVERBURDENED_SPEED_PENALTY != null ? ServerConfig.WEIGHT_OVERBURDENED_SPEED_PENALTY.get() : defaultSpeedPenalty;
            default -> 0.0;
        };
    }

    public double getSinkPull() {
        return switch (this) {
            case LIGHT -> ServerConfig.WEIGHT_LIGHT_SINK_PULL != null ? ServerConfig.WEIGHT_LIGHT_SINK_PULL.get() : defaultSinkPull;
            case MEDIUM -> ServerConfig.WEIGHT_MEDIUM_SINK_PULL != null ? ServerConfig.WEIGHT_MEDIUM_SINK_PULL.get() : defaultSinkPull;
            case OVERBURDENED -> ServerConfig.WEIGHT_OVERBURDENED_SINK_PULL != null ? ServerConfig.WEIGHT_OVERBURDENED_SINK_PULL.get() : defaultSinkPull;
            default -> 0.0;
        };
    }

    public double getFallDamageMultiplier() {
        return switch (this) {
            case LIGHT -> ServerConfig.WEIGHT_LIGHT_FALL_DAMAGE_MULTIPLIER != null ? ServerConfig.WEIGHT_LIGHT_FALL_DAMAGE_MULTIPLIER.get() : defaultFallDamageMultiplier;
            case MEDIUM -> ServerConfig.WEIGHT_MEDIUM_FALL_DAMAGE_MULTIPLIER != null ? ServerConfig.WEIGHT_MEDIUM_FALL_DAMAGE_MULTIPLIER.get() : defaultFallDamageMultiplier;
            case OVERBURDENED -> ServerConfig.WEIGHT_OVERBURDENED_FALL_DAMAGE_MULTIPLIER != null ? ServerConfig.WEIGHT_OVERBURDENED_FALL_DAMAGE_MULTIPLIER.get() : defaultFallDamageMultiplier;
            default -> 1.0;
        };
    }

    public static EncumbranceTier fromWeightRatio(float ratio) {
        double overburdenedThresh = ServerConfig.WEIGHT_OVERBURDENED_THRESHOLD != null ? ServerConfig.WEIGHT_OVERBURDENED_THRESHOLD.get() : 0.85;
        double mediumThresh = ServerConfig.WEIGHT_MEDIUM_THRESHOLD != null ? ServerConfig.WEIGHT_MEDIUM_THRESHOLD.get() : 0.70;
        double lightThresh = ServerConfig.WEIGHT_LIGHT_THRESHOLD != null ? ServerConfig.WEIGHT_LIGHT_THRESHOLD.get() : 0.50;

        if (ratio >= overburdenedThresh) {
            return OVERBURDENED;
        } else if (ratio >= mediumThresh) {
            return MEDIUM;
        } else if (ratio >= lightThresh) {
            return LIGHT;
        }
        return NORMAL;
    }

    public boolean isEncumbered() {
        return this != NORMAL;
    }
}

