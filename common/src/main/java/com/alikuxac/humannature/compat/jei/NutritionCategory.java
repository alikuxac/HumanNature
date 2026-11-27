package com.alikuxac.humannature.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class NutritionCategory implements IRecipeCategory<FoodNutritionRecipe> {
    private final IDrawable icon;
    private final Component title = Component.translatable("jei.humannature.nutrition.title");
    private final Component serving = Component.translatable("jei.humannature.nutrition.per_serving");
    private final Component[][] labels = new Component[3][101];

    public NutritionCategory(IGuiHelper helper) {
        icon = helper.createDrawableItemStack(new ItemStack(Items.APPLE));
        String[] keys = {"calcium", "carbohydrates", "vitamins"};
        for (int row = 0; row < keys.length; row++) {
            for (int gain = 0; gain <= 100; gain++) {
                labels[row][gain] = Component.translatable("jei.humannature.nutrition." + keys[row], gain);
            }
        }
    }

    @Override
    public RecipeType<FoodNutritionRecipe> getRecipeType() {
        return HumanNatureJeiPlugin.NUTRITION;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public int getHeight() {
        return 80;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FoodNutritionRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(6, 34).setStandardSlotBackground()
                .addItemStack(new ItemStack(BuiltInRegistries.ITEM.get(recipe.itemId())));
    }

    @Override
    public ResourceLocation getRegistryName(FoodNutritionRecipe recipe) {
        return recipe.itemId();
    }

    @Override
    public void draw(FoodNutritionRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        var gains = recipe.gains();
        graphics.drawString(font, serving, 6, 6, 0x666666, false);
        graphics.drawString(font, labels[0][gains.calcium()], 32, 24, 0x404040, false);
        graphics.drawString(font, labels[1][gains.carbohydrates()], 32, 40, 0x404040, false);
        graphics.drawString(font, labels[2][gains.vitamins()], 32, 56, 0x404040, false);
    }
}
