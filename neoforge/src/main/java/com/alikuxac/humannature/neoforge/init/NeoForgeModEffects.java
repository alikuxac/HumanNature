package com.alikuxac.humannature.neoforge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.effect.BurdenBearerMobEffect;
import com.alikuxac.humannature.init.ModEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, HumanNatureCommon.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> BURDEN_BEARER = MOB_EFFECTS.register("burden_bearer", BurdenBearerMobEffect::new);

    public static void register(IEventBus eventBus) {
        ModEffects.BURDEN_BEARER = () -> BURDEN_BEARER;
        MOB_EFFECTS.register(eventBus);
    }
}
