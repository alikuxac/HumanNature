package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.core.IHumanModule;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhysiologyModule implements IHumanModule {
    private static final Map<UUID, PlayerPhysiologyData> PHYSIOLOGY_DATA = new ConcurrentHashMap<>();
    private static final Set<UUID> ERUPTING_PLAYERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get();
    }

    public static boolean isSleepDeprivationEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_SLEEP_DEPRIVATION.get();
    }

    public static boolean isToxinBuildupEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_TOXIN_BUILDUP.get();
    }

    public static boolean isExertionEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_PHYSICAL_EXERTION.get();
    }

    public static boolean isSwimmingCrampsEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_SWIMMING_CRAMPS.get();
    }

    public static boolean isDigestionEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_DIGESTION_WINDOW.get();
    }

    public static boolean isOvereatingEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_OVEREATING_REFLEX.get();
    }

    public static boolean isIndigestionEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_INDIGESTION.get();
    }

    public static boolean isCalciumHealingEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_CALCIUM_HEALING.get();
    }

    public static boolean isClaustrophobiaEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_CLAUSTROPHOBIA_PANIC.get();
    }

    public static boolean isAdrenalineEnabled() {
        return ServerConfig.ENABLE_PHYSIOLOGY.get() && ServerConfig.ENABLE_ADRENALINE_SURGE.get();
    }

    public static boolean isErupting(UUID playerId) {
        return ERUPTING_PLAYERS.contains(playerId);
    }

    public static void setErupting(UUID playerId, boolean erupting) {
        if (erupting) {
            ERUPTING_PLAYERS.add(playerId);
        } else {
            ERUPTING_PLAYERS.remove(playerId);
        }
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

    public static float getToxinBuildup(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getToxinBuildup() : 0.0f;
    }

    public static float getToxinPercentage(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getToxinPercentage() : 0.0f;
    }

    public static ToxinType getLastToxinType(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getLastToxinType() : ToxinType.NONE;
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

    public static void resetToxin(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        if (data != null) {
            data.resetToxin();
        }
    }

    public static float getExertionScore(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getExertionScore() : 0.0f;
    }

    public static boolean isMuscleFailure(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null && data.isMuscleFailure();
    }

    public static boolean isSwimmingCramped(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null && data.isSwimmingCramped();
    }

    public static void resetExertion(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        if (data != null) {
            data.resetExertion();
        }
    }

    public static void resetSwimmingCramps(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        if (data != null) {
            data.resetSwimmingCramps();
        }
    }

    public static void reset(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        if (data != null) {
            data.reset();
        }
        ERUPTING_PLAYERS.remove(playerId);
    }

    public static float calculateToxinMultiplier(UUID playerId) {
        PlayerPhysiologyData data = getOrCreatePhysiologyData(playerId);
        float fatigueScore = (isSleepDeprivationEnabled() && ServerConfig.ENABLE_TOXIN_FATIGUE_SCALING.get())
                ? data.getFatigueScore()
                : 0.0f;
        boolean isFractured = ServerConfig.ENABLE_INJURY.get() && InjuryModule.isFractureActive(playerId);
        double fractureMultiplier = isFractured
                ? (ServerConfig.TOXIN_FRACTURE_BUILDUP_MULTIPLIER != null ? ServerConfig.TOXIN_FRACTURE_BUILDUP_MULTIPLIER.get() : 1.5)
                : 1.0;
        return (float) ((1.0f + fatigueScore) * fractureMultiplier);
    }

    public static float getPendingNutrition(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getPendingNutrition() : 0.0f;
    }

    public static float getPendingSaturation(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getPendingSaturation() : 0.0f;
    }

    public static float getIndigestionBuildup(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null ? data.getIndigestionBuildup() : 0.0f;
    }

    public static boolean hasCalciumBuff(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null && data.hasCalciumBuff();
    }

    public static boolean isPanicking(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null && data.isPanicking();
    }

    public static boolean isAdrenalineActive(UUID playerId) {
        PlayerPhysiologyData data = PHYSIOLOGY_DATA.get(playerId);
        return data != null && data.isAdrenalineActive();
    }

    public static void clearData(UUID playerId) {
        PHYSIOLOGY_DATA.remove(playerId);
        ERUPTING_PLAYERS.remove(playerId);
    }
}
