package com.alikuxac.humannature.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodScreenEffectTest {

    @Test
    void intensityRampsBetween3000And2000Ml() {
        assertEquals(0.0f, BloodScreenEffectHandler.intensityFor((short) 5000), 0.0001f);
        assertEquals(0.0f, BloodScreenEffectHandler.intensityFor((short) 3000), 0.0001f);
        assertEquals(0.5f, BloodScreenEffectHandler.intensityFor((short) 2500), 0.0001f);
        assertEquals(1.0f, BloodScreenEffectHandler.intensityFor((short) 2000), 0.0001f);
        assertEquals(1.0f, BloodScreenEffectHandler.intensityFor((short) 0), 0.0001f);
    }
}
