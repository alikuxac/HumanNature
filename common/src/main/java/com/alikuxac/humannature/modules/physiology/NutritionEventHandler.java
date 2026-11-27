package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.advancement.HumanNatureAdvancements;
import com.alikuxac.humannature.api.event.HumanNatureEvents;
import com.alikuxac.humannature.api.event.NutritionConsumptionEvent;
import com.alikuxac.humannature.api.nutrition.NutritionValues;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public final class NutritionEventHandler {

    private static final NutritionValues ZERO_GAINS = new NutritionValues(0, 0, 0);

    private NutritionEventHandler() {
    }

    public static boolean isEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_NUTRIENT_RESERVES.get();
    }

    public static void handleItemConsumption(Player player, ItemStack stack) {
        if (!isEnabled()) return;
        if (player == null || player.level().isClientSide() || player.isCreative() || player.isSpectator()) return;
        if (stack == null || stack.isEmpty()) return;

        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        boolean wasMaxNutrition = hasMaxNutrition(data);

        NutritionValues defaults = FoodNutritionReloadListener.INSTANCE.get(stack.getItem());
        if (defaults == null) defaults = ZERO_GAINS;
        NutritionConsumptionEvent event = HumanNatureEvents.postNutritionConsumption(
                new NutritionConsumptionEvent(player, stack, defaults));
        NutritionValues values = event.getValues();
        data.modifyCalcium(values.calcium());
        data.modifyCarbohydrates(values.carbohydrates());
        data.modifyVitamins(values.vitamins());
        if (!wasMaxNutrition && hasMaxNutrition(data)) {
            HumanNatureAdvancements.trigger(player, HumanNatureAdvancements.MAX_NUTRITION);
        }
        PhysiologyEventHandler.syncNutritionToClient(player, data);
    }

    private static boolean hasMaxNutrition(PlayerPhysiologyData data) {
        return data.getCalcium() == 100 && data.getCarbohydrates() == 100 && data.getVitamins() == 100;
    }

    public static void handlePlayerTick(Player player, PlayerPhysiologyData data) {
        if (!isEnabled()) return;

        int interval = ServerConfig.NUTRIENT_DEPLETION_INTERVAL_TICKS != null
                ? ServerConfig.NUTRIENT_DEPLETION_INTERVAL_TICKS.get() : 600;
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

}
