package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.client.WeightTooltipHandler;
import com.alikuxac.humannature.config.ServerConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeightRecursiveContainerTest {

    @BeforeAll
    static void setup() {
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
        } catch (Throwable ignored) {
        }
    }

    @Test
    void testServerConfigContainerDefaults() {
        assertEquals(1.0, ServerConfig.WEIGHT_CONTAINER_RATIO, 0.001);
        assertEquals(0.20, ServerConfig.LOAD_BEARING_CONTAINER_REDUCTION, 0.001);
    }

    @Test
    void testFormatWeight() {
        assertEquals("0.0", WeightTooltipHandler.formatWeight(0.0f));
        assertEquals("0.0", WeightTooltipHandler.formatWeight(-1.5f));
        assertEquals("0.05", WeightTooltipHandler.formatWeight(0.05f));
        assertEquals("4.5", WeightTooltipHandler.formatWeight(4.5f));
        assertEquals("10.0", WeightTooltipHandler.formatWeight(10.04f));
    }

    @Test
    void testEncumbranceRatioTiers() {
        assertEquals(EncumbranceTier.NORMAL, EncumbranceTier.fromWeightRatio(0.40f));
        assertFalse(EncumbranceTier.fromWeightRatio(0.40f).isEncumbered());

        assertEquals(EncumbranceTier.LIGHT, EncumbranceTier.fromWeightRatio(0.68f));
        assertTrue(EncumbranceTier.fromWeightRatio(0.68f).isEncumbered());

        assertEquals(EncumbranceTier.MEDIUM, EncumbranceTier.fromWeightRatio(0.75f));
        assertTrue(EncumbranceTier.fromWeightRatio(0.75f).isEncumbered());

        assertEquals(EncumbranceTier.OVERBURDENED, EncumbranceTier.fromWeightRatio(0.92f));
        assertTrue(EncumbranceTier.fromWeightRatio(0.92f).isEncumbered());
    }

    @Test
    void testEmptyStackWeights() {
        assertEquals(0.0f, WeightCalculator.getContentsWeight(null));
        assertEquals(0.0f, WeightCalculator.calculateStackWeight(null));
    }
}

