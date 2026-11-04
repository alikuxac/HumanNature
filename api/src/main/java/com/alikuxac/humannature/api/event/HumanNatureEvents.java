package com.alikuxac.humannature.api.event;

import java.util.function.Consumer;

public final class HumanNatureEvents {
    private HumanNatureEvents() {}

    public static void registerInjuryListener(Consumer<LimbInjuryEvent> listener) {
        HumanNatureEventBus.getInstance().register(LimbInjuryEvent.class, listener);
    }

    public static void unregisterInjuryListener(Consumer<LimbInjuryEvent> listener) {
        HumanNatureEventBus.getInstance().unregister(LimbInjuryEvent.class, listener);
    }

    public static void registerFractureListener(Consumer<FractureEvent> listener) {
        HumanNatureEventBus.getInstance().register(FractureEvent.class, listener);
    }

    public static void unregisterFractureListener(Consumer<FractureEvent> listener) {
        HumanNatureEventBus.getInstance().unregister(FractureEvent.class, listener);
    }

    public static void registerEncumbranceListener(Consumer<EncumbranceChangeEvent> listener) {
        HumanNatureEventBus.getInstance().register(EncumbranceChangeEvent.class, listener);
    }

    public static void unregisterEncumbranceListener(Consumer<EncumbranceChangeEvent> listener) {
        HumanNatureEventBus.getInstance().unregister(EncumbranceChangeEvent.class, listener);
    }

    public static void registerHypothermiaListener(Consumer<HypothermiaEvent> listener) {
        HumanNatureEventBus.getInstance().register(HypothermiaEvent.class, listener);
    }

    public static void unregisterHypothermiaListener(Consumer<HypothermiaEvent> listener) {
        HumanNatureEventBus.getInstance().unregister(HypothermiaEvent.class, listener);
    }

    public static LimbInjuryEvent postInjury(LimbInjuryEvent event) {
        return HumanNatureEventBus.getInstance().post(event);
    }

    public static FractureEvent postFracture(FractureEvent event) {
        return HumanNatureEventBus.getInstance().post(event);
    }

    public static EncumbranceChangeEvent postEncumbrance(EncumbranceChangeEvent event) {
        return HumanNatureEventBus.getInstance().post(event);
    }

    public static HypothermiaEvent postHypothermia(HypothermiaEvent event) {
        return HumanNatureEventBus.getInstance().post(event);
    }
}
