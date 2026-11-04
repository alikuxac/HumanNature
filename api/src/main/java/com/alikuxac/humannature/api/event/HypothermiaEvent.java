package com.alikuxac.humannature.api.event;

import net.minecraft.world.entity.player.Player;

public class HypothermiaEvent {
    private final Player player;
    private final float coreTemperature;
    private boolean canceled;

    public HypothermiaEvent(Player player, float coreTemperature) {
        this.player = player;
        this.coreTemperature = coreTemperature;
        this.canceled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public float getCoreTemperature() {
        return coreTemperature;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
