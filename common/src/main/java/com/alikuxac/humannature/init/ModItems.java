package com.alikuxac.humannature.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.item.SplintItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HumanNatureCommon.MOD_ID);

    public static final DeferredItem<Item> SPLINT = ITEMS.register("splint", SplintItem::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}