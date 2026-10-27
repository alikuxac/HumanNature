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

    private float pendingNutrition;
    private float pendingSaturation;
    private int digestionTickCounter;
    private int recentEatsWhenFull;
    private long lastFullEatGameTime;
    private float indigestionBuildup;
    private int indigestionTickCounter;
    private int calciumAbsorptionTicks;

    private int claustrophobiaTicks;
    private boolean panicking;
    private int panicSoundCooldown;

    private int adrenalineTicks;
    private boolean adrenalineActive;
    private int adrenalineCooldownTicks;

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

        this.pendingNutrition = 0.0f;
        this.pendingSaturation = 0.0f;
        this.digestionTickCounter = 0;
        this.recentEatsWhenFull = 0;
        this.lastFullEatGameTime = -1L;
        this.indigestionBuildup = 0.0f;
        this.indigestionTickCounter = 0;
        this.calciumAbsorptionTicks = 0;

        this.claustrophobiaTicks = 0;
        this.panicking = false;
        this.panicSoundCooldown = 0;

        this.adrenalineTicks = 0;
        this.adrenalineActive = false;
        this.adrenalineCooldownTicks = 0;
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
        resetDigestion();
        resetIndigestion();
        resetCalcium();
        resetPanic();
        resetAdrenaline();
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

    // --- AIO-529: Digestion & Gut Reflex ---

    public float getPendingNutrition() {
        return pendingNutrition;
    }

    public void setPendingNutrition(float pendingNutrition) {
        this.pendingNutrition = Math.max(0.0f, pendingNutrition);
    }

    public float getPendingSaturation() {
        return pendingSaturation;
    }

    public void setPendingSaturation(float pendingSaturation) {
        this.pendingSaturation = Math.max(0.0f, pendingSaturation);
    }

    public void addDigestionBuffer(float nutrition, float saturation) {
        this.pendingNutrition += Math.max(0.0f, nutrition);
        this.pendingSaturation += Math.max(0.0f, saturation);
    }

    public void consumeDigestionStep(float nutritionAmount, float saturationAmount) {
        this.pendingNutrition = Math.max(0.0f, this.pendingNutrition - nutritionAmount);
        this.pendingSaturation = Math.max(0.0f, this.pendingSaturation - saturationAmount);
    }

    public int getDigestionTickCounter() {
        return digestionTickCounter;
    }

    public void setDigestionTickCounter(int digestionTickCounter) {
        this.digestionTickCounter = Math.max(0, digestionTickCounter);
    }

    public void incrementDigestionTickCounter() {
        if (this.digestionTickCounter < Integer.MAX_VALUE) {
            this.digestionTickCounter++;
        }
    }

    public void resetDigestionTickCounter() {
        this.digestionTickCounter = 0;
    }

    public int getRecentEatsWhenFull() {
        return recentEatsWhenFull;
    }

    public void setRecentEatsWhenFull(int recentEatsWhenFull) {
        this.recentEatsWhenFull = Math.max(0, recentEatsWhenFull);
    }

    public void incrementRecentEatsWhenFull() {
        if (this.recentEatsWhenFull < Integer.MAX_VALUE) {
            this.recentEatsWhenFull++;
        }
    }

    public void resetRecentEatsWhenFull() {
        this.recentEatsWhenFull = 0;
    }

    public long getLastFullEatGameTime() {
        return lastFullEatGameTime;
    }

    public void setLastFullEatGameTime(long lastFullEatGameTime) {
        this.lastFullEatGameTime = lastFullEatGameTime;
    }

    public void resetDigestion() {
        this.pendingNutrition = 0.0f;
        this.pendingSaturation = 0.0f;
        this.digestionTickCounter = 0;
        this.recentEatsWhenFull = 0;
        this.lastFullEatGameTime = -1L;
    }

    public float getIndigestionBuildup() {
        return indigestionBuildup;
    }

    public void setIndigestionBuildup(float indigestionBuildup) {
        this.indigestionBuildup = Math.max(0.0f, Math.min(100.0f, indigestionBuildup));
    }

    public void addIndigestion(float amount) {
        setIndigestionBuildup(this.indigestionBuildup + Math.max(0.0f, amount));
    }

    public void decayIndigestion(float amount) {
        setIndigestionBuildup(this.indigestionBuildup - Math.max(0.0f, amount));
    }

    public void resetIndigestion() {
        this.indigestionBuildup = 0.0f;
        this.indigestionTickCounter = 0;
    }

    public int getIndigestionTickCounter() {
        return indigestionTickCounter;
    }

    public void setIndigestionTickCounter(int indigestionTickCounter) {
        this.indigestionTickCounter = Math.max(0, indigestionTickCounter);
    }

    public void incrementIndigestionTickCounter() {
        if (this.indigestionTickCounter < Integer.MAX_VALUE) {
            this.indigestionTickCounter++;
        }
    }

    public void resetIndigestionTickCounter() {
        this.indigestionTickCounter = 0;
    }

    public int getCalciumAbsorptionTicks() {
        return calciumAbsorptionTicks;
    }

    public void setCalciumAbsorptionTicks(int calciumAbsorptionTicks) {
        this.calciumAbsorptionTicks = Math.max(0, calciumAbsorptionTicks);
    }

    public void decrementCalciumAbsorptionTicks() {
        if (this.calciumAbsorptionTicks > 0) {
            this.calciumAbsorptionTicks--;
        }
    }

    public boolean hasCalciumBuff() {
        return this.calciumAbsorptionTicks > 0;
    }

    public void resetCalcium() {
        this.calciumAbsorptionTicks = 0;
    }

    // --- AIO-530: Claustrophobia Panic & Sensory Cues ---

    public int getClaustrophobiaTicks() {
        return claustrophobiaTicks;
    }

    public void setClaustrophobiaTicks(int claustrophobiaTicks) {
        this.claustrophobiaTicks = Math.max(0, claustrophobiaTicks);
    }

    public void incrementClaustrophobiaTicks() {
        if (this.claustrophobiaTicks < Integer.MAX_VALUE) {
            this.claustrophobiaTicks++;
        }
    }

    public void resetClaustrophobiaTicks() {
        this.claustrophobiaTicks = 0;
    }

    public boolean isPanicking() {
        return panicking;
    }

    public void setPanicking(boolean panicking) {
        this.panicking = panicking;
    }

    public int getPanicSoundCooldown() {
        return panicSoundCooldown;
    }

    public void setPanicSoundCooldown(int panicSoundCooldown) {
        this.panicSoundCooldown = Math.max(0, panicSoundCooldown);
    }

    public void decrementPanicSoundCooldown() {
        if (this.panicSoundCooldown > 0) {
            this.panicSoundCooldown--;
        }
    }

    public void resetPanic() {
        this.claustrophobiaTicks = 0;
        this.panicking = false;
        this.panicSoundCooldown = 0;
    }

    // --- AIO-531: Adrenaline Surge & Pain Shock ---

    public int getAdrenalineTicks() {
        return adrenalineTicks;
    }

    public void setAdrenalineTicks(int adrenalineTicks) {
        this.adrenalineTicks = Math.max(0, adrenalineTicks);
    }

    public void decrementAdrenalineTicks() {
        if (this.adrenalineTicks > 0) {
            this.adrenalineTicks--;
        }
    }

    public boolean isAdrenalineActive() {
        return adrenalineActive;
    }

    public void setAdrenalineActive(boolean adrenalineActive) {
        this.adrenalineActive = adrenalineActive;
    }

    public int getAdrenalineCooldownTicks() {
        return adrenalineCooldownTicks;
    }

    public void setAdrenalineCooldownTicks(int adrenalineCooldownTicks) {
        this.adrenalineCooldownTicks = Math.max(0, adrenalineCooldownTicks);
    }

    public void decrementAdrenalineCooldownTicks() {
        if (this.adrenalineCooldownTicks > 0) {
            this.adrenalineCooldownTicks--;
        }
    }

    public void triggerAdrenaline(int durationTicks) {
        this.adrenalineTicks = Math.max(0, durationTicks);
        this.adrenalineActive = true;
    }

    public void resetAdrenaline() {
        this.adrenalineTicks = 0;
        this.adrenalineActive = false;
        this.adrenalineCooldownTicks = 0;
    }
}
