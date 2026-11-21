package com.alikuxac.humannature.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodBagVisualsTest {
    @Test
    void fillRatioIsClampedToCapacity() {
        assertEquals(0.0F, BloodBagItem.fillRatioForStored(0));
        assertEquals(0.5F, BloodBagItem.fillRatioForStored(500));
        assertEquals(1.0F, BloodBagItem.fillRatioForStored(BloodBagItem.MAX_CAPACITY));
        assertEquals(0.0F, BloodBagItem.fillRatioForStored(-10));
        assertEquals(1.0F, BloodBagItem.fillRatioForStored(BloodBagItem.MAX_CAPACITY + 10));
    }

    @Test
    void barWidthUsesVanillaThirteenPixelRange() {
        assertEquals(0, BloodBagItem.barWidthForStored(0));
        assertEquals(7, BloodBagItem.barWidthForStored(500));
        assertEquals(13, BloodBagItem.barWidthForStored(BloodBagItem.MAX_CAPACITY));
    }
}
