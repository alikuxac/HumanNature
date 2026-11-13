package com.alikuxac.humannature.api.blood;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface IBloodAccessor {
    short DEFAULT_BLOOD_VOLUME = 5000;
    short MAX_BLOOD_VOLUME = 5000;

    short getBloodVolume(Player player);
    short getBloodVolume(UUID playerId);
    void setBloodVolume(Player player, short amount);
    void setBloodVolume(UUID playerId, short amount);
    void modifyBloodVolume(Player player, int delta);
    void modifyBloodVolume(UUID playerId, int delta);

    BloodType getBloodType(Player player);
    BloodType getBloodType(UUID playerId);
    void setBloodType(Player player, BloodType bloodType);
    void setBloodType(UUID playerId, BloodType bloodType);

    byte getBleedingTier(Player player);
    byte getBleedingTier(UUID playerId);
    void setBleedingTier(Player player, byte tier);
    void setBleedingTier(UUID playerId, byte tier);
}

