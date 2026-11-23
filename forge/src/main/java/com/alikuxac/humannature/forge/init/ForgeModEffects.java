package com.alikuxac.humannature.forge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.effect.BurdenBearerMobEffect;
import com.alikuxac.humannature.effect.MarkerMobEffect;
import com.alikuxac.humannature.init.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgeModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HumanNatureCommon.MOD_ID);

    public static final RegistryObject<MobEffect> BURDEN_BEARER = MOB_EFFECTS.register("burden_bearer", BurdenBearerMobEffect::new);
    public static final RegistryObject<MobEffect> FRACTURE = MOB_EFFECTS.register("fracture", () -> new MarkerMobEffect(0xE05A47));
    public static final RegistryObject<MobEffect> HYPOTHERMIA = MOB_EFFECTS.register("hypothermia", () -> new MarkerMobEffect(0x7FDBFF));
    public static final RegistryObject<MobEffect> HEAT_EXHAUSTION = MOB_EFFECTS.register("heat_exhaustion", () -> new MarkerMobEffect(0xFF4136));
    public static final RegistryObject<MobEffect> OSTEOGENESIS = MOB_EFFECTS.register("osteogenesis", () -> new MarkerMobEffect(0xFFD700));
    public static final RegistryObject<MobEffect> TOXIN = MOB_EFFECTS.register("toxin", () -> new MarkerMobEffect(0x9B59B6));

    public static void register(IEventBus eventBus) {
        ModEffects.BURDEN_BEARER = () -> BURDEN_BEARER.getHolder().orElse(null);
        MOB_EFFECTS.register(eventBus);
    }
}

