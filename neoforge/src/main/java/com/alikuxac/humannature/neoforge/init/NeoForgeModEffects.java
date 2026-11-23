package com.alikuxac.humannature.neoforge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.effect.BurdenBearerMobEffect;
import com.alikuxac.humannature.effect.MarkerMobEffect;
import com.alikuxac.humannature.init.ModEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, HumanNatureCommon.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> BURDEN_BEARER = MOB_EFFECTS.register("burden_bearer", BurdenBearerMobEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FRACTURE = MOB_EFFECTS.register("fracture", () -> new MarkerMobEffect(0xE05A47));
    public static final DeferredHolder<MobEffect, MobEffect> HYPOTHERMIA = MOB_EFFECTS.register("hypothermia", () -> new MarkerMobEffect(0x7FDBFF));
    public static final DeferredHolder<MobEffect, MobEffect> HEAT_EXHAUSTION = MOB_EFFECTS.register("heat_exhaustion", () -> new MarkerMobEffect(0xFF4136));
    public static final DeferredHolder<MobEffect, MobEffect> OSTEOGENESIS = MOB_EFFECTS.register("osteogenesis", () -> new MarkerMobEffect(0xFFD700));
    public static final DeferredHolder<MobEffect, MobEffect> TOXIN = MOB_EFFECTS.register("toxin", () -> new MarkerMobEffect(0x9B59B6));

    public static void register(IEventBus eventBus) {
        ModEffects.BURDEN_BEARER = () -> BURDEN_BEARER;
        MOB_EFFECTS.register(eventBus);
    }
}
