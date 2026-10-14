package com.alikuxac.humannature.config;

import java.util.function.Supplier;

public class ClientConfig {
    // HUD Overlay
    public static Supplier<Boolean> ENABLE_HUD_OVERLAY = () -> true;
    public static Supplier<Integer> HUD_X = () -> 10;
    public static Supplier<Integer> HUD_Y = () -> 10;

    // Camera & Visual Effects
    public static Supplier<Boolean> ENABLE_CAMERA_LIMPING = () -> true;
}
