package com.alikuxac.humannature.modules.temperature;

import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TemperatureModule implements IHumanModule {
    private static final Map<UUID, PlayerTemperatureData> TEMPERATURE_DATA = new ConcurrentHashMap<>();

    @Override
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return ServerConfig.ENABLE_TEMPERATURE.get();
    }

    public static boolean isModuleEnabled() {
        return ServerConfig.ENABLE_TEMPERATURE.get();
    }

    public static boolean isWetnessAccumulationEnabled() {
        return ServerConfig.ENABLE_TEMPERATURE.get() && ServerConfig.ENABLE_WETNESS_ACCUMULATION.get();
    }

    public static boolean isHeatRewarmingEnabled() {
        return ServerConfig.ENABLE_TEMPERATURE.get() && ServerConfig.ENABLE_HEAT_REWARMING.get();
    }

    public static boolean isHypothermiaEnabled() {
        return ServerConfig.ENABLE_TEMPERATURE.get() && ServerConfig.ENABLE_HYPOTHERMIA.get();
    }

    public static PlayerTemperatureData getOrCreateTemperatureData(UUID playerId) {
        return TEMPERATURE_DATA.computeIfAbsent(playerId, k -> new PlayerTemperatureData());
    }

    public static PlayerTemperatureData getTemperatureData(UUID playerId) {
        return TEMPERATURE_DATA.get(playerId);
    }

    public static void removeTemperatureData(UUID playerId) {
        TEMPERATURE_DATA.remove(playerId);
    }

    public static void clearAll() {
        TEMPERATURE_DATA.clear();
    }
}
