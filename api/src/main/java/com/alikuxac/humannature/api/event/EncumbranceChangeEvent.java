package com.alikuxac.humannature.api.event;

import com.alikuxac.humannature.api.weight.EncumbranceTier;
import net.minecraft.world.entity.player.Player;

public class EncumbranceChangeEvent {
    private final Player player;
    private final EncumbranceTier oldTier;
    private final EncumbranceTier newTier;
    private final float carriedWeight;

    public EncumbranceChangeEvent(Player player, EncumbranceTier oldTier, EncumbranceTier newTier, float carriedWeight) {
        this.player = player;
        this.oldTier = oldTier != null ? oldTier : EncumbranceTier.NORMAL;
        this.newTier = newTier != null ? newTier : EncumbranceTier.NORMAL;
        this.carriedWeight = carriedWeight;
    }

    public Player getPlayer() {
        return player;
    }

    public EncumbranceTier getOldTier() {
        return oldTier;
    }

    public EncumbranceTier getNewTier() {
        return newTier;
    }

    public float getCarriedWeight() {
        return carriedWeight;
    }
}
