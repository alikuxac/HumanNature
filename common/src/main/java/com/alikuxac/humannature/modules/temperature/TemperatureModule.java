package com.alikuxac.humannature.modules.temperature;

import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;

public class TemperatureModule implements IHumanModule {
    @Override
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return ServerConfig.ENABLE_TEMPERATURE.get();
    }
}
