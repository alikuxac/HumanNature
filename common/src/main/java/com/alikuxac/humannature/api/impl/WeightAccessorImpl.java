package com.alikuxac.humannature.api.impl;

import com.alikuxac.humannature.api.weight.EncumbranceTier;
import com.alikuxac.humannature.api.weight.IWeightAccessor;
import com.alikuxac.humannature.modules.weight.PlayerWeightData;
import com.alikuxac.humannature.modules.weight.WeightModule;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class WeightAccessorImpl implements IWeightAccessor {

    @Override
    public float getCarriedWeight(Player player) {
        return player != null ? getCarriedWeight(player.getUUID()) : 0.0f;
    }

    @Override
    public float getCarriedWeight(UUID playerId) {
        if (playerId == null) return 0.0f;
        return WeightModule.getOrCreateWeightData(playerId).getCachedWeight();
    }

    @Override
    public float getMaxCapacity(Player player) {
        return player != null ? getMaxCapacity(player.getUUID()) : 500.0f;
    }

    @Override
    public float getMaxCapacity(UUID playerId) {
        if (playerId == null) return 500.0f;
        return WeightModule.getOrCreateWeightData(playerId).getCachedMaxCapacity();
    }

    @Override
    public EncumbranceTier getEncumbranceTier(Player player) {
        return player != null ? getEncumbranceTier(player.getUUID()) : EncumbranceTier.NORMAL;
    }

    @Override
    public EncumbranceTier getEncumbranceTier(UUID playerId) {
        if (playerId == null) return EncumbranceTier.NORMAL;
        PlayerWeightData data = WeightModule.getOrCreateWeightData(playerId);
        com.alikuxac.humannature.modules.weight.EncumbranceTier internalTier = data.getPreviousTier();
        if (internalTier == null) return EncumbranceTier.NORMAL;
        return switch (internalTier) {
            case LIGHT -> EncumbranceTier.LIGHT;
            case MEDIUM -> EncumbranceTier.MEDIUM;
            case OVERBURDENED -> EncumbranceTier.OVERBURDENED;
            default -> EncumbranceTier.NORMAL;
        };
    }

    @Override
    public boolean isEncumbered(Player player) {
        return getEncumbranceTier(player).isEncumbered();
    }

    @Override
    public boolean isEncumbered(UUID playerId) {
        return getEncumbranceTier(playerId).isEncumbered();
    }
}

