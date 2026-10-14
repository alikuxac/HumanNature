package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;

public class PhysiologyEventHandler {
    public static void onTraumaOrSleep() {
        if (!ServerConfig.ENABLE_PHYSIOLOGY.get()) return;
        // Adrenaline surge, sleep deprivation, toxicity buildup handlers
    }
}
