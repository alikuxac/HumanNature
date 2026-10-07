package com.alikuxac.humannature.forge;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.init.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(HumanNatureCommon.MOD_ID)
public class HumanNatureForge {
    public HumanNatureForge(IEventBus modBus, ModContainer container) {
        HumanNatureCommon.init();
        ModItems.register(modBus);
    }
}
