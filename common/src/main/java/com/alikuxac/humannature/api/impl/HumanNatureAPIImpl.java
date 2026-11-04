package com.alikuxac.humannature.api.impl;

import com.alikuxac.humannature.api.HumanNatureAPI;
import com.alikuxac.humannature.api.injury.IInjuryAccessor;
import com.alikuxac.humannature.api.nutrition.INutritionAccessor;
import com.alikuxac.humannature.api.temperature.ITemperatureAccessor;
import com.alikuxac.humannature.api.weight.IWeightAccessor;

public class HumanNatureAPIImpl extends HumanNatureAPI {
    private final IInjuryAccessor injury = new InjuryAccessorImpl();
    private final INutritionAccessor nutrition = new NutritionAccessorImpl();
    private final ITemperatureAccessor temperature = new TemperatureAccessorImpl();
    private final IWeightAccessor weight = new WeightAccessorImpl();

    @Override
    public IInjuryAccessor getInjury() {
        return injury;
    }

    @Override
    public INutritionAccessor getNutrition() {
        return nutrition;
    }

    @Override
    public ITemperatureAccessor getTemperature() {
        return temperature;
    }

    @Override
    public IWeightAccessor getWeight() {
        return weight;
    }
}

