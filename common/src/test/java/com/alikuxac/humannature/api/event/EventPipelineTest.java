package com.alikuxac.humannature.api.event;

import com.alikuxac.humannature.api.injury.InjuryTier;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.api.weight.EncumbranceTier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class EventPipelineTest {

    private Consumer<LimbInjuryEvent> injuryListener;
    private Consumer<FractureEvent> fractureListener;
    private Consumer<EncumbranceChangeEvent> encumbranceListener;
    private Consumer<HypothermiaEvent> hypothermiaListener;

    @AfterEach
    void tearDown() {
        if (injuryListener != null) HumanNatureEvents.unregisterInjuryListener(injuryListener);
        if (fractureListener != null) HumanNatureEvents.unregisterFractureListener(fractureListener);
        if (encumbranceListener != null) HumanNatureEvents.unregisterEncumbranceListener(encumbranceListener);
        if (hypothermiaListener != null) HumanNatureEvents.unregisterHypothermiaListener(hypothermiaListener);
    }

    @Test
    void testLimbInjuryEventCancellationAndModification() {
        injuryListener = event -> {
            if (event.getLimb() == LimbType.HEAD) {
                event.setCanceled(true);
            } else {
                event.setSeverity(1);
            }
        };
        HumanNatureEvents.registerInjuryListener(injuryListener);

        LimbInjuryEvent headEvent = new LimbInjuryEvent(null, LimbType.HEAD, 2);
        HumanNatureEvents.postInjury(headEvent);
        assertTrue(headEvent.isCanceled());

        LimbInjuryEvent torsoEvent = new LimbInjuryEvent(null, LimbType.TORSO, 2);
        HumanNatureEvents.postInjury(torsoEvent);
        assertFalse(torsoEvent.isCanceled());
        assertEquals(1, torsoEvent.getSeverity());
    }

    @Test
    void testFractureEventDispatch() {
        AtomicReference<InjuryTier> receivedTier = new AtomicReference<>(null);
        AtomicBoolean wasHealed = new AtomicBoolean(false);

        fractureListener = event -> {
            receivedTier.set(event.getTier());
            wasHealed.set(event.isHealed());
        };
        HumanNatureEvents.registerFractureListener(fractureListener);

        FractureEvent event = new FractureEvent(null, InjuryTier.TIER_2_DOUBLE_LEG, false);
        HumanNatureEvents.postFracture(event);

        assertEquals(InjuryTier.TIER_2_DOUBLE_LEG, receivedTier.get());
        assertFalse(wasHealed.get());
    }

    @Test
    void testEncumbranceChangeEventDispatch() {
        AtomicReference<EncumbranceTier> targetTier = new AtomicReference<>(null);

        encumbranceListener = event -> targetTier.set(event.getNewTier());
        HumanNatureEvents.registerEncumbranceListener(encumbranceListener);

        EncumbranceChangeEvent event = new EncumbranceChangeEvent(null, EncumbranceTier.NORMAL, EncumbranceTier.OVERBURDENED, 350.0f);
        HumanNatureEvents.postEncumbrance(event);

        assertEquals(EncumbranceTier.OVERBURDENED, targetTier.get());
        assertEquals(350.0f, event.getCarriedWeight(), 0.01f);
    }

    @Test
    void testHypothermiaEventCancellation() {
        hypothermiaListener = event -> {
            if (event.getCoreTemperature() > 30.0f) {
                event.setCanceled(true);
            }
        };
        HumanNatureEvents.registerHypothermiaListener(hypothermiaListener);

        HypothermiaEvent mildCold = new HypothermiaEvent(null, 32.0f);
        HumanNatureEvents.postHypothermia(mildCold);
        assertTrue(mildCold.isCanceled());

        HypothermiaEvent severeCold = new HypothermiaEvent(null, 28.0f);
        HumanNatureEvents.postHypothermia(severeCold);
        assertFalse(severeCold.isCanceled());
    }
}

