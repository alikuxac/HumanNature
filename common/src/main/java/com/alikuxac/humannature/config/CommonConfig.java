package com.alikuxac.humannature.config;

import java.util.function.Supplier;

public class CommonConfig {
    public static Supplier<Boolean> ENABLE_INJURY = () -> true;
    public static Supplier<Boolean> ENABLE_TEMPERATURE = () -> true;
    public static Supplier<Boolean> ENABLE_WEIGHT = () -> true;
    public static Supplier<Boolean> ENABLE_PHYSIOLOGY = () -> true;

    public static Supplier<Boolean> ENABLE_SPLINT = () -> true;
    public static Supplier<Double> SPLINT_SPEED_REDUCTION = () -> 0.10;
    public static Supplier<Integer> SPLINT_DURABILITY_PER_50_STEPS = () -> 1;
}
