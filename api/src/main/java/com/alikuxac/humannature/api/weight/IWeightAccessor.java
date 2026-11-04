package com.alikuxac.humannature.api.weight;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface IWeightAccessor {
    float getCarriedWeight(Player player);
    float getCarriedWeight(UUID playerId);

    float getMaxCapacity(Player player);
    float getMaxCapacity(UUID playerId);

    EncumbranceTier getEncumbranceTier(Player player);
    EncumbranceTier getEncumbranceTier(UUID playerId);

    boolean isEncumbered(Player player);
    boolean isEncumbered(UUID playerId);
}

