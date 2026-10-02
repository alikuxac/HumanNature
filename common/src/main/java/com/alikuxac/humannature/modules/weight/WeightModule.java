package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;

public class WeightModule implements IHumanModule {
    @Override
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return CommonConfig.ENABLE_WEIGHT.get();
    }
}
