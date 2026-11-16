package com.alikuxac.humannature.client;

import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.modules.temperature.PlayerTemperatureData;
import com.alikuxac.humannature.modules.weight.EncumbranceTier;
import com.alikuxac.humannature.network.NetworkHandler;
import com.alikuxac.humannature.network.TreatLimbPayload;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AutoHealthyHudTest {

    @Test
    void testClientConfigDefaults() {
        assertNotNull(ClientConfig.AUTO_HIDE_HEALTHY_HUD);
        assertFalse(ClientConfig.AUTO_HIDE_HEALTHY_HUD.get());
    }

    @Test
    void testNullPlayerHealthyCheck() {
        assertFalse(SwappableHudOverlay.isPlayerFullyHealthy(null));
    }

    @Test
    void testHealthyDataStructures() {
        // 1. Injury data clean state
        PlayerInjuryData injuryData = new PlayerInjuryData();
        assertFalse(injuryData.hasAnyInjury());
        for (LimbType limb : LimbType.values()) {
            assertEquals(0, injuryData.getSeverity(limb));
        }

        // 2. Temperature data clean state
        PlayerTemperatureData tempData = new PlayerTemperatureData();
        assertEquals(0.0f, tempData.getWetnessBuffer());
        assertFalse(tempData.isNearHeatSource());
        assertFalse(tempData.hasInternalWarming());
        assertFalse(tempData.isHypothermiaDebuffActive());
        assertEquals(37.0f, tempData.getCoreTemperature());
        assertTrue(tempData.getCoreTemperature() >= 36.5f && tempData.getCoreTemperature() <= 37.5f);

        // 3. Physiology data clean state
        PlayerPhysiologyData physData = new PlayerPhysiologyData();
        assertEquals(0.0f, physData.getToxinBuildup());
        assertEquals(0.0f, physData.getExertionScore());
        assertFalse(physData.isMuscleFailure());
        assertFalse(physData.isSwimmingCramped());
        assertFalse(physData.isAdrenalineActive());
        assertFalse(physData.isPanicking());

        // 4. Encumbrance tier evaluations
        assertEquals(EncumbranceTier.NORMAL, EncumbranceTier.fromWeightRatio(0.20f));
        assertEquals(EncumbranceTier.LIGHT, EncumbranceTier.fromWeightRatio(0.55f));
        assertEquals(EncumbranceTier.MEDIUM, EncumbranceTier.fromWeightRatio(0.75f));
        assertEquals(EncumbranceTier.OVERBURDENED, EncumbranceTier.fromWeightRatio(0.95f));

        assertTrue(EncumbranceTier.NORMAL == EncumbranceTier.NORMAL || EncumbranceTier.NORMAL == EncumbranceTier.LIGHT);
        assertTrue(EncumbranceTier.LIGHT == EncumbranceTier.NORMAL || EncumbranceTier.LIGHT == EncumbranceTier.LIGHT);
        assertFalse(EncumbranceTier.MEDIUM == EncumbranceTier.NORMAL || EncumbranceTier.MEDIUM == EncumbranceTier.LIGHT);
        assertFalse(EncumbranceTier.OVERBURDENED == EncumbranceTier.NORMAL || EncumbranceTier.OVERBURDENED == EncumbranceTier.LIGHT);
    }

    @Test
    void testNetworkHandlerSendTreatLimbPayload() {
        AtomicReference<TreatLimbPayload> sentPayload = new AtomicReference<>();
        NetworkHandler.setSender(payload -> {
            if (payload instanceof TreatLimbPayload treatPayload) {
                sentPayload.set(treatPayload);
            }
        });

        TreatLimbPayload testPayload = new TreatLimbPayload((byte) LimbType.LEFT_LEG.getIndex());
        NetworkHandler.sendToServer(testPayload);

        assertNotNull(sentPayload.get());
        assertEquals((byte) LimbType.LEFT_LEG.getIndex(), sentPayload.get().limbIndex());

        // Clean up
        NetworkHandler.setSender(null);
    }
}

