package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;

public class PhysiologyModule implements IHumanModule {
    @Override
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return CommonConfig.ENABLE_PHYSIOLOGY.get();
    }
}
