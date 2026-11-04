package com.alikuxac.humannature.api.event;

import com.alikuxac.humannature.api.injury.InjuryTier;
import net.minecraft.world.entity.player.Player;

public class FractureEvent {
    private final Player player;
    private final InjuryTier tier;
    private final boolean healed;

    public FractureEvent(Player player, InjuryTier tier, boolean healed) {
        this.player = player;
        this.tier = tier;
        this.healed = healed;
    }

    public Player getPlayer() {
        return player;
    }

    public InjuryTier getTier() {
        return tier;
    }

    public boolean isHealed() {
        return healed;
    }
}
