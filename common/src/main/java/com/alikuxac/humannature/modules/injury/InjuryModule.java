package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InjuryModule implements IHumanModule {
    private static final Map<UUID, Integer> FRACTURE_TIMERS = new HashMap<>();
    private static final Map<UUID, InjuryTier> FRACTURE_TIERS = new HashMap<>();
    private static final Map<UUID, PlayerInjuryData> INJURY_DATA = new HashMap<>();

    @Override
    public void init() {
        // Timer initialization handled on fracture application
    }

    @Override
    public boolean isEnabled() {
        return CommonConfig.ENABLE_INJURY.get();
    }

    public static PlayerInjuryData getOrCreateInjuryData(UUID playerId) {
        return INJURY_DATA.computeIfAbsent(playerId, k -> new PlayerInjuryData());
    }

    public static void setInjuryData(UUID playerId, PlayerInjuryData data) {
        INJURY_DATA.put(playerId, data);
        if (data.hasAnyInjury()) {
            if (!FRACTURE_TIMERS.containsKey(playerId)) {
                FRACTURE_TIMERS.put(playerId, CommonConfig.BONE_FRACTURE_DURATION.get());
            }
            // Update legacy tier for backwards-compat with any legacy readers
            byte leftLeg = data.getSeverity(LimbType.LEFT_LEG);
            byte rightLeg = data.getSeverity(LimbType.RIGHT_LEG);
            if (leftLeg == 2 || rightLeg == 2 || (leftLeg > 0 && rightLeg > 0)) {
                FRACTURE_TIERS.put(playerId, InjuryTier.TIER_2_DOUBLE_LEG);
            } else if (leftLeg > 0 || rightLeg > 0) {
                FRACTURE_TIERS.put(playerId, InjuryTier.TIER_1_SINGLE_LEG);
            }
        } else {
            clearFractureTimer(playerId);
        }
    }

    public static void setLimbSeverity(UUID playerId, LimbType limb, int severity) {
        PlayerInjuryData data = getOrCreateInjuryData(playerId);
        data.setSeverity(limb, severity);
        setInjuryData(playerId, data);
    }

    public static byte getLimbSeverity(UUID playerId, LimbType limb) {
        return getOrCreateInjuryData(playerId).getSeverity(limb);
    }

    public static void startFractureTimer(UUID playerId, InjuryTier tier) {
        FRACTURE_TIMERS.put(playerId, CommonConfig.BONE_FRACTURE_DURATION.get());
        FRACTURE_TIERS.put(playerId, tier);
        PlayerInjuryData data = getOrCreateInjuryData(playerId);
        if (tier == InjuryTier.TIER_2_DOUBLE_LEG) {
            data.setSeverity(LimbType.LEFT_LEG, 2);
            data.setSeverity(LimbType.RIGHT_LEG, 2);
        } else if (tier == InjuryTier.TIER_1_SINGLE_LEG) {
            data.setSeverity(LimbType.LEFT_LEG, 1);
        }
    }

    public static void setFractureTier(UUID playerId, InjuryTier tier) {
        if (tier == InjuryTier.NONE) {
            clearFractureTimer(playerId);
        } else {
            FRACTURE_TIERS.put(playerId, tier);
            if (!FRACTURE_TIMERS.containsKey(playerId)) {
                FRACTURE_TIMERS.put(playerId, CommonConfig.BONE_FRACTURE_DURATION.get());
            }
            PlayerInjuryData data = getOrCreateInjuryData(playerId);
            if (tier == InjuryTier.TIER_2_DOUBLE_LEG) {
                data.setSeverity(LimbType.LEFT_LEG, 2);
                data.setSeverity(LimbType.RIGHT_LEG, 2);
            } else if (tier == InjuryTier.TIER_1_SINGLE_LEG) {
                data.setSeverity(LimbType.LEFT_LEG, 1);
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
        return FRACTURE_TIMERS.containsKey(playerId) && FRACTURE_TIMERS.get(playerId) > 0 &&
                (getOrCreateInjuryData(playerId).hasAnyInjury() || getFractureTier(playerId) != InjuryTier.NONE);
    }

    public static void clearFractureTimer(UUID playerId) {
        FRACTURE_TIMERS.remove(playerId);
        FRACTURE_TIERS.remove(playerId);
        PlayerInjuryData data = INJURY_DATA.get(playerId);
        if (data != null) {
            data.clear();
        }
    }
}
