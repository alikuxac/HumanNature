package com.alikuxac.humannature.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class BoneBrothItem extends Item {

    public static final FoodProperties BONE_BROTH_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6f)
            .alwaysEdible()
            .build();

    public BoneBrothItem(Properties properties) {
        super(properties.food(BONE_BROTH_FOOD).stacksTo(16));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !player.hasInfiniteMaterials()) {
            ItemStack emptyBowl = new ItemStack(Items.BOWL);
            if (stack.isEmpty()) {
                return emptyBowl;
            }
            if (!player.getInventory().add(emptyBowl)) {
                player.drop(emptyBowl, false);
            }
        }
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }
}
