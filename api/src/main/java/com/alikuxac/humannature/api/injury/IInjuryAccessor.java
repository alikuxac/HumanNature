package com.alikuxac.humannature.api.injury;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface IInjuryAccessor {
    byte getLimbSeverity(Player player, LimbType limb);
    byte getLimbSeverity(UUID playerId, LimbType limb);

    void setLimbSeverity(Player player, LimbType limb, int severity);
    void setLimbSeverity(UUID playerId, LimbType limb, int severity);

    boolean hasAnyInjury(Player player);
    boolean hasAnyInjury(UUID playerId);

    int getTotalInjuries(Player player);
    int getTotalInjuries(UUID playerId);

    boolean hasFracture(Player player);
    boolean hasFracture(UUID playerId);

    InjuryTier getFractureTier(Player player);
    InjuryTier getFractureTier(UUID playerId);

    void treatLimb(Player player, LimbType limb);
    void treatLimb(UUID playerId, LimbType limb);

    void clearInjuries(Player player);
    void clearInjuries(UUID playerId);
}

