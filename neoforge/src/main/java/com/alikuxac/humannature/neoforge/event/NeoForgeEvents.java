package com.alikuxac.humannature.neoforge.event;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber(modid = HumanNatureCommon.MOD_ID)
public class NeoForgeEvents {

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        InjuryEventHandler.onLivingFall(event.getEntity(), event.getDistance(), event.getDamageMultiplier());
    }
}

