package com.alikuxac.humannature.init;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {
    public static Supplier<Item> WOODEN_SPLINT = () -> null;
    public static Supplier<Item> REINFORCED_SPLINT = () -> null;
    public static Supplier<Item> GOLDEN_SPLINT = () -> null;
    public static Supplier<Item> DIAMOND_SPLINT = () -> null;
    public static Supplier<Item> NETHERITE_SPLINT = () -> null;
    public static Supplier<Item> BONE_BROTH = () -> null;

    // Legacy fallback alias
    public static Supplier<Item> SPLINT = () -> WOODEN_SPLINT.get();
}