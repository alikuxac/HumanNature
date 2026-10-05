package com.alikuxac.humannature.neoforge;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.init.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(HumanNatureCommon.MOD_ID)
public class HumanNatureNeoForge {
    public HumanNatureNeoForge(IEventBus modBus, ModContainer container) {
        HumanNatureCommon.init();
        ModItems.register(modBus);
        HumanNatureCommon.registerCuriosSlots(modBus);
    }
}
