package com.alikuxac.humannature.api.nutrition;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface INutritionAccessor {
    byte MAX_NUTRIENT = 100;

    byte getCalcium(Player player);
    byte getCalcium(UUID playerId);
    void setCalcium(Player player, int amount);
    void setCalcium(UUID playerId, int amount);
    void modifyCalcium(Player player, int delta);
    void modifyCalcium(UUID playerId, int delta);

    byte getCarbohydrates(Player player);
    byte getCarbohydrates(UUID playerId);
    void setCarbohydrates(Player player, int amount);
    void setCarbohydrates(UUID playerId, int amount);
    void modifyCarbohydrates(Player player, int delta);
    void modifyCarbohydrates(UUID playerId, int delta);

    byte getVitamins(Player player);
    byte getVitamins(UUID playerId);
    void setVitamins(Player player, int amount);
    void setVitamins(UUID playerId, int amount);
    void modifyVitamins(Player player, int delta);
    void modifyVitamins(UUID playerId, int delta);

    float getToxinBuildup(Player player);
    float getToxinBuildup(UUID playerId);
    void setToxinBuildup(Player player, float amount);
    void setToxinBuildup(UUID playerId, float amount);

    float getExertionScore(Player player);
    float getExertionScore(UUID playerId);
}

