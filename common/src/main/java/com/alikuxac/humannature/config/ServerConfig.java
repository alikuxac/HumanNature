package com.alikuxac.humannature.config;

import java.util.function.Supplier;

public class ServerConfig {
    public static Supplier<Boolean> ENABLE_INJURY = () -> true;
    public static Supplier<Boolean> ENABLE_TEMPERATURE = () -> true;
    public static Supplier<Boolean> ENABLE_WEIGHT = () -> true;
    public static Supplier<Boolean> ENABLE_PHYSIOLOGY = () -> true;

    public static Supplier<Boolean> ENABLE_SPLINT = () -> true;
    public static Supplier<Double> SPLINT_SPEED_REDUCTION = () -> 0.10;
    public static Supplier<Integer> SPLINT_DURABILITY_PER_50_STEPS = () -> 1;

    public static Supplier<Integer> BONE_FRACTURE_DURATION = () -> 6000;
    public static Supplier<Double> BONE_FRACTURE_CROUCH_MULTIPLIER = () -> 0.5;
    public static Supplier<Boolean> ENABLE_TOTEM_BYPASS = () -> true;
    public static Supplier<Boolean> ENABLE_GFORCE_BLACKOUT = () -> true;
    public static Supplier<Boolean> ENABLE_KINETIC_CRASH = () -> true;

    public static Supplier<Boolean> ENABLE_SLEEP_DEPRIVATION = () -> true;
    public static Supplier<Integer> SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS = () -> 72000;
    public static Supplier<Double> SLEEP_FATIGUE_BLINK_THRESHOLD = () -> 0.80;
    public static Supplier<Integer> SLEEP_FATIGUE_BLINK_MIN_INTERVAL = () -> 300;
    public static Supplier<Integer> SLEEP_FATIGUE_BLINK_MAX_INTERVAL = () -> 800;

    public static Supplier<Boolean> ENABLE_TOXIN_BUILDUP = () -> true;
    public static Supplier<Double> TOXIN_BASE_BUILDUP_PER_HIT = () -> 25.0;
    public static Supplier<Integer> TOXIN_DECAY_DELAY_TICKS = () -> 80;
    public static Supplier<Double> TOXIN_DECAY_RATE_PER_SECOND = () -> 2.0;
    public static Supplier<Double> TOXIN_RESTING_HEAT_DECAY_MULTIPLIER = () -> 3.0;
    public static Supplier<Boolean> ENABLE_TOXIN_FATIGUE_SCALING = () -> true;
    public static Supplier<Double> TOXIN_FRACTURE_BUILDUP_MULTIPLIER = () -> 1.5;

    public static Supplier<Boolean> ENABLE_PHYSICAL_EXERTION = () -> true;
    public static Supplier<Integer> EXERTION_CONTINUOUS_MINING_TICKS = () -> 200;
    public static Supplier<Integer> EXERTION_REST_RECOVERY_TICKS = () -> 70;
    public static Supplier<Double> EXERTION_ATTACK_SPEED_PENALTY = () -> 0.50;
    public static Supplier<Double> EXERTION_MINING_SPEED_PENALTY = () -> 0.50;
    public static Supplier<Double> EXERTION_SPRINT_DRAIN_PER_TICK = () -> 0.08;
    public static Supplier<Boolean> ENABLE_SWIMMING_CRAMPS = () -> true;
    public static Supplier<Integer> SWIMMING_CRAMP_THRESHOLD_TICKS = () -> 600;
    public static Supplier<Double> SWIMMING_CRAMP_SPEED_PENALTY = () -> 0.40;
    public static Supplier<Integer> SWIMMING_CRAMP_RECOVERY_TICKS = () -> 60;

    public static Supplier<Boolean> ENABLE_DIGESTION_WINDOW = () -> true;
    public static Supplier<Integer> DIGESTION_INTERVAL_TICKS = () -> 30;
    public static Supplier<Boolean> ENABLE_OVEREATING_REFLEX = () -> true;
    public static Supplier<Boolean> ENABLE_INDIGESTION = () -> true;
    public static Supplier<Boolean> ENABLE_CALCIUM_HEALING = () -> true;
    public static Supplier<Double> CALCIUM_HEALING_MULTIPLIER = () -> 2.0;

    public static Supplier<Boolean> ENABLE_CLAUSTROPHOBIA_PANIC = () -> true;
    public static Supplier<Integer> PANIC_THRESHOLD_TICKS = () -> 100;

    public static Supplier<Boolean> ENABLE_ADRENALINE_SURGE = () -> true;
    public static Supplier<Integer> ADRENALINE_SURGE_DURATION_TICKS = () -> 80;
}
