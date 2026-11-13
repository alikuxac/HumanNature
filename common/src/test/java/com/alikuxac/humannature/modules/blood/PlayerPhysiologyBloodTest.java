package com.alikuxac.humannature.modules.blood;

import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerPhysiologyBloodTest {

    @Test
    void testInitialDefaults() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();
        assertEquals(5000, data.getBloodVolume());
        assertNull(data.getBloodType());
        assertEquals(0, data.getBleedingTier());
        assertFalse(data.isBleeding());
    }

    @Test
    void testVolumeClamping() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();

        data.setBloodVolume((short) 6000);
        assertEquals(5000, data.getBloodVolume());

        data.setBloodVolume((short) -100);
        assertEquals(0, data.getBloodVolume());

        data.setBloodVolume((short) 4200);
        assertEquals(4200, data.getBloodVolume());

        data.modifyBloodVolume(-500);
        assertEquals(3700, data.getBloodVolume());

        data.modifyBloodVolume(2000);
        assertEquals(5000, data.getBloodVolume());
    }

    @Test
    void testBleedingTiers() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();

        data.setBleedingTier((byte) 1);
        assertEquals(1, data.getBleedingTier());
        assertTrue(data.isBleeding());

        data.setBleedingTier((byte) 3);
        assertEquals(3, data.getBleedingTier());
        assertTrue(data.isBleeding());

        // Clamped at 3
        data.setBleedingTier((byte) 5);
        assertEquals(3, data.getBleedingTier());

        data.setBleedingTier((byte) -1);
        assertEquals(0, data.getBleedingTier());
        assertFalse(data.isBleeding());
    }

    @Test
    void testDeltaThresholdAndMarkSynced() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();
        data.setBloodType(BloodType.O_POSITIVE);
        data.markBloodSynced();

        assertEquals(5000, data.getLastSyncedBloodVolume());
        assertEquals(0, data.getLastSyncedBleedingTier());
        assertEquals(BloodType.O_POSITIVE, data.getLastSyncedBloodType());

        // Delta under threshold 25 ml: should not exceed
        data.modifyBloodVolume(-10);
        assertFalse(data.isBloodDeltaThresholdExceeded(25));

        // Delta >= 25 ml: threshold exceeded
        data.modifyBloodVolume(-20); // total delta 30 ml
        assertTrue(data.isBloodDeltaThresholdExceeded(25));

        data.markBloodSynced();
        assertFalse(data.isBloodDeltaThresholdExceeded(25));

        // Bleeding tier change immediately triggers threshold
        data.setBleedingTier((byte) 1);
        assertTrue(data.isBloodDeltaThresholdExceeded(25));

        data.markBloodSynced();
        assertFalse(data.isBloodDeltaThresholdExceeded(25));

        // BloodType change immediately triggers threshold
        data.setBloodType(BloodType.A_NEGATIVE);
        assertTrue(data.isBloodDeltaThresholdExceeded(25));
    }

    @Test
    void testResetBlood() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();
        data.setBloodType(BloodType.B_POSITIVE);
        data.setBloodVolume((short) 2500);
        data.setBleedingTier((byte) 2);

        // Reset without resetting blood type (e.g. respawn)
        data.resetBlood(false);
        assertEquals(5000, data.getBloodVolume());
        assertEquals(0, data.getBleedingTier());
        assertEquals(BloodType.B_POSITIVE, data.getBloodType());

        // Reset with blood type reset
        data.resetBlood(true);
        assertNull(data.getBloodType());
    }
}

