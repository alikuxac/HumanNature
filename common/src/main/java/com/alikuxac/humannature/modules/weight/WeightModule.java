package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WeightModule implements IHumanModule {
    private static final Map<UUID, PlayerWeightData> WEIGHT_DATA = new ConcurrentHashMap<>();

    @Override
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return ServerConfig.ENABLE_WEIGHT.get();
    }

    public static PlayerWeightData getOrCreateWeightData(UUID playerId) {
        return WEIGHT_DATA.computeIfAbsent(playerId, k -> new PlayerWeightData());
    }

    public static PlayerWeightData getWeightData(UUID playerId) {
        return WEIGHT_DATA.get(playerId);
    }

    public static void removeWeightData(UUID playerId) {
        WEIGHT_DATA.remove(playerId);
    }

    public static float getCachedWeight(UUID playerId) {
        PlayerWeightData data = WEIGHT_DATA.get(playerId);
        return data != null ? data.getCachedWeight() : 0.0f;
    }
}
