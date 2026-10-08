package com.alikuxac.humannature.modules.injury;

public enum TraumaSeverity {
    HEALTHY(0, 0xFF2E7D32, "Healthy"),
    MINOR(1, 0xFFF57F17, "Minor / Sprained"),
    MAJOR(2, 0xFFD32F2F, "Severe / Broken");

    private final int level;
    private final int colorRgb;
    private final String displayName;

    TraumaSeverity(int level, int colorRgb, String displayName) {
        this.level = level;
        this.colorRgb = colorRgb;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public int getColorRgb() {
        return colorRgb;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TraumaSeverity fromLevel(int level) {
        if (level >= 2) return MAJOR;
        if (level == 1) return MINOR;
        return HEALTHY;
    }
}
