package com.alikuxac.humannature.modules.physiology;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public enum ToxinType {
    NONE("Clear", "§a", 0xFF55FF55),
    POISON("Poison", "§2", 0xFF00E676),
    WITHER("Wither", "§5", 0xFFBA68C8);

    private final String displayName;
    private final String colorCode;
    private final int rgbColor;

    ToxinType(String displayName, String colorCode, int rgbColor) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.rgbColor = rgbColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorCode() {
        return colorCode;
    }

    public int getRgbColor() {
        return rgbColor;
    }

    public Holder<MobEffect> getEffectHolder() {
        if (this == POISON) return MobEffects.POISON;
        if (this == WITHER) return MobEffects.WITHER;
        return null;
    }
}
