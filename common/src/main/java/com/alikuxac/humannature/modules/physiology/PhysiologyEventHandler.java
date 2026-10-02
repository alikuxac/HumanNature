package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.CommonConfig;

public class PhysiologyEventHandler {
    public static void onTraumaOrSleep() {
        if (!CommonConfig.ENABLE_PHYSIOLOGY.get()) return;
        // Adrenaline surge, sleep deprivation, toxicity buildup handlers
    }
}
