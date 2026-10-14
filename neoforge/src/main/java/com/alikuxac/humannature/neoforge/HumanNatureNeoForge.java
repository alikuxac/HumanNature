package com.alikuxac.humannature.neoforge;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.init.ModCreativeTabs;
import com.alikuxac.humannature.init.ModItems;
import com.alikuxac.humannature.neoforge.config.NeoForgeConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(HumanNatureCommon.MOD_ID)
public class HumanNatureNeoForge {
    public HumanNatureNeoForge(IEventBus modBus, ModContainer container) {
        NeoForgeConfig.register(container);
        HumanNatureCommon.init();
        ModItems.register(modBus);
        ModCreativeTabs.register(modBus);
        modBus.addListener(ModCreativeTabs::onBuildCreativeModeTabContents);
    }
}
