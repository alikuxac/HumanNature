package com.alikuxac.humannature.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class ModKeyMappings {
    public static final KeyMapping OPEN_DIAGNOSTICS = new KeyMapping(
            "key.humannature.open_diagnostics",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_H,
            "key.categories.humannature"
    );

    public static final KeyMapping CYCLE_HUD = new KeyMapping(
            "key.humannature.cycle_hud",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            "key.categories.humannature"
    );
}
