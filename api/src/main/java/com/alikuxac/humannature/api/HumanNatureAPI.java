package com.alikuxac.humannature.api;

import com.alikuxac.humannature.api.event.HumanNatureEventBus;
import com.alikuxac.humannature.api.injury.IInjuryAccessor;
import com.alikuxac.humannature.api.nutrition.INutritionAccessor;
import com.alikuxac.humannature.api.temperature.ITemperatureAccessor;
import com.alikuxac.humannature.api.weight.IWeightAccessor;

public abstract class HumanNatureAPI {
    private static HumanNatureAPI instance;

    public static HumanNatureAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("HumanNatureAPI has not been initialized yet. Ensure the mod is loaded.");
        }
        return instance;
    }

    public static void setInstance(HumanNatureAPI newInstance) {
        if (instance != null && newInstance != null) {
            throw new IllegalStateException("HumanNatureAPI instance has already been registered!");
        }
        instance = newInstance;
    }

    public static boolean isAvailable() {
        return instance != null;
    }

    public abstract IInjuryAccessor getInjury();

    public abstract INutritionAccessor getNutrition();

    public abstract ITemperatureAccessor getTemperature();

    public abstract IWeightAccessor getWeight();

    public HumanNatureEventBus getEventBus() {
        return HumanNatureEventBus.getInstance();
    }
}
