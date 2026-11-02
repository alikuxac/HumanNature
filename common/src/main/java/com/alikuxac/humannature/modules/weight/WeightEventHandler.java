package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.world.entity.player.Player;

public class WeightEventHandler {

    public static void recalculateWeight(Player player) {
        if (player == null || player.level().isClientSide() || !ServerConfig.ENABLE_WEIGHT.get()) {
            return;
        }
        float weight = WeightCalculator.calculateWeight(player);
        PlayerWeightData data = WeightModule.getOrCreateWeightData(player.getUUID());
        data.setCachedWeight(weight);
    }

    public static void onItemPickup(Player player) {
        recalculateWeight(player);
    }

    public static void onItemToss(Player player) {
        recalculateWeight(player);
    }

    public static void onContainerClose(Player player) {
        recalculateWeight(player);
    }

    public static void onEquipmentChange(Player player) {
        recalculateWeight(player);
    }

    public static void onPlayerLoggedIn(Player player) {
        recalculateWeight(player);
    }

    public static void onPlayerRespawn(Player player) {
        PlayerWeightData data = WeightModule.getWeightData(player.getUUID());
        if (data != null) {
            data.reset();
        }
        recalculateWeight(player);
    }

    public static void onPlayerLoggedOut(Player player) {
        WeightModule.removeWeightData(player.getUUID());
    }
}
