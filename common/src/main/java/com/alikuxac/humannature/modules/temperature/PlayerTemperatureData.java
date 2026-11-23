package com.alikuxac.humannature.modules.temperature;

public class PlayerTemperatureData {
    private float wetnessBuffer;
    private boolean nearHeatSource;
    private int heatScanCooldown;
    private int steamSoundCooldown;
    private int stumbleCooldown;
    private int internalWarmingTicks;
    private boolean hypothermiaDebuffActive;
    private float coreTemperature;
    private int heatExhaustionSoundCooldown;
    private int heatDamageCooldown;

    public PlayerTemperatureData() {
        this.wetnessBuffer = 0.0f;
        this.nearHeatSource = false;
        this.heatScanCooldown = 0;
        this.steamSoundCooldown = 0;
        this.stumbleCooldown = 0;
        this.internalWarmingTicks = 0;
        this.hypothermiaDebuffActive = false;
        this.coreTemperature = 37.0f;
        this.heatExhaustionSoundCooldown = 0;
        this.heatDamageCooldown = 0;
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

    public float getCoreTemperature() {
        return coreTemperature;
    }

    public void setCoreTemperature(float coreTemperature) {
        this.coreTemperature = Math.max(25.0f, Math.min(45.0f, coreTemperature));
    }

    public void modifyCoreTemperature(float delta) {
        setCoreTemperature(this.coreTemperature + delta);
    }

    public int getHeatExhaustionSoundCooldown() {
        return heatExhaustionSoundCooldown;
    }

    public void setHeatExhaustionSoundCooldown(int heatExhaustionSoundCooldown) {
        this.heatExhaustionSoundCooldown = heatExhaustionSoundCooldown;
    }

    public void decrementHeatExhaustionSoundCooldown() {
        if (this.heatExhaustionSoundCooldown > 0) {
            this.heatExhaustionSoundCooldown--;
        }
    }

    public int getHeatDamageCooldown() {
        return heatDamageCooldown;
    }

    public void setHeatDamageCooldown(int heatDamageCooldown) {
        this.heatDamageCooldown = heatDamageCooldown;
    }

    public void decrementHeatDamageCooldown() {
        if (this.heatDamageCooldown > 0) {
            this.heatDamageCooldown--;
        }
    }

    public void reset() {
        this.wetnessBuffer = 0.0f;
        this.nearHeatSource = false;
        this.heatScanCooldown = 0;
        this.steamSoundCooldown = 0;
        this.stumbleCooldown = 0;
        this.internalWarmingTicks = 0;
        this.hypothermiaDebuffActive = false;
        this.coreTemperature = 37.0f;
        this.heatExhaustionSoundCooldown = 0;
        this.heatDamageCooldown = 0;
    }
}
