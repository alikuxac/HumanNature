package com.alikuxac.humannature.modules.physiology;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public enum ToxinType {
    NONE("Clear", "§a", 0xFF55FF55, null),
    POISON("Poison", "§2", 0xFF00E676, MobEffects.POISON),
    WITHER("Wither", "§5", 0xFFBA68C8, MobEffects.WITHER);

    private final String displayName;
    private final String colorCode;
    private final int rgbColor;
    private final Holder<MobEffect> effectHolder;

    ToxinType(String displayName, String colorCode, int rgbColor, Holder<MobEffect> effectHolder) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.rgbColor = rgbColor;
        this.effectHolder = effectHolder;
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
        return effectHolder;
    }
}
