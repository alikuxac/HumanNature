package com.alikuxac.humannature.advancement;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HumanNatureTriggerTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void eventIdIsRequired() {
        assertTrue(HumanNatureTrigger.TriggerInstance.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString("{}" )).error().isPresent());
    }

    @Test
    void roundTripPreservesEventAndPlayerPredicate() {
        var input = JsonParser.parseString("{\"event_id\":\"splint_applied\",\"player\":[]}");
        var instance = HumanNatureTrigger.TriggerInstance.CODEC.parse(JsonOps.INSTANCE, input).getOrThrow();
        assertTrue(instance.player().isPresent());
        assertTrue(instance.matches("splint_applied"));
        assertFalse(instance.matches("fracture_healed"));
        assertFalse(instance.matches("SPLINT_APPLIED"));
        assertEquals(input, HumanNatureTrigger.TriggerInstance.CODEC.encodeStart(JsonOps.INSTANCE, instance).getOrThrow());
    }

    @Test
    void customDataPackEventsAreSupportedWithoutAPlayerPredicate() {
        var instance = HumanNatureTrigger.TriggerInstance.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"event_id\":\"addon_milestone\"}")).getOrThrow();
        assertTrue(instance.player().isEmpty());
        assertTrue(instance.matches("addon_milestone"));
        assertFalse(instance.matches(null));
    }
}
