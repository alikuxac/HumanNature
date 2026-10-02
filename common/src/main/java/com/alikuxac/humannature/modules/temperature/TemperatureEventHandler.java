package com.alikuxac.humannature.modules.temperature;

import com.alikuxac.humannature.config.CommonConfig;

public class TemperatureEventHandler {
    public static void onPlayerTick() {
        if (!CommonConfig.ENABLE_TEMPERATURE.get()) return;
        // Temperature wetness / hypothermia logic
    }
}
