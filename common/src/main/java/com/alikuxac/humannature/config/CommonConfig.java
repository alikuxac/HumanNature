package com.alikuxac.humannature.config;

import java.util.function.Supplier;

public class CommonConfig {
    public static Supplier<Boolean> ENABLE_INJURY = () -> true;
    public static Supplier<Boolean> ENABLE_TEMPERATURE = () -> true;
    public static Supplier<Boolean> ENABLE_WEIGHT = () -> true;
    public static Supplier<Boolean> ENABLE_PHYSIOLOGY = () -> true;
}
