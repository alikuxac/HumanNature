package com.alikuxac.humannature;

import com.alikuxac.humannature.api.HumanNatureAPI;
import com.alikuxac.humannature.api.impl.HumanNatureAPIImpl;

public class HumanNatureCommon {
    public static final String MOD_ID = BuildConstants.MOD_ID;

    public static void init() {
        if (!HumanNatureAPI.isAvailable()) {
            HumanNatureAPI.setInstance(new HumanNatureAPIImpl());
        }
    }
}
