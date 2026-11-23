package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.api.injury.InjuryTier;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerPhysiologyNutritionTest {

    @Test
    void testReserveDefaultsAndClamping() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();

        assertEquals(0, data.getCalcium());
        assertEquals(0, data.getCarbohydrates());
        assertEquals(0, data.getVitamins());

        data.modifyCalcium(150);
        assertEquals(100, data.getCalcium());
        data.modifyCalcium(-200);
        assertEquals(0, data.getCalcium());

        data.setCarbohydrates(75);
        assertEquals(75, data.getCarbohydrates());
        data.setVitamins(90);
        assertEquals(90, data.getVitamins());
    }

    @Test
    void testCalciumBuffsScaleWithReserve() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();

        assertEquals(1.0, data.getCalciumKnittingMultiplier(), 0.0001);
        assertEquals(0.0f, data.getCalciumFallBonusBlocks(), 0.0001f);
        assertEquals(1.0f, data.getFallDamageMultiplier(), 0.0001f);

        data.setCalcium(50);
        assertEquals(1.125, data.getCalciumKnittingMultiplier(), 0.0001);
        assertEquals(1.0f, data.getCalciumFallBonusBlocks(), 0.0001f);
        assertEquals(0.875f, data.getFallDamageMultiplier(), 0.0001f);

        data.setCalcium(100);
        assertEquals(1.25, data.getCalciumKnittingMultiplier(), 0.0001);
        assertEquals(2.0f, data.getCalciumFallBonusBlocks(), 0.0001f);
        assertEquals(0.75f, data.getFallDamageMultiplier(), 0.0001f);
    }

    @Test
    void testCarbohydrateStaminaCapacityExpansion() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();

        assertEquals(100.0f, data.getStaminaCapacity(), 0.0001f);
        data.setCarbohydrates(50);
        assertEquals(125.0f, data.getStaminaCapacity(), 0.0001f);
        data.setCarbohydrates(100);
        assertEquals(150.0f, data.getStaminaCapacity(), 0.0001f);

        data.setExertionScore(140.0f);
        assertEquals(140.0f, data.getExertionScore(), 0.0001f);
        assertFalse(data.isMuscleFailure());

        data.setExertionScore(151.0f);
        assertEquals(150.0f, data.getExertionScore(), 0.0001f);
        assertTrue(data.isMuscleFailure());
    }

    @Test
    void testVitaminBuffsScaleWithReserve() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();

        assertEquals(1.0f, data.getColdAbsorptionMultiplier(), 0.0001f);
        assertEquals(1.0f, data.getToxinEliminationMultiplier(), 0.0001f);

        data.setVitamins(100);
        assertEquals(0.5f, data.getColdAbsorptionMultiplier(), 0.0001f);
        assertEquals(2.0f, data.getToxinEliminationMultiplier(), 0.0001f);
    }

    @Test
    void testNutrientTickCounterAndReset() {
        PlayerPhysiologyData data = new PlayerPhysiologyData();
        assertEquals(0, data.getNutrientTickCounter());

        data.incrementNutrientTickCounter();
        data.incrementNutrientTickCounter();
        assertEquals(2, data.getNutrientTickCounter());

        data.resetNutrientTickCounter();
        assertEquals(0, data.getNutrientTickCounter());

        data.setCalcium(40);
        data.setCarbohydrates(60);
        data.setVitamins(80);
        data.resetCalcium();

        assertEquals(0, data.getCalcium());
        assertEquals(0, data.getCarbohydrates());
        assertEquals(0, data.getVitamins());
        assertEquals(0, data.getNutrientTickCounter());
        assertEquals(0, data.getCalciumAbsorptionTicks());
    }

    @Test
    void testConventionalTagKeys() {
        assertEquals("c", NutritionEventHandler.CALCIUM_RICH_FOODS.location().getNamespace());
        assertEquals("foods/calcium_rich", NutritionEventHandler.CALCIUM_RICH_FOODS.location().getPath());
        assertEquals("foods/carbohydrates", NutritionEventHandler.CARBOHYDRATE_FOODS.location().getPath());
        assertEquals("foods/vegetables", NutritionEventHandler.VEGETABLE_FOODS.location().getPath());
        assertEquals("foods/fruits", NutritionEventHandler.FRUIT_FOODS.location().getPath());
    }

    @Test
    void testFractionalKnittingAccumulatesWithoutRoundingLoss() {
        UUID playerId = UUID.randomUUID();
        InjuryModule.startFractureTimer(playerId, InjuryTier.TIER_1_SINGLE_LEG);
        int initial = InjuryModule.getRemainingFractureTime(playerId);

        for (int i = 0; i < 4; i++) {
            InjuryModule.reduceFractureTimerScaled(playerId, 1.25);
        }

        assertEquals(initial - 5, InjuryModule.getRemainingFractureTime(playerId));
        InjuryModule.clearFractureTimer(playerId);
    }
}
