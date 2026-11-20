package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class NutritionEventHandler {

    public static final TagKey<Item> CALCIUM_RICH_FOODS = commonTag("foods/calcium_rich");
    public static final TagKey<Item> CARBOHYDRATE_FOODS = commonTag("foods/carbohydrates");
    public static final TagKey<Item> VEGETABLE_FOODS = commonTag("foods/vegetables");
    public static final TagKey<Item> FRUIT_FOODS = commonTag("foods/fruits");

    private NutritionEventHandler() {
    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    public static boolean isEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_NUTRIENT_RESERVES.get();
    }

    public static void handleItemConsumption(Player player, ItemStack stack) {
        if (!isEnabled()) return;
        if (player == null || player.isCreative() || player.isSpectator()) return;
        if (stack == null || stack.isEmpty()) return;

        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());

        if (stack.is(CALCIUM_RICH_FOODS)) {
            data.modifyCalcium(gain(ServerConfig.CALCIUM_FOOD_GAIN, 25));
        }
        if (stack.is(CARBOHYDRATE_FOODS)) {
            data.modifyCarbohydrates(gain(ServerConfig.CARBOHYDRATE_FOOD_GAIN, 25));
        }
        if (stack.is(VEGETABLE_FOODS) || stack.is(FRUIT_FOODS)) {
            data.modifyVitamins(gain(ServerConfig.VITAMIN_FOOD_GAIN, 20));
        }
        PhysiologyEventHandler.syncNutritionToClient(player, data);
    }

    public static void handlePlayerTick(Player player, PlayerPhysiologyData data) {
        if (!isEnabled()) return;

        int interval = ServerConfig.NUTRIENT_DEPLETION_INTERVAL_TICKS != null
                ? ServerConfig.NUTRIENT_DEPLETION_INTERVAL_TICKS.get() : 150;
        if (interval <= 0) return;

        data.incrementNutrientTickCounter();
        if (data.getNutrientTickCounter() >= interval) {
            data.resetNutrientTickCounter();
            int amount = ServerConfig.NUTRIENT_DEPLETION_AMOUNT != null
                    ? ServerConfig.NUTRIENT_DEPLETION_AMOUNT.get() : 1;
            data.modifyCalcium(-amount);
            data.modifyCarbohydrates(-amount);
            data.modifyVitamins(-amount);
            PhysiologyEventHandler.syncNutritionToClient(player, data);
        }
    }

    private static int gain(Supplier<Integer> supplier, int fallback) {
        return supplier != null ? Math.max(0, supplier.get()) : fallback;
    }
}
