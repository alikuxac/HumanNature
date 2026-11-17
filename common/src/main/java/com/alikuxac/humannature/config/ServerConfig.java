package com.alikuxac.humannature.config;

import java.util.function.Supplier;

public class ServerConfig {

    public static Supplier<Boolean> ENABLE_INJURY = () -> true;
    public static Supplier<Boolean> ENABLE_TEMPERATURE = () -> true;
    public static Supplier<Boolean> ENABLE_WEIGHT = () -> true;
    public static Supplier<Boolean> ENABLE_PHYSIOLOGY = () -> true;

    public static Supplier<Boolean> ENABLE_SPLINT = () -> true;
    public static Supplier<Integer> BONE_FRACTURE_DURATION = () -> 6000;
    public static Supplier<Boolean> ENABLE_TOTEM_BYPASS = () -> true;
    public static Supplier<Boolean> ENABLE_GFORCE_BLACKOUT = () -> true;
    public static Supplier<Boolean> ENABLE_KINETIC_CRASH = () -> true;

    public static final double SPLINT_SPEED_REDUCTION = 0.10;
    public static final int SPLINT_DURABILITY_PER_50_STEPS = 1;
    public static final double BONE_FRACTURE_CROUCH_MULTIPLIER = 0.5;

    public static Supplier<Double> TEMPERATURE_CHANGE_RATE_MULTIPLIER = () -> 1.0;
    public static Supplier<Double> HYPOTHERMIA_PENALTY_THRESHOLD = () -> 0.60;
    public static Supplier<Double> HEAT_STRESS_OVERHEAT_THRESHOLD = () -> 39.5;
    public static Supplier<Double> HEAT_STRESS_DAMAGE_THRESHOLD = () -> 42.0;

    public static final boolean ENABLE_WETNESS_ACCUMULATION = true;
    public static final double WETNESS_RAIN_ACCUMULATION_RATE = 0.0025;
    public static final double WETNESS_WATER_ACCUMULATION_RATE = 0.015;
    public static final double WETNESS_NO_HELMET_MULTIPLIER = 2.0;
    public static final double WETNESS_FREEZE_IMMUNE_REDUCTION = 0.65;

    public static final boolean ENABLE_HEAT_REWARMING = true;
    public static final double HEAT_SOURCE_RADIUS = 3.0;
    public static final double HEAT_DRYING_TIME_SECONDS = 12.5;
    public static final double HEAT_THAW_MULTIPLIER = 5.0;

    public static final boolean ENABLE_HYPOTHERMIA = true;
    public static final int INTERNAL_WARMING_DURATION_TICKS = 1600;
    public static final boolean HYPOTHERMIA_MINING_FATIGUE = true;
    public static final double HYPOTHERMIA_ATTACK_SPEED_PENALTY = 0.35;
    public static final double HYPOTHERMIA_INTERACTION_RANGE_PENALTY = 1.0;

    public static final boolean ENABLE_ARMOR_THERMAL_INSULATION = true;
    public static final double LEATHER_INSULATION_FREEZE_REDUCTION = 0.60;
    public static final double METALLIC_ARMOR_COLD_CONDUCTION_MULTIPLIER = 1.50;

    public static final boolean ENABLE_HEAVY_LOAD_HEAT_STRESS = true;
    public static final double HEAT_STRESS_TEMP_RISE_RATE = 0.0015;
    public static final double HEAT_STRESS_EXERTION_DRAIN = 0.10;
    public static final int HEAT_STRESS_DAMAGE_INTERVAL_TICKS = 40;

    public static Supplier<Double> WEIGHT_MAX_CAPACITY = () -> 500.0;
    public static Supplier<Double> WEIGHT_PENALTY_MULTIPLIER = () -> 1.0;

    public static final double WEIGHT_HEAVY_BLOCK_BASE = 0.8;
    public static final double WEIGHT_METALLIC_ITEM_BASE = 0.4;
    public static final double WEIGHT_DEFAULT_ITEM_BASE = 0.05;
    public static final double WEIGHT_LIGHT_ITEM_BASE = 0.005;

