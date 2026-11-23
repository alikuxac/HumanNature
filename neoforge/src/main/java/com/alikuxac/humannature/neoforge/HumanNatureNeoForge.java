package com.alikuxac.humannature.neoforge;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.command.HumanNatureCommands;
import com.alikuxac.humannature.neoforge.config.NeoForgeConfig;
import com.alikuxac.humannature.neoforge.event.NeoForgeHumanNatureEventBridge;
import com.alikuxac.humannature.neoforge.init.NeoForgeCreativeTabs;
import com.alikuxac.humannature.neoforge.init.NeoForgeModCriteria;
import com.alikuxac.humannature.neoforge.init.NeoForgeModEffects;
import com.alikuxac.humannature.neoforge.init.NeoForgeModItems;
import com.alikuxac.humannature.neoforge.init.NeoForgeModPotions;
import com.alikuxac.humannature.neoforge.init.NeoForgeModSounds;
import com.alikuxac.humannature.neoforge.network.NeoForgeNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(HumanNatureCommon.MOD_ID)
public class HumanNatureNeoForge {
    public HumanNatureNeoForge(IEventBus modBus, ModContainer container) {
        NeoForgeConfig.register(container);
        HumanNatureCommon.init();
        NeoForgeModCriteria.register(modBus);
        NeoForgeHumanNatureEventBridge.register();
        NeoForgeModEffects.register(modBus);
        NeoForgeModPotions.register(modBus);
        NeoForgeModItems.register(modBus);
        NeoForgeModSounds.register(modBus);
        NeoForgeCreativeTabs.register(modBus);
        modBus.addListener(NeoForgeCreativeTabs::onBuildCreativeModeTabContents);
        modBus.addListener(NeoForgeNetwork::onRegisterPayloadHandlers);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> HumanNatureCommands.register(event.getDispatcher()));
    }
}

