package com.alikuxac.humannature.init;

import com.alikuxac.humannature.HumanNatureCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> LOAD_BEARING = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "load_bearing")
    );
}

