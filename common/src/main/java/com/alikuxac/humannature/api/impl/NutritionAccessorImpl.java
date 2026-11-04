package com.alikuxac.humannature.api.impl;

import com.alikuxac.humannature.api.nutrition.INutritionAccessor;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class NutritionAccessorImpl implements INutritionAccessor {

    @Override
    public byte getCalcium(Player player) {
        return player != null ? getCalcium(player.getUUID()) : 0;
    }

    @Override
    public byte getCalcium(UUID playerId) {
        if (playerId == null) return 0;
        return PhysiologyModule.getOrCreatePhysiologyData(playerId).getCalcium();
    }

    @Override
    public void setCalcium(Player player, int amount) {
        if (player != null) setCalcium(player.getUUID(), amount);
    }

    @Override
    public void setCalcium(UUID playerId, int amount) {
        if (playerId != null) {
            PhysiologyModule.getOrCreatePhysiologyData(playerId).setCalcium(amount);
        }
    }

    @Override
    public void modifyCalcium(Player player, int delta) {
        if (player != null) modifyCalcium(player.getUUID(), delta);
    }

    @Override
    public void modifyCalcium(UUID playerId, int delta) {
        if (playerId != null) {
            PhysiologyModule.getOrCreatePhysiologyData(playerId).modifyCalcium(delta);
        }
    }

    @Override
    public byte getCarbohydrates(Player player) {
        return player != null ? getCarbohydrates(player.getUUID()) : 0;
    }

    @Override
    public byte getCarbohydrates(UUID playerId) {
        if (playerId == null) return 0;
        return PhysiologyModule.getOrCreatePhysiologyData(playerId).getCarbohydrates();
    }

    @Override
    public void setCarbohydrates(Player player, int amount) {
        if (player != null) setCarbohydrates(player.getUUID(), amount);
    }

    @Override
    public void setCarbohydrates(UUID playerId, int amount) {
        if (playerId != null) {
            PhysiologyModule.getOrCreatePhysiologyData(playerId).setCarbohydrates(amount);
        }
    }

    @Override
    public void modifyCarbohydrates(Player player, int delta) {
        if (player != null) modifyCarbohydrates(player.getUUID(), delta);
    }

    @Override
    public void modifyCarbohydrates(UUID playerId, int delta) {
        if (playerId != null) {
            PhysiologyModule.getOrCreatePhysiologyData(playerId).modifyCarbohydrates(delta);
        }
    }

    @Override
    public byte getVitamins(Player player) {
        return player != null ? getVitamins(player.getUUID()) : 0;
    }

    @Override
    public byte getVitamins(UUID playerId) {
        if (playerId == null) return 0;
        return PhysiologyModule.getOrCreatePhysiologyData(playerId).getVitamins();
    }

    @Override
    public void setVitamins(Player player, int amount) {
        if (player != null) setVitamins(player.getUUID(), amount);
    }

    @Override
    public void setVitamins(UUID playerId, int amount) {
        if (playerId != null) {
            PhysiologyModule.getOrCreatePhysiologyData(playerId).setVitamins(amount);
        }
    }

    @Override
    public void modifyVitamins(Player player, int delta) {
        if (player != null) modifyVitamins(player.getUUID(), delta);
    }

    @Override
    public void modifyVitamins(UUID playerId, int delta) {
        if (playerId != null) {
            PhysiologyModule.getOrCreatePhysiologyData(playerId).modifyVitamins(delta);
        }
    }

    @Override
    public float getToxinBuildup(Player player) {
        return player != null ? getToxinBuildup(player.getUUID()) : 0.0f;
    }

    @Override
    public float getToxinBuildup(UUID playerId) {
        if (playerId == null) return 0.0f;
        return PhysiologyModule.getOrCreatePhysiologyData(playerId).getToxinBuildup();
    }

    @Override
    public void setToxinBuildup(Player player, float amount) {
        if (player != null) setToxinBuildup(player.getUUID(), amount);
    }

    @Override
    public void setToxinBuildup(UUID playerId, float amount) {
        if (playerId != null) {
            PhysiologyModule.getOrCreatePhysiologyData(playerId).setToxinBuildup(amount);
        }
    }

    @Override
    public float getExertionScore(Player player) {
        return player != null ? getExertionScore(player.getUUID()) : 0.0f;
    }

    @Override
    public float getExertionScore(UUID playerId) {
        if (playerId == null) return 0.0f;
        return PhysiologyModule.getOrCreatePhysiologyData(playerId).getExertionScore();
    }
}

