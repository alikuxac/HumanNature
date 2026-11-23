package com.alikuxac.humannature.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodVolumeHudOverlayTest {
    @Test
    void bloodRatioIsClampedToThePlayersCapacity() {
        assertEquals(0.0F, BloodVolumeHudOverlay.ratioForBloodVolume((short) 0));
        assertEquals(0.5F, BloodVolumeHudOverlay.ratioForBloodVolume((short) 2500));
        assertEquals(1.0F, BloodVolumeHudOverlay.ratioForBloodVolume((short) 5000));
        assertEquals(0.0F, BloodVolumeHudOverlay.ratioForBloodVolume((short) -10));
        assertEquals(1.0F, BloodVolumeHudOverlay.ratioForBloodVolume((short) 6000));
    }
}
