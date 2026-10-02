package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;

public class InjuryModule implements IHumanModule {
    @Override
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return CommonConfig.ENABLE_INJURY.get();
    }
}
