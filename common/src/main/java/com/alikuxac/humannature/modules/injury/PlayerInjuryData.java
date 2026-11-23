package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.item.SplintTier;
import java.util.Arrays;

public class PlayerInjuryData {
    public static final int LIMB_COUNT = 6;
    private final byte[] limbSeverities;
    private final byte[] limbSplintTiers;
    private final int[] limbSplintDurabilities;

    public PlayerInjuryData() {
        this.limbSeverities = new byte[LIMB_COUNT];
        this.limbSplintTiers = new byte[LIMB_COUNT];
        this.limbSplintDurabilities = new int[LIMB_COUNT];
    }

    public PlayerInjuryData(byte[] limbSeverities) {
        this(limbSeverities, null, null);
    }

    public PlayerInjuryData(byte[] limbSeverities, byte[] limbSplintTiers) {
        this(limbSeverities, limbSplintTiers, null);
    }

    public PlayerInjuryData(byte[] limbSeverities, byte[] limbSplintTiers, int[] limbSplintDurabilities) {
        if (limbSeverities == null || limbSeverities.length != LIMB_COUNT) {
            this.limbSeverities = new byte[LIMB_COUNT];
            if (limbSeverities != null) {
                System.arraycopy(limbSeverities, 0, this.limbSeverities, 0, Math.min(limbSeverities.length, LIMB_COUNT));
            }
        } else {
            this.limbSeverities = Arrays.copyOf(limbSeverities, LIMB_COUNT);
        }

        if (limbSplintTiers == null || limbSplintTiers.length != LIMB_COUNT) {
            this.limbSplintTiers = new byte[LIMB_COUNT];
            if (limbSplintTiers != null) {
                System.arraycopy(limbSplintTiers, 0, this.limbSplintTiers, 0, Math.min(limbSplintTiers.length, LIMB_COUNT));
            }
        } else {
            this.limbSplintTiers = Arrays.copyOf(limbSplintTiers, LIMB_COUNT);
        }

        if (limbSplintDurabilities == null || limbSplintDurabilities.length != LIMB_COUNT) {
            this.limbSplintDurabilities = new int[LIMB_COUNT];
            if (limbSplintDurabilities != null) {
                System.arraycopy(limbSplintDurabilities, 0, this.limbSplintDurabilities, 0, Math.min(limbSplintDurabilities.length, LIMB_COUNT));
            }
        } else {
            this.limbSplintDurabilities = Arrays.copyOf(limbSplintDurabilities, LIMB_COUNT);
        }
    }

    public byte getSeverity(LimbType limb) {
        return limbSeverities[limb.getIndex()];
    }

    public void setSeverity(LimbType limb, int severity) {
        int clamped = Math.max(0, Math.min(2, severity));
        limbSeverities[limb.getIndex()] = (byte) clamped;
        if (clamped == 0) {
            removeSplint(limb);
        }
    }

    public SplintTier getSplint(LimbType limb) {
        byte tierIdx = limbSplintTiers[limb.getIndex()];
        return SplintTier.fromIndex(tierIdx);
    }

    public void setSplint(LimbType limb, SplintTier tier) {
        if (tier == null) {
            removeSplint(limb);
        } else {
            setSplint(limb, tier, tier.getMaxDurability());
        }
    }

    public void setSplint(LimbType limb, SplintTier tier, int durability) {
        int idx = limb.getIndex();
        if (tier == null) {
            limbSplintTiers[idx] = 0;
            limbSplintDurabilities[idx] = 0;
        } else {
            limbSplintTiers[idx] = (byte) tier.getIndex();
            limbSplintDurabilities[idx] = Math.max(0, durability);
        }
    }

    public int getSplintDurability(LimbType limb) {
        return limbSplintDurabilities[limb.getIndex()];
    }

    public void setSplintDurability(LimbType limb, int durability) {
        limbSplintDurabilities[limb.getIndex()] = Math.max(0, durability);
    }

    public boolean isSplinted(LimbType limb) {
        return limbSplintTiers[limb.getIndex()] > 0;
    }

    public boolean hasAnySplint() {
        for (byte t : limbSplintTiers) {
            if (t > 0) return true;
        }
        return false;
    }

    public boolean damageSplint(LimbType limb, int amount) {
        int idx = limb.getIndex();
        if (limbSplintTiers[idx] == 0) return false;
        limbSplintDurabilities[idx] -= amount;
        if (limbSplintDurabilities[idx] <= 0) {
            removeSplint(limb);
            return true;
        }
        return false;
    }

    public void removeSplint(LimbType limb) {
        int idx = limb.getIndex();
        limbSplintTiers[idx] = 0;
        limbSplintDurabilities[idx] = 0;
    }

    public byte[] getRawSplints() {
        return Arrays.copyOf(limbSplintTiers, LIMB_COUNT);
    }

    public int[] getRawSplintDurabilities() {
        return Arrays.copyOf(limbSplintDurabilities, LIMB_COUNT);
    }

    public boolean hasAnyInjury() {
        for (byte s : limbSeverities) {
            if (s > 0) return true;
        }
        return false;
    }

    public boolean hasAnySevereTrauma() {
        for (byte s : limbSeverities) {
            if (s >= 2) return true;
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
            if (limbSeverities[maxIndex] == 0) {
                limbSplintTiers[maxIndex] = 0;
                limbSplintDurabilities[maxIndex] = 0;
            }
            return true;
        }
        return false;
    }

    public void clear() {
        Arrays.fill(limbSeverities, (byte) 0);
        Arrays.fill(limbSplintTiers, (byte) 0);
        Arrays.fill(limbSplintDurabilities, 0);
    }
}
