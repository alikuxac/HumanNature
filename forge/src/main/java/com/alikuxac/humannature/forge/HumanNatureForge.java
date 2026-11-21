package com.alikuxac.humannature.forge;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.forge.config.ForgeConfig;
import com.alikuxac.humannature.forge.event.ForgeHumanNatureEventBridge;
import com.alikuxac.humannature.forge.init.ForgeCreativeTabs;
import com.alikuxac.humannature.forge.init.ForgeModEffects;
import com.alikuxac.humannature.forge.init.ForgeModItems;
import com.alikuxac.humannature.forge.init.ForgeModPotions;
import com.alikuxac.humannature.forge.init.ForgeModSounds;
import com.alikuxac.humannature.forge.network.ForgeNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HumanNatureCommon.MOD_ID)
public class HumanNatureForge {
    public HumanNatureForge(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ForgeConfig.register(context);
        HumanNatureCommon.init();
        ForgeNetwork.register();
        ForgeHumanNatureEventBridge.register();
        ForgeModEffects.register(modBus);
        ForgeModPotions.register(modBus);
        ForgeModItems.register(modBus);
        ForgeModSounds.register(modBus);
        ForgeCreativeTabs.register(modBus);
        modBus.addListener(ForgeCreativeTabs::onBuildCreativeModeTabContents);
    }
}

