package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InjuryModule implements IHumanModule {
    private static final Map<UUID, Integer> FRACTURE_TIMERS = new HashMap<>();
    private static final Map<UUID, InjuryTier> FRACTURE_TIERS = new HashMap<>();

    @Override
    public void init() {
        // Timer initialization handled on fracture application
    }

    @Override
    public boolean isEnabled() {
        return CommonConfig.ENABLE_INJURY.get();
    }

    public static void startFractureTimer(UUID playerId, InjuryTier tier) {
        FRACTURE_TIMERS.put(playerId, CommonConfig.BONE_FRACTURE_DURATION.get());
        FRACTURE_TIERS.put(playerId, tier);
    }

    public static void setFractureTier(UUID playerId, InjuryTier tier) {
        if (tier == InjuryTier.NONE) {
            clearFractureTimer(playerId);
        } else {
            FRACTURE_TIERS.put(playerId, tier);
            if (!FRACTURE_TIMERS.containsKey(playerId)) {
                FRACTURE_TIMERS.put(playerId, CommonConfig.BONE_FRACTURE_DURATION.get());
            }
        }
    }

    public static InjuryTier getFractureTier(UUID playerId) {
        return FRACTURE_TIERS.getOrDefault(playerId, InjuryTier.NONE);
    }

    public static void reduceFractureTimer(UUID playerId, int amount) {
        if (FRACTURE_TIMERS.containsKey(playerId)) {
            int newTime = FRACTURE_TIMERS.get(playerId) - amount;
            if (newTime <= 0) {
                clearFractureTimer(playerId);
            } else {
                FRACTURE_TIMERS.put(playerId, newTime);
            }
        }
    }

    public static int getRemainingFractureTime(UUID playerId) {
        if (!FRACTURE_TIMERS.containsKey(playerId)) return 0;
        return FRACTURE_TIMERS.get(playerId);
    }

    public static boolean isFractureActive(UUID playerId) {
        return FRACTURE_TIMERS.containsKey(playerId) && FRACTURE_TIMERS.get(playerId) > 0 && getFractureTier(playerId) != InjuryTier.NONE;
    }

    public static void clearFractureTimer(UUID playerId) {
        FRACTURE_TIMERS.remove(playerId);
        FRACTURE_TIERS.remove(playerId);
    }
}
