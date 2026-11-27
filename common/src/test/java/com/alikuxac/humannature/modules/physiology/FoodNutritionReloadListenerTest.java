package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FoodNutritionReloadListenerTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void clear() {
        reload(Map.of());
    }

    @Test
    void reloadReplacesValuesAndRemovesEntriesAbsentFromNextPack() {
        reload(Map.of("apple", "{\"calcium\":2,\"carbohydrates\":7,\"vitamins\":18}"));
        assertEquals(new NutritionValues(2, 7, 18), FoodNutritionReloadListener.INSTANCE.get(Items.APPLE));
        reload(Map.of("apple", "{\"calcium\":0,\"carbohydrates\":0,\"vitamins\":0}"));
        assertEquals(new NutritionValues(0, 0, 0), FoodNutritionReloadListener.INSTANCE.get(Items.APPLE));
        reload(Map.of());
        assertNull(FoodNutritionReloadListener.INSTANCE.get(Items.APPLE));
    }

    @Test
    void invalidEntriesDoNotPreventOtherFoodsFromLoading() {
        reload(Map.of("apple", "{\"calcium\":1,\"carbohydrates\":2,\"vitamins\":3}"));
        for (String invalid : new String[]{
                "{}", "{\"calcium\":-1,\"carbohydrates\":0,\"vitamins\":0}",
                "{\"calcium\":101,\"carbohydrates\":0,\"vitamins\":0}",
                "{\"calcium\":1.5,\"carbohydrates\":0,\"vitamins\":0}",
                "{\"calcium\":\"5\",\"carbohydrates\":0,\"vitamins\":0}", "[]", "broken"}) {
            reload(Map.of("apple", invalid, "bread", "{\"calcium\":0,\"carbohydrates\":25,\"vitamins\":0}"));
            assertNull(FoodNutritionReloadListener.INSTANCE.get(Items.APPLE));
            assertEquals(new NutritionValues(0, 25, 0), FoodNutritionReloadListener.INSTANCE.get(Items.BREAD));
        }
    }

    @Test
    void higherPriorityPackReplacesWholePresetAndRemovingItRestoresPreset() throws Exception {
        String preset;
        try (var stream = getClass().getResourceAsStream("/data/minecraft/humannature/nutrition/apple.json")) {
            assertNotNull(stream);
            preset = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        var base = pack(preset);
        var disabled = pack("{\"calcium\":0,\"carbohydrates\":0,\"vitamins\":0}");
        var manager = new FallbackResourceManager(PackType.SERVER_DATA, "minecraft");
        manager.push(base);
        manager.push(disabled);
        reload(manager);
        assertEquals(new NutritionValues(0, 0, 0), FoodNutritionReloadListener.INSTANCE.get(Items.APPLE));

        var invalidOverride = new FallbackResourceManager(PackType.SERVER_DATA, "minecraft");
        invalidOverride.push(base);
        invalidOverride.push(pack("{\"vitamins\":9}"));
        reload(invalidOverride);
        assertNull(FoodNutritionReloadListener.INSTANCE.get(Items.APPLE));

        var restored = new FallbackResourceManager(PackType.SERVER_DATA, "minecraft");
        restored.push(base);
        reload(restored);
        assertEquals(new NutritionValues(0, 8, 20), FoodNutritionReloadListener.INSTANCE.get(Items.APPLE));
    }

    private static PackResources pack(String json) {
        var pack = mock(PackResources.class);
        doAnswer(invocation -> {
            PackResources.ResourceOutput output = invocation.getArgument(3);
            output.accept(ResourceLocation.parse("minecraft:humannature/nutrition/apple.json"),
                    () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
            return null;
        }).when(pack).listResources(eq(PackType.SERVER_DATA), eq("minecraft"), eq("humannature/nutrition"), any());
        return pack;
    }

    @Test
    void bundledPresetsCoverEveryVanillaFoodAndMilk() throws Exception {
        for (var item : BuiltInRegistries.ITEM) {
            if (item.components().has(DataComponents.FOOD) || item == Items.MILK_BUCKET) {
                var id = BuiltInRegistries.ITEM.getKey(item);
                try (var stream = getClass().getResourceAsStream("/data/" + id.getNamespace()
                        + "/humannature/nutrition/" + id.getPath() + ".json")) {
                    assertNotNull(stream, "Missing preset: " + id);
                    String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                    reload(Map.of(id.getPath(), json));
                    assertNotNull(FoodNutritionReloadListener.INSTANCE.get(item), "Invalid preset: " + id);
                }
            }
        }
    }

    static void reload(Map<String, String> files) {
        ResourceManager manager = mock(ResourceManager.class);
        Map<ResourceLocation, Resource> resources = new HashMap<>();
        files.forEach((name, json) -> resources.put(ResourceLocation.parse("minecraft:humannature/nutrition/" + name + ".json"),
                new Resource(mock(PackResources.class), () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))));
        when(manager.listResources(eq("humannature/nutrition"), any())).thenReturn(resources);
        reload(manager);
    }

    private static void reload(ResourceManager manager) {
        PreparableReloadListener.PreparationBarrier barrier = new PreparableReloadListener.PreparationBarrier() {
            public <T> CompletableFuture<T> wait(T prepared) { return CompletableFuture.completedFuture(prepared); }
        };
        FoodNutritionReloadListener.INSTANCE.reload(barrier, manager, InactiveProfiler.INSTANCE,
                InactiveProfiler.INSTANCE, Runnable::run, Runnable::run).join();
    }
}
