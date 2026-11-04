package com.alikuxac.humannature.api.impl;

import com.alikuxac.humannature.api.injury.IInjuryAccessor;
import com.alikuxac.humannature.api.injury.InjuryTier;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class InjuryAccessorImpl implements IInjuryAccessor {

    @Override
    public byte getLimbSeverity(Player player, LimbType limb) {
        return player != null ? getLimbSeverity(player.getUUID(), limb) : 0;
    }

    @Override
    public byte getLimbSeverity(UUID playerId, LimbType limb) {
        if (playerId == null || limb == null) return 0;
        return InjuryModule.getOrCreateInjuryData(playerId).getSeverity(limb);
    }

    @Override
    public void setLimbSeverity(Player player, LimbType limb, int severity) {
        if (player != null) {
            setLimbSeverity(player.getUUID(), limb, severity);
            InjuryEventHandler.updatePlayerAttributesAndPose(player);
        }
    }

    @Override
    public void setLimbSeverity(UUID playerId, LimbType limb, int severity) {
        if (playerId != null && limb != null) {
            InjuryModule.setLimbSeverity(playerId, limb, severity);
        }
    }

    @Override
    public boolean hasAnyInjury(Player player) {
        return player != null && hasAnyInjury(player.getUUID());
    }

    @Override
    public boolean hasAnyInjury(UUID playerId) {
        if (playerId == null) return false;
        return InjuryModule.getOrCreateInjuryData(playerId).hasAnyInjury();
    }

    @Override
    public int getTotalInjuries(Player player) {
        return player != null ? getTotalInjuries(player.getUUID()) : 0;
    }

    @Override
    public int getTotalInjuries(UUID playerId) {
        if (playerId == null) return 0;
        return InjuryModule.getOrCreateInjuryData(playerId).getTotalInjuries();
    }

    @Override
    public boolean hasFracture(Player player) {
        return player != null && hasFracture(player.getUUID());
    }

    @Override
    public boolean hasFracture(UUID playerId) {
        if (playerId == null) return false;
        return InjuryModule.getFractureTier(playerId) != InjuryTier.NONE;
    }

    @Override
    public InjuryTier getFractureTier(Player player) {
        return player != null ? getFractureTier(player.getUUID()) : InjuryTier.NONE;
    }

    @Override
    public InjuryTier getFractureTier(UUID playerId) {
        if (playerId == null) return InjuryTier.NONE;
        return InjuryModule.getFractureTier(playerId);
    }

    @Override
    public void treatLimb(Player player, LimbType limb) {
        if (player != null) {
            treatLimb(player.getUUID(), limb);
            InjuryEventHandler.updatePlayerAttributesAndPose(player);
        }
    }

    @Override
    public void treatLimb(UUID playerId, LimbType limb) {
        if (playerId != null && limb != null) {
            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(playerId);
            int current = data.getSeverity(limb);
            if (current > 0) {
                data.setSeverity(limb, current - 1);
                InjuryModule.setInjuryData(playerId, data);
            }
        }
    }

    @Override
    public void clearInjuries(Player player) {
        if (player != null) {
            clearInjuries(player.getUUID());
            InjuryEventHandler.updatePlayerAttributesAndPose(player);
        }
    }

    @Override
    public void clearInjuries(UUID playerId) {
        if (playerId != null) {
            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(playerId);
            data.clear();
            InjuryModule.setInjuryData(playerId, data);
            InjuryModule.clearFractureTimer(playerId);
        }
    }
}

