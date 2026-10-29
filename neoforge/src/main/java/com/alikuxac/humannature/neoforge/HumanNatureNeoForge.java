package com.alikuxac.humannature.neoforge;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.neoforge.config.NeoForgeConfig;
import com.alikuxac.humannature.neoforge.init.NeoForgeCreativeTabs;
import com.alikuxac.humannature.neoforge.init.NeoForgeModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(HumanNatureCommon.MOD_ID)
public class HumanNatureNeoForge {
    public HumanNatureNeoForge(IEventBus modBus, ModContainer container) {
        NeoForgeConfig.register(container);
        HumanNatureCommon.init();
        NeoForgeModItems.register(modBus);
        NeoForgeCreativeTabs.register(modBus);
        modBus.addListener(NeoForgeCreativeTabs::onBuildCreativeModeTabContents);
    }
}

