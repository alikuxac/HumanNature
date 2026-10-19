package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;

public class PlayerPhysiologyData {
    private long awakeTicks;
    private int blinkCooldown;

    private float toxinBuildup;
    private ToxinType lastToxinType;
    private int ticksSinceLastExposure;
    private int pendingDuration;
    private int pendingAmplifier;

    private float exertionScore;
    private int continuousMiningTicks;
    private int ticksSinceLastMining;
    private long lastMiningGameTime;
    private int restTicks;
    private boolean muscleFailure;
    private int consecutiveJumps;
    private int ticksSinceLastJump;
    private long lastJumpGameTime;

    private int continuousSwimmingTicks;
    private boolean swimmingCramped;
    private int crampRestTicks;

    public PlayerPhysiologyData() {
        this.awakeTicks = 0L;
        this.blinkCooldown = 0;
        this.toxinBuildup = 0.0f;
        this.lastToxinType = ToxinType.NONE;
        this.ticksSinceLastExposure = 0;
        this.pendingDuration = 0;
        this.pendingAmplifier = 0;

        this.exertionScore = 0.0f;
        this.continuousMiningTicks = 0;
        this.ticksSinceLastMining = 0;
        this.lastMiningGameTime = -1L;
        this.lastJumpGameTime = -1L;
        this.restTicks = 0;
        this.muscleFailure = false;
        this.consecutiveJumps = 0;
        this.ticksSinceLastJump = 0;

        this.continuousSwimmingTicks = 0;
        this.swimmingCramped = false;
        this.crampRestTicks = 0;
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

    public void reset() {
        resetFatigue();
        resetToxin();
        resetExertion();
        resetSwimmingCramps();
    }

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

    public float getExertionScore() {
        return exertionScore;
    }

    public void setExertionScore(float exertionScore) {
        this.exertionScore = Math.max(0.0f, Math.min(100.0f, exertionScore));
        if (this.exertionScore >= 100.0f) {
            this.muscleFailure = true;
        } else if (this.exertionScore <= 0.0f) {
            this.muscleFailure = false;
        }
    }

    public void addExertion(float amount) {
        setExertionScore(this.exertionScore + Math.max(0.0f, amount));
    }

    public void decayExertion(float amount) {
        setExertionScore(this.exertionScore - Math.max(0.0f, amount));
    }

    public boolean isMuscleFailure() {
        return muscleFailure;
    }

    public void setMuscleFailure(boolean muscleFailure) {
        this.muscleFailure = muscleFailure;
        if (muscleFailure) {
            this.exertionScore = 100.0f;
        }
    }

    public int getContinuousMiningTicks() {
        return continuousMiningTicks;
    }

    public void setContinuousMiningTicks(int continuousMiningTicks) {
        this.continuousMiningTicks = Math.max(0, continuousMiningTicks);
    }

    public void incrementContinuousMiningTicks() {
        if (this.continuousMiningTicks < Integer.MAX_VALUE) {
            this.continuousMiningTicks++;
        }
    }

    public void resetContinuousMiningTicks() {
        this.continuousMiningTicks = 0;
    }

    public int getTicksSinceLastMining() {
        return ticksSinceLastMining;
    }

    public void setTicksSinceLastMining(int ticksSinceLastMining) {
        this.ticksSinceLastMining = Math.max(0, ticksSinceLastMining);
    }

    public void incrementTicksSinceLastMining() {
        if (this.ticksSinceLastMining < Integer.MAX_VALUE) {
            this.ticksSinceLastMining++;
        }
    }

    public long getLastMiningGameTime() {
        return lastMiningGameTime;
    }

    public void setLastMiningGameTime(long lastMiningGameTime) {
        this.lastMiningGameTime = lastMiningGameTime;
    }

    public int getRestTicks() {
        return restTicks;
    }

    public void setRestTicks(int restTicks) {
        this.restTicks = Math.max(0, restTicks);
    }

    public void incrementRestTicks() {
        if (this.restTicks < Integer.MAX_VALUE) {
            this.restTicks++;
        }
    }

    public void resetRestTicks() {
        this.restTicks = 0;
    }

    public int getConsecutiveJumps() {
        return consecutiveJumps;
    }

    public void setConsecutiveJumps(int consecutiveJumps) {
        this.consecutiveJumps = Math.max(0, consecutiveJumps);
    }

    public void incrementConsecutiveJumps() {
        if (this.consecutiveJumps < Integer.MAX_VALUE) {
            this.consecutiveJumps++;
        }
    }

    public void resetConsecutiveJumps() {
        this.consecutiveJumps = 0;
    }

    public int getTicksSinceLastJump() {
        return ticksSinceLastJump;
    }

    public void setTicksSinceLastJump(int ticksSinceLastJump) {
        this.ticksSinceLastJump = Math.max(0, ticksSinceLastJump);
    }

    public void incrementTicksSinceLastJump() {
        if (this.ticksSinceLastJump < Integer.MAX_VALUE) {
            this.ticksSinceLastJump++;
        }
    }

    public long getLastJumpGameTime() {
        return lastJumpGameTime;
    }

    public void setLastJumpGameTime(long lastJumpGameTime) {
        this.lastJumpGameTime = lastJumpGameTime;
    }

    public void resetExertion() {
        this.exertionScore = 0.0f;
        this.continuousMiningTicks = 0;
        this.ticksSinceLastMining = 0;
        this.lastMiningGameTime = -1L;
        this.lastJumpGameTime = -1L;
        this.restTicks = 0;
        this.muscleFailure = false;
        this.consecutiveJumps = 0;
        this.ticksSinceLastJump = 0;
    }

    public int getContinuousSwimmingTicks() {
        return continuousSwimmingTicks;
    }

    public void setContinuousSwimmingTicks(int continuousSwimmingTicks) {
        this.continuousSwimmingTicks = Math.max(0, continuousSwimmingTicks);
    }

    public void incrementContinuousSwimmingTicks() {
        if (this.continuousSwimmingTicks < Integer.MAX_VALUE) {
            this.continuousSwimmingTicks++;
        }
    }

    public void resetContinuousSwimmingTicks() {
        this.continuousSwimmingTicks = 0;
    }

    public boolean isSwimmingCramped() {
        return swimmingCramped;
    }

    public void setSwimmingCramped(boolean swimmingCramped) {
        this.swimmingCramped = swimmingCramped;
    }

    public int getCrampRestTicks() {
        return crampRestTicks;
    }

    public void setCrampRestTicks(int crampRestTicks) {
        this.crampRestTicks = Math.max(0, crampRestTicks);
    }

    public void incrementCrampRestTicks() {
        if (this.crampRestTicks < Integer.MAX_VALUE) {
            this.crampRestTicks++;
        }
    }

    public void resetCrampRestTicks() {
        this.crampRestTicks = 0;
    }

    public void resetSwimmingCramps() {
        this.continuousSwimmingTicks = 0;
        this.swimmingCramped = false;
        this.crampRestTicks = 0;
    }
}
