package com.alikuxac.humannature.api.event;

import com.alikuxac.humannature.api.injury.LimbType;
import net.minecraft.world.entity.player.Player;

public class LimbInjuryEvent {
    private final Player player;
    private final LimbType limb;
    private int severity;
    private boolean canceled;

    public LimbInjuryEvent(Player player, LimbType limb, int severity) {
        this.player = player;
        this.limb = limb;
        this.severity = Math.max(0, Math.min(2, severity));
        this.canceled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public LimbType getLimb() {
        return limb;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = Math.max(0, Math.min(2, severity));
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
