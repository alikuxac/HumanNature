package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;

public class PlayerPhysiologyData {
    private long awakeTicks;
    private int blinkCooldown;

    // Toxin Buildup
    private float toxinBuildup;
    private ToxinType lastToxinType;
    private int ticksSinceLastExposure;
    private int pendingDuration;
    private int pendingAmplifier;

    public PlayerPhysiologyData() {
        this.awakeTicks = 0L;
        this.blinkCooldown = 0;
        this.toxinBuildup = 0.0f;
        this.lastToxinType = ToxinType.NONE;
        this.ticksSinceLastExposure = 0;
        this.pendingDuration = 0;
        this.pendingAmplifier = 0;
    }

    public PlayerPhysiologyData(long initialAwakeTicks) {
        this();
        this.awakeTicks = Math.max(0L, initialAwakeTicks);
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

    // --- Toxin Buildup Methods ---

    public float getToxinBuildup() {
        return toxinBuildup;
    }

    public void setToxinBuildup(float toxinBuildup) {
        this.toxinBuildup = Math.max(0.0f, Math.min(100.0f, toxinBuildup));
        if (this.toxinBuildup <= 0.0f) {
            this.lastToxinType = ToxinType.NONE;
            this.pendingDuration = 0;
            this.pendingAmplifier = 0;
        }
    }

    public float getToxinPercentage() {
        return this.toxinBuildup;
    }

    public ToxinType getLastToxinType() {
        return lastToxinType != null ? lastToxinType : ToxinType.NONE;
    }

    public void setLastToxinType(ToxinType lastToxinType) {
        this.lastToxinType = lastToxinType;
    }

    public int getTicksSinceLastExposure() {
        return ticksSinceLastExposure;
    }

    public void setTicksSinceLastExposure(int ticksSinceLastExposure) {
        this.ticksSinceLastExposure = Math.max(0, ticksSinceLastExposure);
    }

    public void incrementTicksSinceLastExposure() {
        if (this.ticksSinceLastExposure < Integer.MAX_VALUE) {
            this.ticksSinceLastExposure++;
        }
    }

    public int getPendingDuration() {
        return pendingDuration;
    }

    public int getPendingAmplifier() {
        return pendingAmplifier;
    }

    public void addToxinBuildup(float amount, ToxinType type, int duration, int amplifier) {
        this.toxinBuildup = Math.min(100.0f, this.toxinBuildup + Math.max(0.0f, amount));
        this.lastToxinType = type;
        this.ticksSinceLastExposure = 0;
        this.pendingDuration = Math.max(this.pendingDuration, duration);
        this.pendingAmplifier = Math.max(this.pendingAmplifier, amplifier);
    }

    public void decayToxinBuildup(float amount) {
        if (this.toxinBuildup > 0.0f) {
            this.toxinBuildup = Math.max(0.0f, this.toxinBuildup - Math.max(0.0f, amount));
            if (this.toxinBuildup <= 0.0f) {
                this.lastToxinType = ToxinType.NONE;
                this.pendingDuration = 0;
                this.pendingAmplifier = 0;
            }
        }
    }

    public void reduceToxinBuildup(float fraction) {
        if (this.toxinBuildup > 0.0f) {
            float reduction = this.toxinBuildup * Math.max(0.0f, Math.min(1.0f, fraction));
            decayToxinBuildup(reduction);
        }
    }

    public void resetToxin() {
        this.toxinBuildup = 0.0f;
        this.lastToxinType = ToxinType.NONE;
        this.ticksSinceLastExposure = 0;
        this.pendingDuration = 0;
        this.pendingAmplifier = 0;
    }
}
