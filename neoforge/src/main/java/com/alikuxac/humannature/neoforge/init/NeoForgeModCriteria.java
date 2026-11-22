package com.alikuxac.humannature.neoforge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.advancement.HumanNatureAdvancements;
import com.alikuxac.humannature.advancement.HumanNatureTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModCriteria {
    private static final DeferredRegister<CriterionTrigger<?>> CRITERIA =
            DeferredRegister.create(Registries.TRIGGER_TYPE, HumanNatureCommon.MOD_ID);
    private static final DeferredHolder<CriterionTrigger<?>, HumanNatureTrigger> GAMEPLAY_EVENT =
            CRITERIA.register("gameplay_event", HumanNatureTrigger::new);

    public static void register(IEventBus eventBus) {
        HumanNatureAdvancements.GAMEPLAY_EVENT = GAMEPLAY_EVENT;
        CRITERIA.register(eventBus);
    }
}
