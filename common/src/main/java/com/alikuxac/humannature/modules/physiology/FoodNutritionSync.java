package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.network.SyncFoodNutritionPayload;
import com.alikuxac.humannature.api.nutrition.NutritionValues;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Map;

public final class FoodNutritionSync {
    private FoodNutritionSync() {}

    public static void sync(Collection<ServerPlayer> players) {
        if (players.isEmpty()) return;
        Map<ResourceLocation, NutritionValues> values = NutritionEventHandler.isEnabled()
                ? FoodNutritionReloadListener.INSTANCE.snapshot() : Map.of();
        for (var payload : SyncFoodNutritionPayload.split(values)) {
            var packet = new ClientboundCustomPayloadPacket(payload);
            for (var player : players) player.connection.send(packet);
        }
    }
}
