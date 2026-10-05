package com.alikuxac.humannature.item;

import com.alikuxac.humannature.config.CommonConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;

public class SplintItem extends Item {
    public static final int MAX_DURABILITY = 100;
    public static final int STEPS_PER_DURABILITY = 50;

    public SplintItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(MAX_DURABILITY)
                .fireResistant());
    }

    public static boolean isValidSplint(ItemStack stack) {
        return stack.getItem() instanceof SplintItem && stack.getDamageValue() < stack.getMaxDamage();
    }

    public static void consumeDurability(Player player, ItemStack stack) {
        if (!CommonConfig.ENABLE_SPLINT.get()) return;
        if (!isValidSplint(stack)) return;

        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    public static int getRemainingSteps(ItemStack stack) {
        if (!isValidSplint(stack)) return 0;
        return (stack.getMaxDamage() - stack.getDamageValue()) * STEPS_PER_DURABILITY;
    }
}