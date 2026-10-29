package com.alikuxac.humannature.config;

import java.util.function.Supplier;

public class ClientConfig {
    public static Supplier<Boolean> ENABLE_HUD_OVERLAY = () -> true;
    public static Supplier<Integer> HUD_X = () -> 10;
    public static Supplier<Integer> HUD_Y = () -> 10;
    public static Supplier<Boolean> ENABLE_TOXIN_HUD_OVERLAY = () -> true;
    public static Supplier<Boolean> ENABLE_STAMINA_HUD_OVERLAY = () -> true;
    public static Supplier<Boolean> ENABLE_SWAPPABLE_HUD = () -> true;
    public static Supplier<String> DEFAULT_HUD_MODE = () -> "COMBINED";
    public static Supplier<Integer> SWAPPABLE_HUD_X = () -> 10;
    public static Supplier<Integer> SWAPPABLE_HUD_Y = () -> 10;
    public static Supplier<Boolean> SHOW_METABOLIC_DETAILS = () -> true;
    public static Supplier<Boolean> SHOW_CARDIAC_HUD = () -> true;

    public static Supplier<Boolean> ENABLE_CAMERA_LIMPING = () -> true;
    public static Supplier<Boolean> ENABLE_SHIVERING_CAMERA = () -> true;
    public static Supplier<Boolean> SHOW_ACTIONBAR_ENVIRONMENT_CUES = () -> true;
}
