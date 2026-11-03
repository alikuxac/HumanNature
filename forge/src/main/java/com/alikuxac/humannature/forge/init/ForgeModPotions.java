package com.alikuxac.humannature.forge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.init.ModPotions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgeModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, HumanNatureCommon.MOD_ID);

    public static final RegistryObject<Potion> BURDEN_BEARER = POTIONS.register("burden_bearer",
            () -> new Potion(new MobEffectInstance(ForgeModEffects.BURDEN_BEARER.getHolder().get(), 3600, 0)));

    public static final RegistryObject<Potion> LONG_BURDEN_BEARER = POTIONS.register("long_burden_bearer",
            () -> new Potion(new MobEffectInstance(ForgeModEffects.BURDEN_BEARER.getHolder().get(), 9600, 0)));

    public static final RegistryObject<Potion> STRONG_BURDEN_BEARER = POTIONS.register("strong_burden_bearer",
            () -> new Potion(new MobEffectInstance(ForgeModEffects.BURDEN_BEARER.getHolder().get(), 1800, 1)));

    public static void register(IEventBus eventBus) {
        ModPotions.BURDEN_BEARER = () -> BURDEN_BEARER.getHolder().orElse(null);
        ModPotions.LONG_BURDEN_BEARER = () -> LONG_BURDEN_BEARER.getHolder().orElse(null);
        ModPotions.STRONG_BURDEN_BEARER = () -> STRONG_BURDEN_BEARER.getHolder().orElse(null);
        POTIONS.register(eventBus);
    }
}

