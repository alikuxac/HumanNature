package com.alikuxac.humannature.config;

import java.util.function.Supplier;

public class ServerConfig {
    // Module Master Toggles
    public static Supplier<Boolean> ENABLE_INJURY = () -> true;
    public static Supplier<Boolean> ENABLE_TEMPERATURE = () -> true;
    public static Supplier<Boolean> ENABLE_WEIGHT = () -> true;
    public static Supplier<Boolean> ENABLE_PHYSIOLOGY = () -> true;

    // Splint Mechanics
    public static Supplier<Boolean> ENABLE_SPLINT = () -> true;
    public static Supplier<Double> SPLINT_SPEED_REDUCTION = () -> 0.10;
    public static Supplier<Integer> SPLINT_DURABILITY_PER_50_STEPS = () -> 1;

    // Bone Fracture & Trauma Mechanics
    public static Supplier<Integer> BONE_FRACTURE_DURATION = () -> 6000; // 6000 ticks = 5 minutes
    public static Supplier<Double> BONE_FRACTURE_CROUCH_MULTIPLIER = () -> 0.5; // half speed when crouching
    public static Supplier<Boolean> ENABLE_TOTEM_BYPASS = () -> true;
    public static Supplier<Boolean> ENABLE_GFORCE_BLACKOUT = () -> true;
    public static Supplier<Boolean> ENABLE_KINETIC_CRASH = () -> true;

    // Physiology - Sleep Deprivation Mechanics
    public static Supplier<Boolean> ENABLE_SLEEP_DEPRIVATION = () -> true;
    public static Supplier<Integer> SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS = () -> 72000; // 3 in-game days
    public static Supplier<Double> SLEEP_FATIGUE_BLINK_THRESHOLD = () -> 0.80; // 80% fatigue
    public static Supplier<Integer> SLEEP_FATIGUE_BLINK_MIN_INTERVAL = () -> 300; // 15 seconds
    public static Supplier<Integer> SLEEP_FATIGUE_BLINK_MAX_INTERVAL = () -> 800; // 40 seconds

    // Physiology - Toxin Buildup Mechanics
    public static Supplier<Boolean> ENABLE_TOXIN_BUILDUP = () -> true;
    public static Supplier<Double> TOXIN_BASE_BUILDUP_PER_HIT = () -> 25.0; // 25% minimum buildup per exposure
    public static Supplier<Integer> TOXIN_DECAY_DELAY_TICKS = () -> 80; // 4 seconds before decay starts
    public static Supplier<Double> TOXIN_DECAY_RATE_PER_SECOND = () -> 2.0; // 2% per second passive decay
    public static Supplier<Double> TOXIN_RESTING_HEAT_DECAY_MULTIPLIER = () -> 3.0; // 3x decay near heat/resting
}
