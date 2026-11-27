package com.alikuxac.humannature.compat.jei;

import com.alikuxac.humannature.client.FoodNutritionClientData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@JeiPlugin
public final class HumanNatureJeiPlugin implements IModPlugin {
    public static final RecipeType<FoodNutritionRecipe> NUTRITION = new RecipeType<>(
            ResourceLocation.parse("humannature:nutrition"), FoodNutritionRecipe.class);
    private final Map<ResourceLocation, FoodNutritionRecipe> registered = new LinkedHashMap<>();
    private IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.parse("humannature:nutrition_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new NutritionCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registered.clear();
        var recipes = FoodNutritionRecipe.visible(FoodNutritionClientData.snapshot());
        recipes.forEach(recipe -> registered.put(recipe.itemId(), recipe));
        registration.addRecipes(NUTRITION, recipes);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;
        FoodNutritionClientData.setChangeListener(this::refresh);
        refresh();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        FoodNutritionClientData.setChangeListener(() -> {});
        registered.clear();
    }

    private void refresh() {
        if (runtime == null) return;
        var recipes = FoodNutritionRecipe.visible(FoodNutritionClientData.snapshot());
        var visibleIds = recipes.stream().map(FoodNutritionRecipe::itemId).collect(Collectors.toSet());
        var hidden = registered.values().stream().filter(recipe -> !visibleIds.contains(recipe.itemId())).toList();
        var existing = new ArrayList<FoodNutritionRecipe>();
        var added = new ArrayList<FoodNutritionRecipe>();
        for (var recipe : recipes) {
            FoodNutritionRecipe previous = registered.putIfAbsent(recipe.itemId(), recipe);
            if (previous == null) added.add(recipe);
            else existing.add(previous);
        }
        var manager = runtime.getRecipeManager();
        if (!hidden.isEmpty()) manager.hideRecipes(NUTRITION, hidden);
        if (!existing.isEmpty()) manager.unhideRecipes(NUTRITION, existing);
        if (!added.isEmpty()) manager.addRecipes(NUTRITION, added);
    }
}
