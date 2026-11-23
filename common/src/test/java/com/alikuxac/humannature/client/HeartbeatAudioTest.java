package com.alikuxac.humannature.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartbeatAudioTest {

    @Test
    void heartbeatAcceleratesAsBloodVolumeFalls() {
        int mild = HeartbeatAudioHandler.intervalTicks((short) 3900, 75);
        int moderate = HeartbeatAudioHandler.intervalTicks((short) 2500, 85);
        int severe = HeartbeatAudioHandler.intervalTicks((short) 2000, 95);

        assertTrue(mild > moderate, "mild should be slower than moderate");
        assertTrue(moderate > severe, "moderate should be slower than severe");
        assertTrue(severe >= 6, "interval must stay above the minimum");
    }
}
