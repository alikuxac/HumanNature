package com.alikuxac.humannature.modules.injury;

public enum InjuryTier {
    NONE(0, 0.0, 0.0, false, false),
    TIER_1_SINGLE_LEG(1, -0.45, 0.0, false, false),
    TIER_2_DOUBLE_LEG(2, -0.85, -1.0, true, true);

    private final int tierLevel;
    private final double speedModifier;
    private final double jumpModifier;
    private final boolean lockJump;
    private final boolean forceCrawl;

    InjuryTier(int tierLevel, double speedModifier, double jumpModifier, boolean lockJump, boolean forceCrawl) {
        this.tierLevel = tierLevel;
        this.speedModifier = speedModifier;
        this.jumpModifier = jumpModifier;
        this.lockJump = lockJump;
        this.forceCrawl = forceCrawl;
    }

    public int getTierLevel() {
        return tierLevel;
    }

    public double getSpeedModifier() {
        return speedModifier;
    }

    public double getJumpModifier() {
        return jumpModifier;
    }

    public boolean isLockJump() {
        return lockJump;
    }

    public boolean isForceCrawl() {
        return forceCrawl;
    }
}
