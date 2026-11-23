package com.alikuxac.humannature.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class MarkerMobEffect extends MobEffect {
    public MarkerMobEffect(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }
}
