package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;

public final class FoodNutritionReloadListener extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final FoodNutritionReloadListener INSTANCE = new FoodNutritionReloadListener();
    private volatile Map<ResourceLocation, NutritionValues> values = Map.of();

    private FoodNutritionReloadListener() {
        super(new Gson(), "humannature/nutrition");
    }

    public NutritionValues get(Item item) {
        return values.get(BuiltInRegistries.ITEM.getKey(item));
    }

    public Map<ResourceLocation, NutritionValues> snapshot() {
        return values;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, NutritionValues> loaded = new HashMap<>();
        entries.forEach((id, json) -> {
            try {
                if (!BuiltInRegistries.ITEM.containsKey(id)) {
                    throw new IllegalArgumentException("Unknown item " + id);
                }
                JsonObject object = json.getAsJsonObject();
                loaded.put(id, new NutritionValues(readGain(object, "calcium"),
                        readGain(object, "carbohydrates"), readGain(object, "vitamins")));
            } catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
                LOGGER.error("Ignoring food nutrition {}: {}", id, exception.getMessage());
            }
        });
        values = Map.copyOf(loaded);
    }

    private static int readGain(JsonObject object, String name) {
        JsonElement element = object.get(name);
        if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()) {
            throw new IllegalArgumentException(name + " must be an integer from 0 to 100");
        }
        int value = primitive.getAsBigDecimal().intValueExact();
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(name + " must be an integer from 0 to 100");
        }
        return value;
    }
}
