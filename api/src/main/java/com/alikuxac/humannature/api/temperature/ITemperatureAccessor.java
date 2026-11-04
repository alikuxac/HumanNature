package com.alikuxac.humannature.api.temperature;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface ITemperatureAccessor {
    float getCoreTemperature(Player player);
    float getCoreTemperature(UUID playerId);

    void setCoreTemperature(Player player, float temperature);
    void setCoreTemperature(UUID playerId, float temperature);

    void modifyCoreTemperature(Player player, float delta);
    void modifyCoreTemperature(UUID playerId, float delta);

    float getWetnessBuffer(Player player);
    float getWetnessBuffer(UUID playerId);

    void setWetnessBuffer(Player player, float amount);
    void setWetnessBuffer(UUID playerId, float amount);

    boolean isNearHeatSource(Player player);
    boolean isNearHeatSource(UUID playerId);

    boolean isFreezing(Player player);
    boolean isFreezing(UUID playerId);

    boolean isOverheating(Player player);
    boolean isOverheating(UUID playerId);
}

