package com.alikuxac.humannature.client;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import com.alikuxac.humannature.compat.jei.FoodNutritionRecipe;
import com.alikuxac.humannature.compat.jei.HumanNatureJeiPlugin;
import com.alikuxac.humannature.network.SyncFoodNutritionPayload;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HumanNatureJeiPluginTest {
    private final HumanNatureJeiPlugin plugin = new HumanNatureJeiPlugin();
    private static final ResourceLocation APPLE = ResourceLocation.parse("minecraft:apple");
    private static final ResourceLocation BREAD = ResourceLocation.parse("minecraft:bread");

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void reset() {
        plugin.onRuntimeUnavailable();
        FoodNutritionClientData.clear();
    }

    @Test
    void reloadUpdatesValuesAndVisibilityWithoutRegisteringDuplicateRecipes() {
        var added = new ArrayList<FoodNutritionRecipe>();
        Set<FoodNutritionRecipe> visible = new HashSet<>();
        var registration = mock(IRecipeRegistration.class);
        doAnswer(call -> {
            List<FoodNutritionRecipe> recipes = call.getArgument(1);
            added.addAll(recipes);
            visible.addAll(recipes);
            return null;
        }).when(registration).addRecipes(eq(HumanNatureJeiPlugin.NUTRITION), anyList());
        sync(Map.of(APPLE, new NutritionValues(0, 8, 20)));
        plugin.registerRecipes(registration);
        assertEquals(1, added.size());
        FoodNutritionRecipe apple = added.getFirst();

        var manager = mock(IRecipeManager.class);
        doAnswer(call -> {
            List<FoodNutritionRecipe> recipes = call.getArgument(1);
            added.addAll(recipes);
            visible.addAll(recipes);
            return null;
        }).when(manager).addRecipes(eq(HumanNatureJeiPlugin.NUTRITION), anyList());
        doAnswer(call -> { visible.removeAll(call.getArgument(1)); return null; })
                .when(manager).hideRecipes(eq(HumanNatureJeiPlugin.NUTRITION), anyCollection());
        doAnswer(call -> { visible.addAll(call.getArgument(1)); return null; })
                .when(manager).unhideRecipes(eq(HumanNatureJeiPlugin.NUTRITION), anyCollection());
        var runtime = mock(IJeiRuntime.class);
        when(runtime.getRecipeManager()).thenReturn(manager);
        plugin.onRuntimeAvailable(runtime);

        sync(Map.of(APPLE, new NutritionValues(0, 2, 5), BREAD, new NutritionValues(0, 25, 0)));
        assertEquals(2, added.size());
        assertEquals(2, visible.size());
        assertEquals(new NutritionValues(0, 2, 5), apple.gains());
        sync(Map.of(APPLE, new NutritionValues(0, 0, 0)));
        assertTrue(visible.isEmpty());
        sync(Map.of(APPLE, new NutritionValues(1, 2, 3)));
        assertEquals(Set.of(apple), visible);
        assertEquals(2, added.size());
        assertEquals(new NutritionValues(1, 2, 3), apple.gains());
        FoodNutritionClientData.clear();
        assertTrue(visible.isEmpty());
    }

    private static void sync(Map<ResourceLocation, NutritionValues> values) {
        FoodNutritionClientData.apply(new SyncFoodNutritionPayload(true, true, values));
    }
}
