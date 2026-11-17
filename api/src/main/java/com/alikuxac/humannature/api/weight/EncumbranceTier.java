package com.alikuxac.humannature.api.weight;

public enum EncumbranceTier {
    NORMAL("Normal", "§a"),
    LIGHT("Light", "§e"),
    MEDIUM("Medium", "§6"),
    OVERBURDENED("Overburdened", "§c");

    private final String displayName;
    private final String colorCode;

    EncumbranceTier(String displayName, String colorCode) {
        this.displayName = displayName;
        this.colorCode = colorCode;
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

    public boolean isEncumbered() {
        return this != NORMAL;
    }
}

