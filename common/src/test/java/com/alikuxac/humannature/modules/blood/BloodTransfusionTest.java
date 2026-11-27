package com.alikuxac.humannature.modules.blood;

import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.item.BloodBagItem;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.modules.physiology.ToxinType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BloodTransfusionTest {

    @Test
    void testUniversalDonorCompatibility() {
        BloodType universalDonor = BloodType.O_NEGATIVE;
        for (BloodType recipient : BloodType.values()) {
            assertTrue(recipient.canReceiveFrom(universalDonor), "Recipient " + recipient + " should accept O-");
        }
    }

    @Test
    void testUniversalRecipientCompatibility() {
        BloodType universalRecipient = BloodType.AB_POSITIVE;
        for (BloodType donor : BloodType.values()) {
            assertTrue(universalRecipient.canReceiveFrom(donor), "AB+ should accept donor " + donor);
        }
    }

    @Test
    void testIncompatiblePairs() {
        assertFalse(BloodType.B_POSITIVE.canReceiveFrom(BloodType.A_POSITIVE));
        assertFalse(BloodType.B_NEGATIVE.canReceiveFrom(BloodType.A_NEGATIVE));

        assertFalse(BloodType.O_NEGATIVE.canReceiveFrom(BloodType.O_POSITIVE));
        assertFalse(BloodType.A_NEGATIVE.canReceiveFrom(BloodType.A_POSITIVE));
        assertFalse(BloodType.B_NEGATIVE.canReceiveFrom(BloodType.B_POSITIVE));
        assertFalse(BloodType.AB_NEGATIVE.canReceiveFrom(BloodType.AB_POSITIVE));
    }

    @Test
    void testSimulatedTransfusionSuccess() {
        PlayerPhysiologyData recipient = new PlayerPhysiologyData();
        recipient.setBloodType(BloodType.A_POSITIVE);
        recipient.setBloodVolume((short) 3000);

        BloodType donorType = BloodType.O_POSITIVE;
        short transferVol = 1000;

        assertTrue(recipient.getBloodType().canReceiveFrom(donorType));
        recipient.modifyBloodVolume(transferVol);

        assertEquals(4000, recipient.getBloodVolume());
        assertEquals(0.0f, recipient.getToxinBuildup(), 0.001f);
    }

    @Test
    void testSimulatedTransfusionVolumeCapping() {
        PlayerPhysiologyData recipient = new PlayerPhysiologyData();
        recipient.setBloodType(BloodType.AB_POSITIVE);
        recipient.setBloodVolume((short) 4500);

        BloodType donorType = BloodType.B_POSITIVE;
        short transferVol = 1000;

        assertTrue(recipient.getBloodType().canReceiveFrom(donorType));
        recipient.modifyBloodVolume(transferVol);

        assertEquals(PlayerPhysiologyData.MAX_BLOOD_VOLUME, recipient.getBloodVolume());
    }

    @Test
    void testSimulatedTransfusionMismatchShock() {
        PlayerPhysiologyData recipient = new PlayerPhysiologyData();
        recipient.setBloodType(BloodType.O_NEGATIVE);
        recipient.setBloodVolume((short) 3500);
        recipient.setToxinBuildup(0.0f);

        BloodType donorType = BloodType.A_POSITIVE;

        assertFalse(recipient.getBloodType().canReceiveFrom(donorType));

        recipient.setToxinBuildup(100.0f);
        recipient.setLastToxinType(ToxinType.POISON);

        assertEquals(100.0f, recipient.getToxinBuildup(), 0.001f);
        assertEquals(ToxinType.POISON, recipient.getLastToxinType());
    }

    @Test
    void testHemostasisTreatmentLogic() {
        PlayerPhysiologyData patient = new PlayerPhysiologyData();

        patient.setBleedingTier((byte) 1);
        assertTrue(patient.isBleeding());
        assertTrue(patient.getClottingTicks() > 0);

        patient.setBleedingTier((byte) 0);
        patient.setClottingTicks(0);

        assertEquals(0, patient.getBleedingTier());
        assertFalse(patient.isBleeding());
        assertEquals(0, patient.getClottingTicks());

        patient.setBleedingTier((byte) 2);
        assertTrue(patient.isBleeding());

        patient.setBleedingTier((byte) 0);
        patient.setClottingTicks(0);

        assertEquals(0, patient.getBleedingTier());
        assertFalse(patient.isBleeding());
    }

    @Test
    void testCulinaryBloodRegenLogic() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();
        data.setBloodVolume((short) 4000);

        data.modifyBloodVolume(500);
        assertEquals(4500, data.getBloodVolume());

        data.setBloodRegenBoostTicks(1200);
        assertEquals(1200, data.getBloodRegenBoostTicks());

        int baseRegen = 1;
        int mult = 3;
        data.modifyBloodVolume(baseRegen * mult);
        assertEquals(4503, data.getBloodVolume());
    }

    @Test
    void testBloodBagDataStateAndBloodTypeLocking() {
        BloodBagItem.BloodBagData empty = BloodBagItem.BloodBagData.EMPTY;
        assertTrue(empty.isEmpty());
        assertFalse(empty.isFull());
        assertTrue(empty.canAccept(BloodType.O_NEGATIVE));
        assertTrue(empty.canAccept(BloodType.A_POSITIVE));

        BloodBagItem.BloodBagData partial = new BloodBagItem.BloodBagData(500, BloodType.A_POSITIVE);
        assertFalse(partial.isEmpty());
        assertFalse(partial.isFull());
        assertEquals(500, partial.storedMl());
        assertEquals(BloodType.A_POSITIVE, partial.bloodType());

        assertTrue(partial.canAccept(BloodType.A_POSITIVE));
        assertFalse(partial.canAccept(BloodType.B_POSITIVE));
        assertFalse(partial.canAccept(BloodType.O_NEGATIVE));

        BloodBagItem.BloodBagData full = new BloodBagItem.BloodBagData(1000, BloodType.A_POSITIVE);
        assertFalse(full.isEmpty());
        assertTrue(full.isFull());
        assertFalse(full.canAccept(BloodType.A_POSITIVE));
    }

    @Test
    void testExtractionSafetyThresholdBoundary() {
        short unsafeBoundary = 3500;
        short safeBoundary = 3501;

        assertFalse(unsafeBoundary > BloodBagItem.SAFE_EXTRACTION_THRESHOLD);
        assertTrue(safeBoundary > BloodBagItem.SAFE_EXTRACTION_THRESHOLD);
    }
}

