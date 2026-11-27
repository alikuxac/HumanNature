package com.alikuxac.humannature.client;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import com.alikuxac.humannature.network.SyncFoodNutritionPayload;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FoodNutritionClientDataTest {
    private static final ResourceLocation APPLE = ResourceLocation.parse("minecraft:apple");
    private static final ResourceLocation BREAD = ResourceLocation.parse("minecraft:bread");

    @AfterEach
    void reset() {
        FoodNutritionClientData.setChangeListener(() -> {});
        FoodNutritionClientData.clear();
    }

    @Test
    void publishesCompleteSnapshotOnceAndRemovesStaleFoods() {
        FoodNutritionClientData.apply(new SyncFoodNutritionPayload(true, true, Map.of(BREAD, new NutritionValues(0, 25, 0))));
        var changes = new AtomicInteger();
        FoodNutritionClientData.setChangeListener(changes::incrementAndGet);
        FoodNutritionClientData.apply(new SyncFoodNutritionPayload(true, false, Map.of(APPLE, new NutritionValues(0, 8, 20))));
        assertEquals(1, FoodNutritionClientData.snapshot().size());
        assertTrue(FoodNutritionClientData.snapshot().containsKey(BREAD));
        assertEquals(0, changes.get());
        FoodNutritionClientData.apply(new SyncFoodNutritionPayload(false, true, Map.of()));
        assertEquals(Map.of(APPLE, new NutritionValues(0, 8, 20)), FoodNutritionClientData.snapshot());
        assertEquals(1, changes.get());
        assertThrows(UnsupportedOperationException.class, () -> FoodNutritionClientData.snapshot().clear());
    }

    @Test
    void disconnectDiscardsPendingSnapshotAndNotifiesViewer() {
        FoodNutritionClientData.apply(new SyncFoodNutritionPayload(true, true, Map.of(APPLE, new NutritionValues(0, 8, 20))));
        FoodNutritionClientData.apply(new SyncFoodNutritionPayload(true, false, Map.of(BREAD, new NutritionValues(0, 25, 0))));
        var changes = new AtomicInteger();
        FoodNutritionClientData.setChangeListener(changes::incrementAndGet);
        FoodNutritionClientData.clear();
        FoodNutritionClientData.apply(new SyncFoodNutritionPayload(false, true, Map.of()));
        assertTrue(FoodNutritionClientData.snapshot().isEmpty());
        assertEquals(1, changes.get());
    }
}
