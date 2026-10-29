package com.alikuxac.humannature.modules.temperature;

public class PlayerTemperatureData {
    private float wetnessBuffer;
    private boolean nearHeatSource;
    private int heatScanCooldown;
    private int steamSoundCooldown;
    private int stumbleCooldown;
    private int internalWarmingTicks;
    private boolean hypothermiaDebuffActive;

    public PlayerTemperatureData() {
        this.wetnessBuffer = 0.0f;
        this.nearHeatSource = false;
        this.heatScanCooldown = 0;
        this.steamSoundCooldown = 0;
        this.stumbleCooldown = 0;
        this.internalWarmingTicks = 0;
        this.hypothermiaDebuffActive = false;
    }

    public float getWetnessBuffer() {
        return wetnessBuffer;
    }

    public void setWetnessBuffer(float wetnessBuffer) {
        this.wetnessBuffer = Math.max(0.0f, Math.min(1.0f, wetnessBuffer));
    }

    public void addWetness(float amount) {
        this.wetnessBuffer = Math.min(1.0f, this.wetnessBuffer + Math.max(0.0f, amount));
    }

    public void reduceWetness(float amount) {
        this.wetnessBuffer = Math.max(0.0f, this.wetnessBuffer - Math.max(0.0f, amount));
    }

    public boolean isNearHeatSource() {
        return nearHeatSource;
    }

    public void setNearHeatSource(boolean nearHeatSource) {
        this.nearHeatSource = nearHeatSource;
    }

    public int getHeatScanCooldown() {
        return heatScanCooldown;
    }

    public void setHeatScanCooldown(int heatScanCooldown) {
        this.heatScanCooldown = heatScanCooldown;
    }

    public void decrementHeatScanCooldown() {
        if (this.heatScanCooldown > 0) {
            this.heatScanCooldown--;
        }
    }

    public int getSteamSoundCooldown() {
        return steamSoundCooldown;
    }

    public void setSteamSoundCooldown(int steamSoundCooldown) {
        this.steamSoundCooldown = steamSoundCooldown;
    }

    public void decrementSteamSoundCooldown() {
        if (this.steamSoundCooldown > 0) {
            this.steamSoundCooldown--;
        }
    }

    public int getStumbleCooldown() {
        return stumbleCooldown;
    }

    public void setStumbleCooldown(int stumbleCooldown) {
        this.stumbleCooldown = stumbleCooldown;
    }

    public void decrementStumbleCooldown() {
        if (this.stumbleCooldown > 0) {
            this.stumbleCooldown--;
        }
    }

    public int getInternalWarmingTicks() {
        return internalWarmingTicks;
    }

    public void setInternalWarmingTicks(int internalWarmingTicks) {
        this.internalWarmingTicks = Math.max(0, internalWarmingTicks);
    }

    public boolean hasInternalWarming() {
        return internalWarmingTicks > 0;
    }

    public void decrementInternalWarmingTicks() {
        if (this.internalWarmingTicks > 0) {
            this.internalWarmingTicks--;
        }
    }

    public boolean isHypothermiaDebuffActive() {
        return hypothermiaDebuffActive;
    }

    public void setHypothermiaDebuffActive(boolean active) {
        this.hypothermiaDebuffActive = active;
    }

    public void reset() {
        this.wetnessBuffer = 0.0f;
        this.nearHeatSource = false;
        this.heatScanCooldown = 0;
        this.steamSoundCooldown = 0;
        this.stumbleCooldown = 0;
        this.internalWarmingTicks = 0;
        this.hypothermiaDebuffActive = false;
    }
}
