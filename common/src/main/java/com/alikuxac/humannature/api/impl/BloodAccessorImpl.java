package com.alikuxac.humannature.api.impl;

import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.api.blood.IBloodAccessor;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class BloodAccessorImpl implements IBloodAccessor {

    @Override
    public short getBloodVolume(Player player) {
        return getBloodVolume(player.getUUID());
    }

    @Override
    public short getBloodVolume(UUID playerId) {
        return PhysiologyModule.getBloodVolume(playerId);
    }

    @Override
    public void setBloodVolume(Player player, short amount) {
        setBloodVolume(player.getUUID(), amount);
    }

    @Override
    public void setBloodVolume(UUID playerId, short amount) {
        PhysiologyModule.setBloodVolume(playerId, amount);
    }

    @Override
    public void modifyBloodVolume(Player player, int delta) {
        modifyBloodVolume(player.getUUID(), delta);
    }

    @Override
    public void modifyBloodVolume(UUID playerId, int delta) {
        PhysiologyModule.modifyBloodVolume(playerId, delta);
    }

    @Override
    public BloodType getBloodType(Player player) {
        return getBloodType(player.getUUID());
    }

    @Override
    public BloodType getBloodType(UUID playerId) {
        return PhysiologyModule.getBloodType(playerId);
    }

    @Override
    public void setBloodType(Player player, BloodType bloodType) {
        setBloodType(player.getUUID(), bloodType);
    }

    @Override
    public void setBloodType(UUID playerId, BloodType bloodType) {
        PhysiologyModule.setBloodType(playerId, bloodType);
    }

    @Override
    public byte getBleedingTier(Player player) {
        return getBleedingTier(player.getUUID());
    }

    @Override
    public byte getBleedingTier(UUID playerId) {
        return PhysiologyModule.getBleedingTier(playerId);
    }

    @Override
    public void setBleedingTier(Player player, byte tier) {
        setBleedingTier(player.getUUID(), tier);
    }

    @Override
    public void setBleedingTier(UUID playerId, byte tier) {
        PhysiologyModule.setBleedingTier(playerId, tier);
    }
}

