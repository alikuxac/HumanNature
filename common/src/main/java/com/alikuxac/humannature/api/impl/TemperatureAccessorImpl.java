package com.alikuxac.humannature.api.impl;

import com.alikuxac.humannature.api.temperature.ITemperatureAccessor;
import com.alikuxac.humannature.modules.temperature.PlayerTemperatureData;
import com.alikuxac.humannature.modules.temperature.TemperatureModule;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TemperatureAccessorImpl implements ITemperatureAccessor {
    private static final float BASELINE_TEMP = 37.0f;
    private final Map<UUID, Float> coreTemperatures = new ConcurrentHashMap<>();

    @Override
    public float getCoreTemperature(Player player) {
        if (player == null) return BASELINE_TEMP;
        return coreTemperatures.computeIfAbsent(player.getUUID(), id -> {
            int ticksFrozen = player.getTicksFrozen();
            if (ticksFrozen > 0) {
                return Math.max(30.0f, BASELINE_TEMP - (ticksFrozen / 140.0f) * 4.0f);
            }
            if (player.isOnFire()) {
                return Math.min(42.0f, BASELINE_TEMP + 3.0f);
            }
            return BASELINE_TEMP;
        });
    }

    @Override
    public float getCoreTemperature(UUID playerId) {
        if (playerId == null) return BASELINE_TEMP;
        return coreTemperatures.getOrDefault(playerId, BASELINE_TEMP);
    }

    @Override
    public void setCoreTemperature(Player player, float temperature) {
        if (player != null) {
            setCoreTemperature(player.getUUID(), temperature);
        }
    }

    @Override
    public void setCoreTemperature(UUID playerId, float temperature) {
        if (playerId != null) {
            coreTemperatures.put(playerId, Math.max(25.0f, Math.min(45.0f, temperature)));
        }
    }

    @Override
    public void modifyCoreTemperature(Player player, float delta) {
        if (player != null) {
            modifyCoreTemperature(player.getUUID(), delta);
        }
    }

    @Override
    public void modifyCoreTemperature(UUID playerId, float delta) {
        if (playerId != null) {
            float current = getCoreTemperature(playerId);
            setCoreTemperature(playerId, current + delta);
        }
    }

    @Override
    public float getWetnessBuffer(Player player) {
        return player != null ? getWetnessBuffer(player.getUUID()) : 0.0f;
    }

    @Override
    public float getWetnessBuffer(UUID playerId) {
        if (playerId == null) return 0.0f;
        return TemperatureModule.getOrCreateTemperatureData(playerId).getWetnessBuffer();
    }

    @Override
    public void setWetnessBuffer(Player player, float amount) {
        if (player != null) setWetnessBuffer(player.getUUID(), amount);
    }

    @Override
    public void setWetnessBuffer(UUID playerId, float amount) {
        if (playerId != null) {
            TemperatureModule.getOrCreateTemperatureData(playerId).setWetnessBuffer(amount);
        }
    }

    @Override
    public boolean isNearHeatSource(Player player) {
        return player != null && isNearHeatSource(player.getUUID());
    }

    @Override
    public boolean isNearHeatSource(UUID playerId) {
        if (playerId == null) return false;
        return TemperatureModule.getOrCreateTemperatureData(playerId).isNearHeatSource();
    }

    @Override
    public boolean isFreezing(Player player) {
        if (player == null) return false;
        PlayerTemperatureData data = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
        return player.getTicksFrozen() > 0 || data.isHypothermiaDebuffActive() || getCoreTemperature(player) < 35.0f;
    }

    @Override
    public boolean isFreezing(UUID playerId) {
        if (playerId == null) return false;
        PlayerTemperatureData data = TemperatureModule.getOrCreateTemperatureData(playerId);
        return data.isHypothermiaDebuffActive() || getCoreTemperature(playerId) < 35.0f;
    }

    @Override
    public boolean isOverheating(Player player) {
        if (player == null) return false;
        return player.isOnFire() || getCoreTemperature(player) > 39.5f;
    }

    @Override
    public boolean isOverheating(UUID playerId) {
        if (playerId == null) return false;
        return getCoreTemperature(playerId) > 39.5f;
    }
}

