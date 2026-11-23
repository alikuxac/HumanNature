package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.ChatFormatting;

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

    public String getTranslationKey() {
        return "humannature.encumbrance." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public String getColorCode() {
        return colorCode;
    }

    public ChatFormatting getChatFormatting() {
        return switch (this) {
            case NORMAL -> ChatFormatting.GREEN;
            case LIGHT -> ChatFormatting.YELLOW;
            case MEDIUM -> ChatFormatting.GOLD;
            case OVERBURDENED -> ChatFormatting.RED;
        };
    }

    public double getSpeedPenalty() {
        double mult = ServerConfig.WEIGHT_PENALTY_MULTIPLIER != null ? ServerConfig.WEIGHT_PENALTY_MULTIPLIER.get() : 1.0;
        double base = switch (this) {
            case LIGHT -> ServerConfig.WEIGHT_LIGHT_SPEED_PENALTY.get();
            case MEDIUM -> ServerConfig.WEIGHT_MEDIUM_SPEED_PENALTY.get();
            case OVERBURDENED -> ServerConfig.WEIGHT_OVERBURDENED_SPEED_PENALTY.get();
            default -> 0.0;
        };
        return base * mult;
    }

    public double getSinkPull() {
        double mult = ServerConfig.WEIGHT_PENALTY_MULTIPLIER != null ? ServerConfig.WEIGHT_PENALTY_MULTIPLIER.get() : 1.0;
        double base = switch (this) {
            case LIGHT -> ServerConfig.WEIGHT_LIGHT_SINK_PULL.get();
            case MEDIUM -> ServerConfig.WEIGHT_MEDIUM_SINK_PULL.get();
            case OVERBURDENED -> ServerConfig.WEIGHT_OVERBURDENED_SINK_PULL.get();
            default -> 0.0;
        };
        return base * mult;
    }

    public double getFallDamageMultiplier() {
        double mult = ServerConfig.WEIGHT_PENALTY_MULTIPLIER != null ? ServerConfig.WEIGHT_PENALTY_MULTIPLIER.get() : 1.0;
        double base = switch (this) {
            case LIGHT -> ServerConfig.WEIGHT_LIGHT_FALL_DAMAGE_MULTIPLIER.get();
            case MEDIUM -> ServerConfig.WEIGHT_MEDIUM_FALL_DAMAGE_MULTIPLIER.get();
            case OVERBURDENED -> ServerConfig.WEIGHT_OVERBURDENED_FALL_DAMAGE_MULTIPLIER.get();
            default -> 1.0;
        };
        return 1.0 + (base - 1.0) * mult;
    }

    public static EncumbranceTier fromWeightRatio(float ratio) {
        if (ratio >= ServerConfig.WEIGHT_OVERBURDENED_THRESHOLD.get()) {
            return OVERBURDENED;
        } else if (ratio >= ServerConfig.WEIGHT_MEDIUM_THRESHOLD.get()) {
            return MEDIUM;
        } else if (ratio >= ServerConfig.WEIGHT_LIGHT_THRESHOLD.get()) {
            return LIGHT;
        }
        return NORMAL;
    }

    public boolean isEncumbered() {
        return this != NORMAL;
    }
}

