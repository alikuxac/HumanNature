package com.alikuxac.humannature.modules.injury;

import java.util.Arrays;

public class PlayerInjuryData {
    public static final int LIMB_COUNT = 6;
    private final byte[] limbSeverities;

    public PlayerInjuryData() {
        this.limbSeverities = new byte[LIMB_COUNT];
    }

    public PlayerInjuryData(byte[] limbSeverities) {
        if (limbSeverities == null || limbSeverities.length != LIMB_COUNT) {
            this.limbSeverities = new byte[LIMB_COUNT];
            if (limbSeverities != null) {
                System.arraycopy(limbSeverities, 0, this.limbSeverities, 0, Math.min(limbSeverities.length, LIMB_COUNT));
            }
        } else {
            this.limbSeverities = Arrays.copyOf(limbSeverities, LIMB_COUNT);
        }
    }

    public byte getSeverity(LimbType limb) {
        return limbSeverities[limb.getIndex()];
    }

    public void setSeverity(LimbType limb, int severity) {
        limbSeverities[limb.getIndex()] = (byte) Math.max(0, Math.min(2, severity));
    }

    public boolean hasAnyInjury() {
        for (byte s : limbSeverities) {
            if (s > 0) return true;
        }
        return false;
    }

    public int getTotalInjuries() {
        int count = 0;
        for (byte s : limbSeverities) {
            if (s > 0) count++;
        }
        return count;
    }

    public byte[] getRawData() {
        return Arrays.copyOf(limbSeverities, LIMB_COUNT);
    }

    public boolean downgradeWorstInjury() {
        int maxIndex = -1;
        byte maxSev = 0;
        for (int i = 0; i < LIMB_COUNT; i++) {
            if (limbSeverities[i] > maxSev) {
                maxSev = limbSeverities[i];
                maxIndex = i;
            }
        }
        if (maxIndex != -1 && maxSev > 0) {
            limbSeverities[maxIndex]--;
            return true;
        }
        return false;
    }

    public void clear() {
        Arrays.fill(limbSeverities, (byte) 0);
    }
}
