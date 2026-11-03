package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.config.ServerConfig;

public class PlayerWeightData {
    private float cachedWeight;
    private float cachedMaxCapacity;
    private EncumbranceTier previousTier;

    public PlayerWeightData() {
        this.cachedWeight = 0.0f;
        this.cachedMaxCapacity = 500.0f;
        this.previousTier = EncumbranceTier.NORMAL;
    }

    public float getCachedWeight() {
        return cachedWeight;
    }

    public void setCachedWeight(float cachedWeight) {
        this.cachedWeight = Math.max(0.0f, cachedWeight);
    }

    public float getCachedMaxCapacity() {
        return cachedMaxCapacity > 0.0f ? cachedMaxCapacity : 500.0f;
    }

    public void setCachedMaxCapacity(float cachedMaxCapacity) {
        this.cachedMaxCapacity = Math.max(1.0f, cachedMaxCapacity);
    }

    public EncumbranceTier getPreviousTier() {
        return previousTier;
    }

    public void setPreviousTier(EncumbranceTier previousTier) {
        this.previousTier = previousTier != null ? previousTier : EncumbranceTier.NORMAL;
    }

    public float getWeightRatio() {
        float maxCapacity = getCachedMaxCapacity();
        return maxCapacity > 0.0f ? (cachedWeight / maxCapacity) : 0.0f;
    }

    public EncumbranceTier getEncumbranceTier() {
        return EncumbranceTier.fromWeightRatio(getWeightRatio());
    }

    public void reset() {
        this.cachedWeight = 0.0f;
        this.cachedMaxCapacity = 500.0f;
        this.previousTier = EncumbranceTier.NORMAL;
    }
}
