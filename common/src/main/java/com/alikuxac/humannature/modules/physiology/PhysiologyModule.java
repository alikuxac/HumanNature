package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhysiologyModule implements IHumanModule {
    private static final Map<UUID, PlayerPhysiologyData> PHYSIOLOGY_DATA = new ConcurrentHashMap<>();

    @Override
    public void init() {
        // Module initialization
    }

    @Override
    public boolean isEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get();
    }

    public static boolean isSleepDeprivationEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_SLEEP_DEPRIVATION.get();
    }

    public static PlayerPhysiologyData getOrCreatePhysiologyData(UUID playerId) {
        return PHYSIOLOGY_DATA.computeIfAbsent(playerId, k -> new PlayerPhysiologyData());
    }

    public static float getFatigueScore(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getFatigueScore() : 0.0f;
    }

    public static float getFatiguePercentage(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getFatiguePercentage() : 0.0f;
    }

    public static long getAwakeTicks(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getAwakeTicks() : 0L;
    }

    public static void setAwakeTicks(UUID playerId, long ticks) {
        getOrCreatePhysiologyData(playerId).setAwakeTicks(ticks);
    }

    public static void resetSleepDeprivation(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        if (data != null) {
            data.resetFatigue();
        }
    }

    public static void clearData(UUID playerId) {
        PHYSIOLOGY_DATA.remove(playerId);
    }
}

