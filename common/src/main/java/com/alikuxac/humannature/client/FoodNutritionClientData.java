package com.alikuxac.humannature.client;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import com.alikuxac.humannature.network.SyncFoodNutritionPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public final class FoodNutritionClientData {
    private static volatile Map<ResourceLocation, NutritionValues> current = Map.of();
    private static Map<ResourceLocation, NutritionValues> pending;
    private static Runnable changeListener = () -> {};

    private FoodNutritionClientData() {}

    public static void apply(SyncFoodNutritionPayload payload) {
        if (payload.first()) pending = new HashMap<>();
        if (pending == null) return;
        pending.putAll(payload.values());
        if (payload.last()) {
            current = Map.copyOf(pending);
            pending = null;
            changeListener.run();
        }
    }

    public static void clear() {
        current = Map.of();
        pending = null;
        changeListener.run();
    }

    public static void setChangeListener(Runnable listener) {
        changeListener = Objects.requireNonNull(listener);
    }

    public static Map<ResourceLocation, NutritionValues> snapshot() {
        return current;
    }
}
