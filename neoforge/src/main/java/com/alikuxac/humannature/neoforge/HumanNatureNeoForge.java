package com.alikuxac.humannature.neoforge;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.neoforge.config.NeoForgeConfig;
import com.alikuxac.humannature.neoforge.event.NeoForgeHumanNatureEventBridge;
import com.alikuxac.humannature.neoforge.init.NeoForgeCreativeTabs;
import com.alikuxac.humannature.neoforge.init.NeoForgeModEffects;
import com.alikuxac.humannature.neoforge.init.NeoForgeModItems;
import com.alikuxac.humannature.neoforge.init.NeoForgeModPotions;
import com.alikuxac.humannature.neoforge.init.NeoForgeModSounds;
import com.alikuxac.humannature.neoforge.network.NeoForgeNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(HumanNatureCommon.MOD_ID)
public class HumanNatureNeoForge {
    public HumanNatureNeoForge(IEventBus modBus, ModContainer container) {
        NeoForgeConfig.register(container);
        HumanNatureCommon.init();
        NeoForgeHumanNatureEventBridge.register();
        NeoForgeModEffects.register(modBus);
        NeoForgeModPotions.register(modBus);
        NeoForgeModItems.register(modBus);
        NeoForgeModSounds.register(modBus);
        NeoForgeCreativeTabs.register(modBus);
        modBus.addListener(NeoForgeCreativeTabs::onBuildCreativeModeTabContents);
        modBus.addListener(NeoForgeNetwork::onRegisterPayloadHandlers);
    }
}

