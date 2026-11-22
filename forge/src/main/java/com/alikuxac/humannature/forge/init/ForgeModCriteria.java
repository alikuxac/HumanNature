package com.alikuxac.humannature.forge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.advancement.HumanNatureAdvancements;
import com.alikuxac.humannature.advancement.HumanNatureTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ForgeModCriteria {
    private static final DeferredRegister<CriterionTrigger<?>> CRITERIA =
            DeferredRegister.create(Registries.TRIGGER_TYPE, HumanNatureCommon.MOD_ID);
    private static final RegistryObject<HumanNatureTrigger> GAMEPLAY_EVENT =
            CRITERIA.register("gameplay_event", HumanNatureTrigger::new);

    public static void register(IEventBus eventBus) {
        HumanNatureAdvancements.GAMEPLAY_EVENT = GAMEPLAY_EVENT;
        CRITERIA.register(eventBus);
    }
}
