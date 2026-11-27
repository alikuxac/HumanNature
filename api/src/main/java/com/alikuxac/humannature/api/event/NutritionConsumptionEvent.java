package com.alikuxac.humannature.api.event;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class NutritionConsumptionEvent {
    private final Player player;
    private final ItemStack stack;
    private final NutritionValues defaultValues;
    private NutritionValues overrideValues;

    public NutritionConsumptionEvent(Player player, ItemStack stack, NutritionValues defaultValues) {
        this.player = Objects.requireNonNull(player, "player");
        this.stack = Objects.requireNonNull(stack, "stack").copy();
        this.defaultValues = Objects.requireNonNull(defaultValues, "defaultValues");
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getStack() {
        return stack.copy();
    }

    public NutritionValues getDefaultValues() {
        return defaultValues;
    }

    public NutritionValues getValues() {
        return isOverridden() ? overrideValues : defaultValues;
    }

    public boolean isOverridden() {
        return overrideValues != null;
    }

    public void setValues(NutritionValues values) {
        overrideValues = Objects.requireNonNull(values, "values");
    }
}
