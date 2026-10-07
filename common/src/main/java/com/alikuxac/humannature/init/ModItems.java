package com.alikuxac.humannature.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.item.SplintTier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HumanNatureCommon.MOD_ID);

    public static final DeferredItem<Item> WOODEN_SPLINT = ITEMS.register("wooden_splint", () -> new SplintItem(SplintTier.WOODEN));
    public static final DeferredItem<Item> REINFORCED_SPLINT = ITEMS.register("reinforced_splint", () -> new SplintItem(SplintTier.REINFORCED));
    public static final DeferredItem<Item> GOLDEN_SPLINT = ITEMS.register("golden_splint", () -> new SplintItem(SplintTier.GOLDEN));
    public static final DeferredItem<Item> DIAMOND_SPLINT = ITEMS.register("diamond_splint", () -> new SplintItem(SplintTier.DIAMOND));
    public static final DeferredItem<Item> NETHERITE_SPLINT = ITEMS.register("netherite_splint", () -> new SplintItem(SplintTier.NETHERITE));

    // Legacy fallback alias
    public static final DeferredItem<Item> SPLINT = WOODEN_SPLINT;

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}