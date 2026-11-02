package com.alikuxac.humannature.modules.weight;

public class PlayerWeightData {
    private float cachedWeight;

    public PlayerWeightData() {
        this.cachedWeight = 0.0f;
    }

    public float getCachedWeight() {
        return cachedWeight;
    }

    public void setCachedWeight(float cachedWeight) {
        this.cachedWeight = Math.max(0.0f, cachedWeight);
    }

    public void reset() {
        this.cachedWeight = 0.0f;
    }
}
