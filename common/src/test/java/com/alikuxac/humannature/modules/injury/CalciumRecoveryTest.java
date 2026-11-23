package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.api.injury.InjuryTier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalciumRecoveryTest {
    @Test
    void remembersCalciumAssistanceAfterTheBuffExpires() {
        UUID id = fracturedPlayer();
        assertFalse(InjuryModule.reduceNaturalFractureTimer(id, 1, 2.0));
        assertTrue(InjuryModule.reduceNaturalFractureTimer(id, 100_000, 1.0));
        assertFalse(InjuryModule.isFractureActive(id));
        assertFalse(InjuryModule.reduceNaturalFractureTimer(id, 100_000, 1.0));
    }

    @Test
    void calciumFirstUsedOnTheFinalOrdinaryHealingTickDoesNotQualify() {
        UUID id = fracturedPlayer();
        InjuryModule.reduceFractureTimer(id, InjuryModule.getRemainingFractureTime(id) - 1);
        assertFalse(InjuryModule.reduceNaturalFractureTimer(id, 1, 2.0));
    }

    @Test
    void ordinaryRecoveryDoesNotQualify() {
        assertFalse(InjuryModule.reduceNaturalFractureTimer(fracturedPlayer(), 100_000, 1.0));
    }

    @Test
    void nonNaturalCuresAndResetsDiscardAssistance() {
        UUID id = fracturedPlayer();
        InjuryModule.reduceNaturalFractureTimer(id, 1, 2.0);
        InjuryModule.reduceFractureTimer(id, 100_000);
        assertFalse(InjuryModule.reduceNaturalFractureTimer(id, 100_000, 1.0));
        InjuryModule.startFractureTimer(id, InjuryTier.TIER_1_SINGLE_LEG);
        assertFalse(InjuryModule.reduceNaturalFractureTimer(id, 100_000, 1.0));

        InjuryModule.startFractureTimer(id, InjuryTier.TIER_1_SINGLE_LEG);
        InjuryModule.reduceNaturalFractureTimer(id, 1, 2.0);
        InjuryModule.clearFractureTimer(id);
        InjuryModule.startFractureTimer(id, InjuryTier.TIER_1_SINGLE_LEG);
        assertFalse(InjuryModule.reduceNaturalFractureTimer(id, 100_000, 1.0));
    }

    @Test
    void restartingAnInjuryAndZeroHealingCannotCarryEligibility() {
        UUID id = fracturedPlayer();
        InjuryModule.reduceNaturalFractureTimer(id, 1, 2.0);
        InjuryModule.startFractureTimer(id, InjuryTier.TIER_1_SINGLE_LEG);
        assertFalse(InjuryModule.reduceNaturalFractureTimer(id, 100_000, 1.0));
        id = fracturedPlayer();
        assertFalse(InjuryModule.reduceNaturalFractureTimer(id, 0, 2.0));
        assertFalse(InjuryModule.reduceNaturalFractureTimer(id, 100_000, 1.0));
    }

    private UUID fracturedPlayer() {
        UUID id = UUID.randomUUID();
        InjuryModule.startFractureTimer(id, InjuryTier.TIER_1_SINGLE_LEG);
        return id;
    }
}
