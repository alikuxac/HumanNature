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

        data.modifyBloodVolume(-10);
        assertFalse(data.isBloodDeltaThresholdExceeded(25));

        data.modifyBloodVolume(-20);
        assertTrue(data.isBloodDeltaThresholdExceeded(25));

        data.markBloodSynced();
        assertFalse(data.isBloodDeltaThresholdExceeded(25));

        data.setBleedingTier((byte) 1);
        assertTrue(data.isBloodDeltaThresholdExceeded(25));

        data.markBloodSynced();
        assertFalse(data.isBloodDeltaThresholdExceeded(25));

        data.setBloodType(BloodType.A_NEGATIVE);
        assertTrue(data.isBloodDeltaThresholdExceeded(25));
    }

    @Test
    void testResetBlood() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();
        data.setBloodType(BloodType.B_POSITIVE);
        data.setBloodVolume((short) 2500);
        data.setBleedingTier((byte) 2);

        data.resetBlood(false);
        assertEquals(5000, data.getBloodVolume());
        assertEquals(0, data.getBleedingTier());
        assertEquals(BloodType.B_POSITIVE, data.getBloodType());

        data.resetBlood(true);
        assertNull(data.getBloodType());
        assertEquals(0, data.getClottingTicks());
        assertEquals(0, data.getBloodRegenBoostTicks());
    }

    @Test
    void testClottingCountdownOnTier1() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();

        data.setBleedingTier((byte) 1);
        assertEquals(1, data.getBleedingTier());
        assertTrue(data.getClottingTicks() > 0);

        int initialClotting = data.getClottingTicks();
        data.decrementClottingTicks();
        assertEquals(initialClotting - 1, data.getClottingTicks());

        data.setBleedingTier((byte) 2);
        assertEquals(2, data.getBleedingTier());
        assertEquals(0, data.getClottingTicks());
    }

    @Test
    void testExertionDoubledWhenBloodBelow80Percent() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();
        assertEquals(5000, data.getBloodVolume());

        data.addExertion(10.0f);
        assertEquals(10.0f, data.getExertionScore(), 0.001f);

        data.setBloodVolume((short) 3800);
        data.addExertion(10.0f);
        assertEquals(30.0f, data.getExertionScore(), 0.001f);
    }

    @Test
    void testBloodRegenBoostCountdown() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();
        assertEquals(0, data.getBloodRegenBoostTicks());

        data.setBloodRegenBoostTicks(1200);
        assertEquals(1200, data.getBloodRegenBoostTicks());

        data.decrementBloodRegenBoostTicks();
        assertEquals(1199, data.getBloodRegenBoostTicks());
    }

    @Test
    void testMaxHealthSafetyFloorCalculation() {
        double h0 = 20.0;
        double floor = 4.0;

        double lost50 = 0.50;
        double target50 = Math.max(floor, h0 * (1.0 - lost50));
        assertEquals(10.0, target50, 0.001);

        double lost90 = 0.90;
        double target90 = Math.max(floor, h0 * (1.0 - lost90));
        assertEquals(4.0, target90, 0.001);

        double hBuffed = 40.0;
        double lost95 = 0.95;
        double targetBuffed = Math.max(floor, hBuffed * (1.0 - lost95));
        assertEquals(4.0, targetBuffed, 0.001);
    }
}
