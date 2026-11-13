package com.alikuxac.humannature.api.impl;

import com.alikuxac.humannature.api.HumanNatureAPI;
import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.api.blood.IBloodAccessor;
import com.alikuxac.humannature.api.injury.InjuryTier;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.api.weight.EncumbranceTier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccessorContractTest {

    @BeforeAll
    static void setUp() {
        if (!HumanNatureAPI.isAvailable()) {
            HumanNatureAPI.setInstance(new HumanNatureAPIImpl());
        }
    }

    @Test
    void testAPIInitialization() {
        HumanNatureAPI api = HumanNatureAPI.getInstance();
        assertNotNull(api);
        assertNotNull(api.getInjury());
        assertNotNull(api.getNutrition());
        assertNotNull(api.getTemperature());
        assertNotNull(api.getWeight());
        assertNotNull(api.getBlood());
        assertNotNull(api.getEventBus());
    }

    @Test
    void testInjuryAccessorContracts() {
        HumanNatureAPI api = HumanNatureAPI.getInstance();
        UUID playerId = UUID.randomUUID();

        assertFalse(api.getInjury().hasAnyInjury(playerId));
        assertEquals(0, api.getInjury().getLimbSeverity(playerId, LimbType.HEAD));

        api.getInjury().setLimbSeverity(playerId, LimbType.HEAD, 2);
        assertTrue(api.getInjury().hasAnyInjury(playerId));
        assertEquals(2, api.getInjury().getLimbSeverity(playerId, LimbType.HEAD));
        assertEquals(1, api.getInjury().getTotalInjuries(playerId));

        api.getInjury().treatLimb(playerId, LimbType.HEAD);
        assertEquals(1, api.getInjury().getLimbSeverity(playerId, LimbType.HEAD));

        api.getInjury().clearInjuries(playerId);
        assertFalse(api.getInjury().hasAnyInjury(playerId));
        assertEquals(0, api.getInjury().getLimbSeverity(playerId, LimbType.HEAD));
    }

    @Test
    void testNutritionAccessorContracts() {
        HumanNatureAPI api = HumanNatureAPI.getInstance();
        UUID playerId = UUID.randomUUID();

        assertEquals(0, api.getNutrition().getCalcium(playerId));
        assertEquals(0, api.getNutrition().getCarbohydrates(playerId));
        assertEquals(0, api.getNutrition().getVitamins(playerId));

        api.getNutrition().modifyCalcium(playerId, 40);
        assertEquals(40, api.getNutrition().getCalcium(playerId));

        api.getNutrition().modifyCalcium(playerId, 100);
        assertEquals(100, api.getNutrition().getCalcium(playerId));

        api.getNutrition().modifyCalcium(playerId, -200);
        assertEquals(0, api.getNutrition().getCalcium(playerId));

        api.getNutrition().setCarbohydrates(playerId, 75);
        assertEquals(75, api.getNutrition().getCarbohydrates(playerId));

        api.getNutrition().setVitamins(playerId, 90);
        assertEquals(90, api.getNutrition().getVitamins(playerId));

        assertEquals(0.0f, api.getNutrition().getToxinBuildup(playerId), 0.01f);
        api.getNutrition().setToxinBuildup(playerId, 25.5f);
        assertEquals(25.5f, api.getNutrition().getToxinBuildup(playerId), 0.01f);
        assertEquals(0.0f, api.getNutrition().getExertionScore(playerId), 0.01f);
    }

    @Test
    void testTemperatureAccessorContracts() {
        HumanNatureAPI api = HumanNatureAPI.getInstance();
        UUID playerId = UUID.randomUUID();

        assertEquals(37.0f, api.getTemperature().getCoreTemperature(playerId), 0.01f);
        api.getTemperature().modifyCoreTemperature(playerId, -3.0f);
        assertEquals(34.0f, api.getTemperature().getCoreTemperature(playerId), 0.01f);
        assertTrue(api.getTemperature().isFreezing(playerId));

        assertEquals(0.0f, api.getTemperature().getWetnessBuffer(playerId), 0.01f);
        api.getTemperature().setWetnessBuffer(playerId, 0.8f);
        assertEquals(0.8f, api.getTemperature().getWetnessBuffer(playerId), 0.01f);
    }

    @Test
    void testWeightAccessorContracts() {
        HumanNatureAPI api = HumanNatureAPI.getInstance();
        UUID playerId = UUID.randomUUID();

        assertEquals(0.0f, api.getWeight().getCarriedWeight(playerId), 0.01f);
        assertEquals(500.0f, api.getWeight().getMaxCapacity(playerId), 0.01f);
        assertEquals(EncumbranceTier.NORMAL, api.getWeight().getEncumbranceTier(playerId));
        assertFalse(api.getWeight().isEncumbered(playerId));
    }

    @Test
    void testBloodAccessorContracts() {
        HumanNatureAPI api = HumanNatureAPI.getInstance();
        IBloodAccessor blood = api.getBlood();
        assertNotNull(blood);
        UUID playerId = UUID.randomUUID();

        // Default standard volume 5000 ml
        assertEquals(IBloodAccessor.DEFAULT_BLOOD_VOLUME, blood.getBloodVolume(playerId));
        assertNull(blood.getBloodType(playerId));
        assertEquals((byte) 0, blood.getBleedingTier(playerId));

        // Assign BloodType
        blood.setBloodType(playerId, BloodType.A_POSITIVE);
        assertEquals(BloodType.A_POSITIVE, blood.getBloodType(playerId));

        // Modify volume
        blood.modifyBloodVolume(playerId, -500);
        assertEquals((short) 4500, blood.getBloodVolume(playerId));

        blood.setBloodVolume(playerId, (short) 3200);
        assertEquals((short) 3200, blood.getBloodVolume(playerId));

        // Upper clamp
        blood.modifyBloodVolume(playerId, 10000);
        assertEquals(IBloodAccessor.MAX_BLOOD_VOLUME, blood.getBloodVolume(playerId));

        // Lower clamp
        blood.modifyBloodVolume(playerId, -10000);
        assertEquals((short) 0, blood.getBloodVolume(playerId));

        // Bleeding tier
        blood.setBleedingTier(playerId, (byte) 2);
        assertEquals((byte) 2, blood.getBleedingTier(playerId));

        // Clamp bleeding tier max 3
        blood.setBleedingTier(playerId, (byte) 10);
        assertEquals((byte) 3, blood.getBleedingTier(playerId));
    }
}
