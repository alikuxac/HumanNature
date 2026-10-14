package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;

public class PhysiologyModule implements IHumanModule {
    @Override
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get();
    }
}
