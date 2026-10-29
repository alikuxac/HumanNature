package com.alikuxac.humannature.neoforge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.init.ModItems;
import com.alikuxac.humannature.item.BoneBrothItem;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.item.SplintTier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HumanNatureCommon.MOD_ID);

    public static final DeferredItem<Item> WOODEN_SPLINT = ITEMS.register("wooden_splint", () -> new SplintItem(SplintTier.WOODEN));
    public static final DeferredItem<Item> REINFORCED_SPLINT = ITEMS.register("reinforced_splint", () -> new SplintItem(SplintTier.REINFORCED));
    public static final DeferredItem<Item> GOLDEN_SPLINT = ITEMS.register("golden_splint", () -> new SplintItem(SplintTier.GOLDEN));
    public static final DeferredItem<Item> DIAMOND_SPLINT = ITEMS.register("diamond_splint", () -> new SplintItem(SplintTier.DIAMOND));
    public static final DeferredItem<Item> NETHERITE_SPLINT = ITEMS.register("netherite_splint", () -> new SplintItem(SplintTier.NETHERITE));
    public static final DeferredItem<Item> BONE_BROTH = ITEMS.register("bone_broth", () -> new BoneBrothItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ModItems.WOODEN_SPLINT = WOODEN_SPLINT;
        ModItems.REINFORCED_SPLINT = REINFORCED_SPLINT;
        ModItems.GOLDEN_SPLINT = GOLDEN_SPLINT;
        ModItems.DIAMOND_SPLINT = DIAMOND_SPLINT;
        ModItems.NETHERITE_SPLINT = NETHERITE_SPLINT;
        ModItems.BONE_BROTH = BONE_BROTH;
        ModItems.SPLINT = WOODEN_SPLINT;

        ITEMS.register(eventBus);
    }
}
