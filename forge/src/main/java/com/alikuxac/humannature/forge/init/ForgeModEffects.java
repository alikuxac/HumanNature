package com.alikuxac.humannature.forge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.effect.BurdenBearerMobEffect;
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

    public static void register(IEventBus eventBus) {
        ModEffects.BURDEN_BEARER = () -> BURDEN_BEARER.getHolder().orElse(null);
        MOB_EFFECTS.register(eventBus);
    }
}

