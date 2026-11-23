package com.alikuxac.humannature.item;

public enum SplintTier {
    WOODEN(1, 1.0F, 0.0F, 0, 0, 0.15F, 0.40F, 1.0F, 20, "Wooden"),
    REINFORCED(4, 0.5F, 0.0F, 30 * 20, 0, 0.02F, 0.05F, 1.25F, 100, "Reinforced"),
    GOLDEN(8, 0.33F, 2.0F, 5 * 20, 120 * 20, 0.01F, 0.02F, 1.50F, 150, "Golden"),
    DIAMOND(16, 0.0F, 0.0F, 20 * 20, 60 * 20, 0.0F, 0.0F, 1.75F, 400, "Diamond"),
    NETHERITE(24, 0.0F, 0.0F, 45 * 20, 0, 0.0F, 0.0F, 2.0F, 0, "Netherite");

    private final int maxDurability;
    private final float healDurationMultiplier;
    private final float absorptionAmount;
    private final int primaryBuffDuration;
    private final int secondaryBuffDuration;
    private final float sprintDislodgeChance;
    private final float lightFallDislodgeChance;
    private final float naturalHealingMultiplier;
    private final int sprintDurabilityDrainInterval;
    private final String displayName;

    SplintTier(int maxDurability, float healDurationMultiplier, float absorptionAmount, int primaryBuffDuration, int secondaryBuffDuration,
               float sprintDislodgeChance, float lightFallDislodgeChance, float naturalHealingMultiplier, int sprintDurabilityDrainInterval, String displayName) {
        this.maxDurability = maxDurability;
        this.healDurationMultiplier = healDurationMultiplier;
        this.absorptionAmount = absorptionAmount;
        this.primaryBuffDuration = primaryBuffDuration;
        this.secondaryBuffDuration = secondaryBuffDuration;
        this.sprintDislodgeChance = sprintDislodgeChance;
        this.lightFallDislodgeChance = lightFallDislodgeChance;
        this.naturalHealingMultiplier = naturalHealingMultiplier;
        this.sprintDurabilityDrainInterval = sprintDurabilityDrainInterval;
        this.displayName = displayName;
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

    public float getSprintDislodgeChance() {
        return sprintDislodgeChance;
    }

    public float getLightFallDislodgeChance() {
        return lightFallDislodgeChance;
    }

    public float getNaturalHealingMultiplier() {
        return naturalHealingMultiplier;
    }

    public int getSprintDurabilityDrainInterval() {
        return sprintDurabilityDrainInterval;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTranslationKey() {
        return "humannature.splint_tier." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public int getIndex() {
        return ordinal() + 1;
    }

    public static SplintTier fromIndex(int index) {
        if (index < 1 || index > values().length) {
            return null;
        }
        return values()[index - 1];
    }
}
