package com.alikuxac.humannature.item;

public enum SplintTier {
    WOODEN(1, 1.0F, 0.0F, 0, 0),
    REINFORCED(4, 0.5F, 0.0F, 30 * 20, 0),
    GOLDEN(8, 0.33F, 2.0F, 5 * 20, 120 * 20),
    DIAMOND(16, 0.0F, 0.0F, 20 * 20, 60 * 20),
    NETHERITE(24, 0.0F, 0.0F, 45 * 20, 0);

    private final int maxDurability;
    private final float healDurationMultiplier;
    private final float absorptionAmount;
    private final int primaryBuffDuration;
    private final int secondaryBuffDuration;

    SplintTier(int maxDurability, float healDurationMultiplier, float absorptionAmount, int primaryBuffDuration, int secondaryBuffDuration) {
        this.maxDurability = maxDurability;
        this.healDurationMultiplier = healDurationMultiplier;
        this.absorptionAmount = absorptionAmount;
        this.primaryBuffDuration = primaryBuffDuration;
        this.secondaryBuffDuration = secondaryBuffDuration;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public float getHealDurationMultiplier() {
        return healDurationMultiplier;
    }

    public float getAbsorptionAmount() {
        return absorptionAmount;
    }

    public int getPrimaryBuffDuration() {
        return primaryBuffDuration;
    }

    public int getSecondaryBuffDuration() {
        return secondaryBuffDuration;
    }
}
