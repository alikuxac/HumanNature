package com.alikuxac.humannature.modules.temperature;

import com.alikuxac.humannature.config.ServerConfig;

public class TemperatureEventHandler {
    public static void onPlayerTick() {
        if (!ServerConfig.ENABLE_TEMPERATURE.get()) return;
        // Temperature wetness / hypothermia logic
    }
}
