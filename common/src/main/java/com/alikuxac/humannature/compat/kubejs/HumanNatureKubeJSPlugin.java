package com.alikuxac.humannature.compat.kubejs;

import com.alikuxac.humannature.api.injury.InjuryTier;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.api.weight.EncumbranceTier;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class HumanNatureKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("HumanNature", HumanNatureKubeJSHelper.class);
        bindings.add("LimbType", LimbType.class);
        bindings.add("InjuryTier", InjuryTier.class);
        bindings.add("EncumbranceTier", EncumbranceTier.class);
    }
}
