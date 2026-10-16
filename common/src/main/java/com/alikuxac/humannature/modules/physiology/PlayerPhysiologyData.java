package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;

public class PlayerPhysiologyData {
    private long awakeTicks;
    private int blinkCooldown;

    public PlayerPhysiologyData() {
        this.awakeTicks = 0L;
        this.blinkCooldown = 0;
    }

    public PlayerPhysiologyData(long initialAwakeTicks) {
        this.awakeTicks = Math.max(0L, initialAwakeTicks);
        this.blinkCooldown = 0;
    }

    public long getAwakeTicks() {
        return awakeTicks;
    }

    public void setAwakeTicks(long awakeTicks) {
        this.awakeTicks = Math.max(0L, awakeTicks);
    }

    public void incrementAwakeTicks() {
        if (this.awakeTicks < Long.MAX_VALUE) {
            this.awakeTicks++;
        }
    }

    public float getFatigueScore() {
        int fullTicks = ServerConfig.SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS != null ?
                ServerConfig.SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS.get() : 72000;
        return calculateFatigueScore(fullTicks);
    }

    public float calculateFatigueScore(int fullFatigueTicks) {
        if (fullFatigueTicks <= 0) return 1.0f;
        float score = (float) awakeTicks / (float) fullFatigueTicks;
        return Math.max(0.0f, Math.min(1.0f, score));
    }

    public float getFatiguePercentage() {
        return getFatigueScore() * 100.0f;
    }

    public boolean isBlinkEligible() {
        double threshold = ServerConfig.SLEEP_FATIGUE_BLINK_THRESHOLD != null ?
                ServerConfig.SLEEP_FATIGUE_BLINK_THRESHOLD.get() : 0.80;
        return getFatigueScore() >= (float) threshold;
    }

    public int getBlinkCooldown() {
        return blinkCooldown;
    }

    public void setBlinkCooldown(int blinkCooldown) {
        this.blinkCooldown = Math.max(0, blinkCooldown);
    }

    public void decrementBlinkCooldown() {
        if (this.blinkCooldown > 0) {
            this.blinkCooldown--;
        }
    }

    public void resetFatigue() {
        this.awakeTicks = 0L;
        this.blinkCooldown = 0;
    }
}
