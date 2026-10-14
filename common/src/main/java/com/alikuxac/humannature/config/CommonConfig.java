package com.alikuxac.humannature.config;

import java.util.function.Supplier;

/**
 * @deprecated Use {@link ServerConfig} for gameplay mechanics or {@link ClientConfig} for client visual/HUD options.
 */
@Deprecated
public class CommonConfig {
    public static Supplier<Boolean> ENABLE_INJURY = () -> ServerConfig.ENABLE_INJURY.get();
    public static Supplier<Boolean> ENABLE_TEMPERATURE = () -> ServerConfig.ENABLE_TEMPERATURE.get();
    public static Supplier<Boolean> ENABLE_WEIGHT = () -> ServerConfig.ENABLE_WEIGHT.get();
    public static Supplier<Boolean> ENABLE_PHYSIOLOGY = () -> ServerConfig.ENABLE_PHYSIOLOGY.get();

    public static Supplier<Boolean> ENABLE_SPLINT = () -> ServerConfig.ENABLE_SPLINT.get();
    public static Supplier<Double> SPLINT_SPEED_REDUCTION = () -> ServerConfig.SPLINT_SPEED_REDUCTION.get();
    public static Supplier<Integer> SPLINT_DURABILITY_PER_50_STEPS = () -> ServerConfig.SPLINT_DURABILITY_PER_50_STEPS.get();

    public static Supplier<Integer> BONE_FRACTURE_DURATION = () -> ServerConfig.BONE_FRACTURE_DURATION.get();
    public static Supplier<Double> BONE_FRACTURE_CROUCH_MULTIPLIER = () -> ServerConfig.BONE_FRACTURE_CROUCH_MULTIPLIER.get();
    public static Supplier<Boolean> ENABLE_TOTEM_BYPASS = () -> ServerConfig.ENABLE_TOTEM_BYPASS.get();
    public static Supplier<Boolean> ENABLE_GFORCE_BLACKOUT = () -> ServerConfig.ENABLE_GFORCE_BLACKOUT.get();
    public static Supplier<Boolean> ENABLE_KINETIC_CRASH = () -> ServerConfig.ENABLE_KINETIC_CRASH.get();

    // Client proxies
    public static Supplier<Boolean> ENABLE_HUD_OVERLAY = () -> ClientConfig.ENABLE_HUD_OVERLAY.get();
    public static Supplier<Boolean> ENABLE_CAMERA_LIMPING = () -> ClientConfig.ENABLE_CAMERA_LIMPING.get();
}