    public static final double WEIGHT_LIGHT_THRESHOLD = 0.50;
    public static final double WEIGHT_MEDIUM_THRESHOLD = 0.70;
    public static final double WEIGHT_OVERBURDENED_THRESHOLD = 0.85;

    public static final double WEIGHT_LIGHT_SPEED_PENALTY = 0.10;
    public static final double WEIGHT_MEDIUM_SPEED_PENALTY = 0.25;
    public static final double WEIGHT_OVERBURDENED_SPEED_PENALTY = 0.50;

    public static final double WEIGHT_LIGHT_SINK_PULL = 0.015;
    public static final double WEIGHT_MEDIUM_SINK_PULL = 0.035;
    public static final double WEIGHT_OVERBURDENED_SINK_PULL = 0.065;

    public static final double WEIGHT_LIGHT_FALL_DAMAGE_MULTIPLIER = 1.25;
    public static final double WEIGHT_MEDIUM_FALL_DAMAGE_MULTIPLIER = 1.50;
    public static final double WEIGHT_OVERBURDENED_FALL_DAMAGE_MULTIPLIER = 2.00;

    public static final double BURDEN_BEARER_CAPACITY_BONUS = 150.0;
    public static final double LOAD_BEARING_CAPACITY_BONUS = 50.0;
    public static final double WEIGHT_CONTAINER_RATIO = 1.0;
    public static final double LOAD_BEARING_CONTAINER_REDUCTION = 0.20;

    public static Supplier<Boolean> ENABLE_SLEEP_DEPRIVATION = () -> true;
    public static Supplier<Integer> SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS = () -> 72000;
    public static Supplier<Boolean> ENABLE_TOXIN_BUILDUP = () -> true;
    public static Supplier<Boolean> ENABLE_PHYSICAL_EXERTION = () -> true;

    public static final double SLEEP_FATIGUE_BLINK_THRESHOLD = 0.80;
    public static final int SLEEP_FATIGUE_BLINK_MIN_INTERVAL = 300;
    public static final int SLEEP_FATIGUE_BLINK_MAX_INTERVAL = 800;

    public static final double TOXIN_BASE_BUILDUP_PER_HIT = 25.0;
    public static final int TOXIN_DECAY_DELAY_TICKS = 80;
    public static final double TOXIN_DECAY_RATE_PER_SECOND = 2.0;
    public static final double TOXIN_RESTING_HEAT_DECAY_MULTIPLIER = 3.0;
    public static final boolean ENABLE_TOXIN_FATIGUE_SCALING = true;
    public static final double TOXIN_FRACTURE_BUILDUP_MULTIPLIER = 1.5;

    public static final int EXERTION_CONTINUOUS_MINING_TICKS = 200;
    public static final int EXERTION_REST_RECOVERY_TICKS = 70;
    public static final double EXERTION_ATTACK_SPEED_PENALTY = 0.50;
    public static final double EXERTION_MINING_SPEED_PENALTY = 0.50;
    public static final double EXERTION_SPRINT_DRAIN_PER_TICK = 0.08;

    public static final boolean ENABLE_SWIMMING_CRAMPS = true;
    public static final int SWIMMING_CRAMP_THRESHOLD_TICKS = 600;
    public static final double SWIMMING_CRAMP_SPEED_PENALTY = 0.40;
    public static final int SWIMMING_CRAMP_RECOVERY_TICKS = 60;

    public static final boolean ENABLE_DIGESTION_WINDOW = true;
    public static final int DIGESTION_INTERVAL_TICKS = 30;
    public static final boolean ENABLE_OVEREATING_REFLEX = true;
    public static final boolean ENABLE_INDIGESTION = true;
    public static final boolean ENABLE_CALCIUM_HEALING = true;
    public static final double CALCIUM_HEALING_MULTIPLIER = 2.0;

    public static final boolean ENABLE_CLAUSTROPHOBIA_PANIC = true;
    public static final int PANIC_THRESHOLD_TICKS = 100;

    public static final boolean ENABLE_ADRENALINE_SURGE = true;
    public static final int ADRENALINE_SURGE_DURATION_TICKS = 80;
}
