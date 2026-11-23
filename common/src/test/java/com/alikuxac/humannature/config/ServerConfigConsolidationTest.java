package com.alikuxac.humannature.config;

import com.alikuxac.humannature.modules.temperature.TemperatureEventHandler;
import com.alikuxac.humannature.modules.weight.EncumbranceTier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigConsolidationTest {

    @AfterEach
    void tearDown() {
        ServerConfig.TEMPERATURE_CHANGE_RATE_MULTIPLIER = () -> 1.0;
        ServerConfig.WEIGHT_PENALTY_MULTIPLIER = () -> 1.0;
        ServerConfig.WEIGHT_MAX_CAPACITY = () -> 500.0;
    }

    @Test
    void testCore19ConfigDefaults() {
        assertTrue(ServerConfig.ENABLE_INJURY.get());
        assertTrue(ServerConfig.ENABLE_TEMPERATURE.get());
        assertTrue(ServerConfig.ENABLE_WEIGHT.get());
        assertTrue(ServerConfig.ENABLE_PHYSIOLOGY.get());

        assertTrue(ServerConfig.ENABLE_SPLINT.get());
        assertEquals(6000, ServerConfig.BONE_FRACTURE_DURATION.get());
        assertTrue(ServerConfig.ENABLE_TOTEM_BYPASS.get());
        assertTrue(ServerConfig.ENABLE_GFORCE_BLACKOUT.get());
        assertTrue(ServerConfig.ENABLE_KINETIC_CRASH.get());

        assertEquals(1.0, ServerConfig.TEMPERATURE_CHANGE_RATE_MULTIPLIER.get(), 0.001);
        assertEquals(0.60, ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get(), 0.001);
        assertEquals(39.5, ServerConfig.HEAT_STRESS_OVERHEAT_THRESHOLD.get(), 0.001);
        assertEquals(42.0, ServerConfig.HEAT_STRESS_DAMAGE_THRESHOLD.get(), 0.001);

        assertEquals(500.0, ServerConfig.WEIGHT_MAX_CAPACITY.get(), 0.001);
        assertEquals(1.0, ServerConfig.WEIGHT_PENALTY_MULTIPLIER.get(), 0.001);

        assertTrue(ServerConfig.ENABLE_SLEEP_DEPRIVATION.get());
        assertEquals(72000, ServerConfig.SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS.get());
        assertTrue(ServerConfig.ENABLE_TOXIN_BUILDUP.get());
        assertTrue(ServerConfig.ENABLE_PHYSICAL_EXERTION.get());
    }

    @Test
    void testWeightPenaltyMultiplierScaling() {
        assertEquals(1.0, ServerConfig.WEIGHT_PENALTY_MULTIPLIER.get(), 0.001);
        double baseLightSpeed = EncumbranceTier.LIGHT.getSpeedPenalty();
        assertEquals(0.10, baseLightSpeed, 0.001);

        ServerConfig.WEIGHT_PENALTY_MULTIPLIER = () -> 2.0;
        assertEquals(0.20, EncumbranceTier.LIGHT.getSpeedPenalty(), 0.001);
        assertEquals(0.50, EncumbranceTier.MEDIUM.getSpeedPenalty(), 0.001);
        assertEquals(1.00, EncumbranceTier.OVERBURDENED.getSpeedPenalty(), 0.001);

        assertEquals(0.030, EncumbranceTier.LIGHT.getSinkPull(), 0.001);
        assertEquals(0.070, EncumbranceTier.MEDIUM.getSinkPull(), 0.001);
        assertEquals(0.130, EncumbranceTier.OVERBURDENED.getSinkPull(), 0.001);

        assertEquals(1.50, EncumbranceTier.LIGHT.getFallDamageMultiplier(), 0.001);
        assertEquals(3.00, EncumbranceTier.OVERBURDENED.getFallDamageMultiplier(), 0.001);

        ServerConfig.WEIGHT_PENALTY_MULTIPLIER = () -> 0.0;
        assertEquals(0.0, EncumbranceTier.LIGHT.getSpeedPenalty(), 0.001);
        assertEquals(0.0, EncumbranceTier.LIGHT.getSinkPull(), 0.001);
        assertEquals(1.0, EncumbranceTier.LIGHT.getFallDamageMultiplier(), 0.001);
    }

    @Test
    void testInsulatingArmorTagKeyRegistration() {
        assertNotNull(TemperatureEventHandler.INSULATING_ARMOR);
        assertEquals("humannature:insulating_armor", TemperatureEventHandler.INSULATING_ARMOR.location().toString());
    }

    @Test
    void testPublicStaticFinalConstants() {
        assertEquals(0.10, ServerConfig.SPLINT_SPEED_REDUCTION.get(), 0.001);
        assertEquals(1, ServerConfig.SPLINT_DURABILITY_PER_50_STEPS.get());
        assertEquals(0.5, ServerConfig.BONE_FRACTURE_CROUCH_MULTIPLIER.get(), 0.001);

        assertEquals(0.0025, ServerConfig.WETNESS_RAIN_ACCUMULATION_RATE.get(), 0.0001);
        assertEquals(0.015, ServerConfig.WETNESS_WATER_ACCUMULATION_RATE.get(), 0.0001);
        assertEquals(12.5, ServerConfig.HEAT_DRYING_TIME_SECONDS.get(), 0.01);
        assertEquals(5.0, ServerConfig.HEAT_THAW_MULTIPLIER.get(), 0.01);

        assertEquals(0.8, ServerConfig.WEIGHT_HEAVY_BLOCK_BASE.get(), 0.001);
        assertEquals(0.4, ServerConfig.WEIGHT_METALLIC_ITEM_BASE.get(), 0.001);
        assertEquals(0.05, ServerConfig.WEIGHT_DEFAULT_ITEM_BASE.get(), 0.001);
        assertEquals(0.005, ServerConfig.WEIGHT_LIGHT_ITEM_BASE.get(), 0.001);

        assertEquals(25.0, ServerConfig.TOXIN_BASE_BUILDUP_PER_HIT.get(), 0.001);
        assertEquals(80, ServerConfig.TOXIN_DECAY_DELAY_TICKS.get());
        assertEquals(2.0, ServerConfig.TOXIN_DECAY_RATE_PER_SECOND.get(), 0.001);
        assertEquals(200, ServerConfig.EXERTION_CONTINUOUS_MINING_TICKS.get());
        assertEquals(70, ServerConfig.EXERTION_REST_RECOVERY_TICKS.get());
    }
}

