package com.alikuxac.humannature.neoforge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.init.ModPotions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, HumanNatureCommon.MOD_ID);

    public static final DeferredHolder<Potion, Potion> BURDEN_BEARER = POTIONS.register("burden_bearer",
            () -> new Potion(new MobEffectInstance(NeoForgeModEffects.BURDEN_BEARER, 3600, 0)));

    public static final DeferredHolder<Potion, Potion> LONG_BURDEN_BEARER = POTIONS.register("long_burden_bearer",
            () -> new Potion(new MobEffectInstance(NeoForgeModEffects.BURDEN_BEARER, 9600, 0)));

    public static final DeferredHolder<Potion, Potion> STRONG_BURDEN_BEARER = POTIONS.register("strong_burden_bearer",
            () -> new Potion(new MobEffectInstance(NeoForgeModEffects.BURDEN_BEARER, 1800, 1)));

    public static void register(IEventBus eventBus) {
        ModPotions.BURDEN_BEARER = () -> BURDEN_BEARER;
        ModPotions.LONG_BURDEN_BEARER = () -> LONG_BURDEN_BEARER;
        ModPotions.STRONG_BURDEN_BEARER = () -> STRONG_BURDEN_BEARER;
        POTIONS.register(eventBus);
    }
}
