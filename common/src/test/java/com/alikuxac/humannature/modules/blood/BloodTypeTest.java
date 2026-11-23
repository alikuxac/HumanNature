package com.alikuxac.humannature.modules.blood;

import com.alikuxac.humannature.api.blood.BloodType;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BloodTypeTest {

    @Test
    void testAllEightTypesExistWithCorrectIds() {
        BloodType[] all = BloodType.values();
        assertEquals(8, all.length);

        assertEquals(0, BloodType.O_NEGATIVE.getId());
        assertEquals("O-", BloodType.O_NEGATIVE.getDisplayName());

        assertEquals(1, BloodType.O_POSITIVE.getId());
        assertEquals("O+", BloodType.O_POSITIVE.getDisplayName());

        assertEquals(2, BloodType.A_NEGATIVE.getId());
        assertEquals("A-", BloodType.A_NEGATIVE.getDisplayName());

        assertEquals(3, BloodType.A_POSITIVE.getId());
        assertEquals("A+", BloodType.A_POSITIVE.getDisplayName());

        assertEquals(4, BloodType.B_NEGATIVE.getId());
        assertEquals("B-", BloodType.B_NEGATIVE.getDisplayName());

        assertEquals(5, BloodType.B_POSITIVE.getId());
        assertEquals("B+", BloodType.B_POSITIVE.getDisplayName());

        assertEquals(6, BloodType.AB_NEGATIVE.getId());
        assertEquals("AB-", BloodType.AB_NEGATIVE.getDisplayName());

        assertEquals(7, BloodType.AB_POSITIVE.getId());
        assertEquals("AB+", BloodType.AB_POSITIVE.getDisplayName());
    }

    @Test
    void testFromIdAndFromName() {
        for (BloodType type : BloodType.values()) {
            assertSame(type, BloodType.fromId(type.getId()));
            assertSame(type, BloodType.fromName(type.name()));
            assertSame(type, BloodType.fromName(type.getDisplayName()));
        }

        // Default fallbacks for invalid inputs
        assertSame(BloodType.O_POSITIVE, BloodType.fromId((byte) 99));
        assertSame(BloodType.O_POSITIVE, BloodType.fromName(null));
        assertSame(BloodType.O_POSITIVE, BloodType.fromName("UNKNOWN"));
    }

    @Test
    void testUniversalDonorAndRecipient() {
        BloodType universalDonor = BloodType.O_NEGATIVE;
        BloodType universalRecipient = BloodType.AB_POSITIVE;

        for (BloodType recipient : BloodType.values()) {
            assertTrue(universalDonor.canDonateTo(recipient), "O- should be able to donate to " + recipient);
            assertTrue(recipient.canReceiveFrom(universalDonor), recipient + " should be able to receive from O-");
        }

        for (BloodType donor : BloodType.values()) {
            assertTrue(universalRecipient.canReceiveFrom(donor), "AB+ should be able to receive from " + donor);
            assertTrue(donor.canDonateTo(universalRecipient), donor + " should be able to donate to AB+");
        }

        // O- cannot receive from anyone except O-
        for (BloodType donor : BloodType.values()) {
            if (donor == BloodType.O_NEGATIVE) {
                assertTrue(universalDonor.canReceiveFrom(donor));
            } else {
                assertFalse(universalDonor.canReceiveFrom(donor), "O- must not receive from " + donor);
            }
        }
    }

    @Test
    void testSpecificTypeCompatibilities() {
        // A+ can receive from A+, A-, O+, O-
        assertTrue(BloodType.A_POSITIVE.canReceiveFrom(BloodType.A_POSITIVE));
        assertTrue(BloodType.A_POSITIVE.canReceiveFrom(BloodType.A_NEGATIVE));
        assertTrue(BloodType.A_POSITIVE.canReceiveFrom(BloodType.O_POSITIVE));
        assertTrue(BloodType.A_POSITIVE.canReceiveFrom(BloodType.O_NEGATIVE));
        assertFalse(BloodType.A_POSITIVE.canReceiveFrom(BloodType.B_POSITIVE));
        assertFalse(BloodType.A_POSITIVE.canReceiveFrom(BloodType.AB_POSITIVE));

        // B- can receive from B-, O- only
        assertTrue(BloodType.B_NEGATIVE.canReceiveFrom(BloodType.B_NEGATIVE));
        assertTrue(BloodType.B_NEGATIVE.canReceiveFrom(BloodType.O_NEGATIVE));
        assertFalse(BloodType.B_NEGATIVE.canReceiveFrom(BloodType.B_POSITIVE));
        assertFalse(BloodType.B_NEGATIVE.canReceiveFrom(BloodType.O_POSITIVE));
    }

    @Test
    void testRandomGeneration() {
        Random random = new Random(42);
        for (int i = 0; i < 50; i++) {
            BloodType randomType = BloodType.getRandom(random);
            assertNotNull(randomType);
        }
    }
}

