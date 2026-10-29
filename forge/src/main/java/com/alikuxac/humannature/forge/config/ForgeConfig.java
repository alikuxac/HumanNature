package com.alikuxac.humannature.forge.config;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Supplier;

public class ForgeConfig {

    private static final ForgeConfigSpec SERVER_SPEC;
    private static final ForgeConfigSpec CLIENT_SPEC;

    static {
        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();

        serverBuilder.comment("Module master toggles").push("modules");
        ForgeConfigSpec.BooleanValue enableInjury = serverBuilder
                .comment("Enable injury & trauma module")
                .define("enableInjury", true);
        ForgeConfigSpec.BooleanValue enableTemperature = serverBuilder
                .comment("Enable body temperature module")
                .define("enableTemperature", true);
        ForgeConfigSpec.BooleanValue enableWeight = serverBuilder
                .comment("Enable inventory weight & encumbrance module")
                .define("enableWeight", true);
        ForgeConfigSpec.BooleanValue enablePhysiology = serverBuilder
                .comment("Enable physiology module")
                .define("enablePhysiology", true);
        serverBuilder.pop();

        serverBuilder.comment("Splint & limb stabilization mechanics").push("splint");
        ForgeConfigSpec.BooleanValue enableSplint = serverBuilder
                .comment("Enable splint mechanics")
                .define("enableSplint", true);
        ForgeConfigSpec.DoubleValue splintSpeedReduction = serverBuilder
                .comment("Movement speed penalty when wearing a splint")
                .defineInRange("splintSpeedReduction", 0.10, 0.0, 1.0);
        ForgeConfigSpec.IntValue splintDurabilityPer50Steps = serverBuilder
                .comment("Durability consumed per 50 steps walked with active splint")
                .defineInRange("splintDurabilityPer50Steps", 1, 0, 100);
        serverBuilder.pop();

        serverBuilder.comment("Bone fracture & trauma mechanics").push("fracture");
        ForgeConfigSpec.IntValue boneFractureDuration = serverBuilder
                .comment("Base natural recovery duration in ticks (6000 ticks = 5 minutes)")
                .defineInRange("boneFractureDurationTicks", 6000, 100, 72000);
        ForgeConfigSpec.DoubleValue boneFractureCrouchMultiplier = serverBuilder
                .comment("Movement speed multiplier when crouching with a fractured leg")
                .defineInRange("boneFractureCrouchMultiplier", 0.5, 0.0, 1.0);
        ForgeConfigSpec.BooleanValue enableTotemBypass = serverBuilder
                .comment("Enable trauma totem bypass on lethal fall / blunt trauma")
                .define("enableTotemBypass", true);
        ForgeConfigSpec.BooleanValue enableGForceBlackout = serverBuilder
                .comment("Enable high G-force blackout on tight turns during high-speed elytra flight")
                .define("enableGForceBlackout", true);
        ForgeConfigSpec.BooleanValue enableKineticCrash = serverBuilder
                .comment("Enable kinetic crash trauma when impacting blocks at high speed with elytra")
                .define("enableKineticCrash", true);
        serverBuilder.pop();

        serverBuilder.comment("Physiology & sleep deprivation mechanics").push("physiology");
        ForgeConfigSpec.BooleanValue enableSleepDeprivation = serverBuilder
                .comment("Enable sleep deprivation and fatigue buildup")
                .define("enableSleepDeprivation", true);
        ForgeConfigSpec.IntValue sleepDeprivationFullFatigueTicks = serverBuilder
                .comment("Ticks of being awake required to reach 100% fatigue (72000 ticks = 3 in-game days)")
                .defineInRange("sleepDeprivationFullFatigueTicks", 72000, 20, 720000);
        ForgeConfigSpec.DoubleValue sleepFatigueBlinkThreshold = serverBuilder
                .comment("Fatigue score threshold (0.0 to 1.0) above which darkness blinks begin occurring")
                .defineInRange("sleepFatigueBlinkThreshold", 0.80, 0.0, 1.0);
        ForgeConfigSpec.IntValue sleepFatigueBlinkMinInterval = serverBuilder
                .comment("Minimum interval in ticks between darkness blinks (20 ticks = 1s)")
                .defineInRange("sleepFatigueBlinkMinIntervalTicks", 300, 20, 72000);
        ForgeConfigSpec.IntValue sleepFatigueBlinkMaxInterval = serverBuilder
                .comment("Maximum interval in ticks between darkness blinks")
                .defineInRange("sleepFatigueBlinkMaxIntervalTicks", 800, 40, 72000);

        ForgeConfigSpec.BooleanValue enableToxinBuildup = serverBuilder
                .comment("Enable soulslike bad effect (poison/wither) buildup and transient gauge")
                .define("enableToxinBuildup", true);
        ForgeConfigSpec.DoubleValue toxinBaseBuildupPerHit = serverBuilder
                .comment("Base minimum toxin buildup percentage added per exposure")
                .defineInRange("toxinBaseBuildupPerHit", 25.0, 1.0, 100.0);
        ForgeConfigSpec.IntValue toxinDecayDelayTicks = serverBuilder
                .comment("Ticks of delay without exposure before toxin buildup begins decaying (80 ticks = 4s)")
                .defineInRange("toxinDecayDelayTicks", 80, 1, 72000);
        ForgeConfigSpec.DoubleValue toxinDecayRatePerSecond = serverBuilder
                .comment("Passive percentage decay of toxin buildup per second")
                .defineInRange("toxinDecayRatePerSecond", 2.0, 0.1, 100.0);
        ForgeConfigSpec.DoubleValue toxinRestingHeatDecayMultiplier = serverBuilder
                .comment("Toxin decay rate multiplier when near heat sources (campfires, lava) or resting")
                .defineInRange("toxinRestingHeatDecayMultiplier", 3.0, 1.0, 20.0);
        ForgeConfigSpec.BooleanValue enableToxinFatigueScaling = serverBuilder
                .comment("Whether sleep deprivation and fatigue score increase toxin buildup accumulation rate")
                .define("enableToxinFatigueScaling", true);
        ForgeConfigSpec.DoubleValue toxinFractureBuildupMultiplier = serverBuilder
                .comment("Toxin buildup multiplier when suffering from bone fractures or active trauma")
                .defineInRange("toxinFractureBuildupMultiplier", 1.5, 1.0, 5.0);

        ForgeConfigSpec.BooleanValue enablePhysicalExertion = serverBuilder
                .comment("Enable physical exertion and muscle failure")
                .define("enablePhysicalExertion", true);
        ForgeConfigSpec.IntValue exertionContinuousMiningTicks = serverBuilder
                .comment("Ticks of continuous chopping or mining required to trigger muscle failure (200 ticks = 10s)")
                .defineInRange("exertionContinuousMiningTicks", 200, 20, 72000);
        ForgeConfigSpec.IntValue exertionRestRecoveryTicks = serverBuilder
                .comment("Ticks standing still without actions to recover 100% muscle capacity (70 ticks = 3.5s)")
                .defineInRange("exertionRestRecoveryTicks", 70, 10, 1200);
        ForgeConfigSpec.DoubleValue exertionAttackSpeedPenalty = serverBuilder
                .comment("Attack speed penalty fraction during muscle failure (0.50 = -50%)")
                .defineInRange("exertionAttackSpeedPenalty", 0.50, 0.0, 1.0);
        ForgeConfigSpec.DoubleValue exertionMiningSpeedPenalty = serverBuilder
                .comment("Mining break speed penalty fraction during muscle failure (0.50 = -50%)")
                .defineInRange("exertionMiningSpeedPenalty", 0.50, 0.0, 1.0);
        ForgeConfigSpec.DoubleValue exertionSprintDrainPerTick = serverBuilder
                .comment("Stamina / exertion drain per tick while sprinting (0.08 = ~1.6% per sec, ~60s to deplete)")
                .defineInRange("exertionSprintDrainPerTick", 0.08, 0.001, 10.0);
        ForgeConfigSpec.BooleanValue enableSwimmingCramps = serverBuilder
                .comment("Enable temporary swimming cramps from prolonged continuous swimming/diving")
                .define("enableSwimmingCramps", true);
        ForgeConfigSpec.IntValue swimmingCrampThresholdTicks = serverBuilder
                .comment("Ticks of continuous swimming before leg cramp occurs (600 ticks = 30s)")
                .defineInRange("swimmingCrampThresholdTicks", 600, 40, 72000);
        ForgeConfigSpec.DoubleValue swimmingCrampSpeedPenalty = serverBuilder
                .comment("Swimming speed penalty fraction during cramp (0.40 = -40%)")
                .defineInRange("swimmingCrampSpeedPenalty", 0.40, 0.0, 1.0);
        ForgeConfigSpec.IntValue swimmingCrampRecoveryTicks = serverBuilder
                .comment("Ticks resting on land or boat to recover from leg cramp (60 ticks = 3s)")
                .defineInRange("swimmingCrampRecoveryTicks", 60, 10, 1200);

        ForgeConfigSpec.BooleanValue enableDigestionWindow = serverBuilder
                .comment("Enable gradual digestion absorption window for eaten food")
                .define("enableDigestionWindow", true);
        ForgeConfigSpec.IntValue digestionIntervalTicks = serverBuilder
                .comment("Interval in ticks between digestion nutrition absorption (30 ticks = 1.5s)")
                .defineInRange("digestionIntervalTicks", 30, 5, 200);
        ForgeConfigSpec.BooleanValue enableOvereatingReflex = serverBuilder
                .comment("Enable overeating hiccups, speed debuff, and vomiting reflex when gorging food")
                .define("enableOvereatingReflex", true);
        ForgeConfigSpec.BooleanValue enableIndigestion = serverBuilder
                .comment("Enable indigestion accumulation and stomach ache from raw/rotten meat")
                .define("enableIndigestion", true);
        ForgeConfigSpec.BooleanValue enableCalciumHealing = serverBuilder
                .comment("Enable calcium absorption from bone broth and milk accelerating bone healing")
                .define("enableCalciumHealing", true);
        ForgeConfigSpec.DoubleValue calciumHealingMultiplier = serverBuilder
                .comment("Healing rate multiplier for Tier 1 fractures with active calcium buff")
                .defineInRange("calciumHealingMultiplier", 2.0, 1.0, 5.0);
        ForgeConfigSpec.BooleanValue enableClaustrophobiaPanic = serverBuilder
                .comment("Enable underground claustrophobia panic, heart rate spike, and aim tremor in dark caves (Y < 0, 0 lux)")
                .define("enableClaustrophobiaPanic", true);
        ForgeConfigSpec.IntValue panicThresholdTicks = serverBuilder
                .comment("Ticks in pitch dark deep underground before panic triggers (100 ticks = 5s)")
                .defineInRange("panicThresholdTicks", 100, 20, 1200);
        ForgeConfigSpec.BooleanValue enableAdrenalineSurge = serverBuilder
                .comment("Enable 4-second adrenaline surge burst upon severe bone fracture")
                .define("enableAdrenalineSurge", true);
        ForgeConfigSpec.IntValue adrenalineSurgeDurationTicks = serverBuilder
                .comment("Duration in ticks of the adrenaline surge (80 ticks = 4.0s)")
                .defineInRange("adrenalineSurgeDurationTicks", 80, 20, 400);
        serverBuilder.pop();

        serverBuilder.comment("Temperature & hypothermia mechanics").push("temperature");
        ForgeConfigSpec.BooleanValue enableWetnessAccumulation = serverBuilder
                .comment("Enable rain soaking and water wetness accumulation")
                .define("enableWetnessAccumulation", true);
        ForgeConfigSpec.DoubleValue wetnessRainAccumulationRate = serverBuilder
                .comment("Base wetness accumulation per tick when soaking under rain")
                .defineInRange("wetnessRainAccumulationRate", 0.0025, 0.0001, 1.0);
        ForgeConfigSpec.DoubleValue wetnessWaterAccumulationRate = serverBuilder
                .comment("Wetness accumulation per tick when submerged in water")
                .defineInRange("wetnessWaterAccumulationRate", 0.015, 0.0001, 1.0);
        ForgeConfigSpec.DoubleValue wetnessNoHelmetMultiplier = serverBuilder
                .comment("Multiplier applied to rain soaking rate when no helmet is equipped")
                .defineInRange("wetnessNoHelmetMultiplier", 2.0, 1.0, 10.0);
        ForgeConfigSpec.DoubleValue wetnessFreezeImmuneReduction = serverBuilder
                .comment("Wetness accumulation reduction when wearing freeze-immune wearables (leather armor)")
                .defineInRange("wetnessFreezeImmuneReduction", 0.65, 0.0, 1.0);
        ForgeConfigSpec.BooleanValue enableHeatRewarming = serverBuilder
                .comment("Enable heat source detection, drying, and rapid thawing")
                .define("enableHeatRewarming", true);
        ForgeConfigSpec.DoubleValue heatSourceRadius = serverBuilder
                .comment("Radius in blocks to detect heat sources (campfires, lit furnaces, lava, fire)")
                .defineInRange("heatSourceRadius", 3.0, 1.0, 10.0);
        ForgeConfigSpec.DoubleValue heatDryingTimeSeconds = serverBuilder
                .comment("Time in seconds to completely dry full wetness near heat sources")
                .defineInRange("heatDryingTimeSeconds", 12.5, 1.0, 300.0);
        ForgeConfigSpec.DoubleValue heatThawMultiplier = serverBuilder
                .comment("Multiplier for thawing frozen ticks when near heat sources")
                .defineInRange("heatThawMultiplier", 5.0, 1.0, 20.0);
        ForgeConfigSpec.BooleanValue enableHypothermia = serverBuilder
                .comment("Enable hypothermia freeze ticks, screen frost, and stumbling when fully soaked")
                .define("enableHypothermia", true);
        ForgeConfigSpec.IntValue internalWarmingDurationTicks = serverBuilder
                .comment("Duration in ticks of internal warming buff granted by hot soups/stews (1600 ticks = 80s)")
                .defineInRange("internalWarmingDurationTicks", 1600, 20, 72000);
        ForgeConfigSpec.DoubleValue hypothermiaPenaltyThreshold = serverBuilder
                .comment("Fraction of required freeze ticks to trigger hypothermia physical penalties and shivering (0.60 = 60%)")
                .defineInRange("hypothermiaPenaltyThreshold", 0.60, 0.1, 1.0);
        ForgeConfigSpec.BooleanValue hypothermiaMiningFatigue = serverBuilder
                .comment("Enable Mining Fatigue I debuff when suffering from severe hypothermia")
                .define("hypothermiaMiningFatigue", true);
        ForgeConfigSpec.DoubleValue hypothermiaAttackSpeedPenalty = serverBuilder
                .comment("Attack speed penalty fraction during hypothermia (0.35 = -35%)")
                .defineInRange("hypothermiaAttackSpeedPenalty", 0.35, 0.0, 1.0);
        ForgeConfigSpec.DoubleValue hypothermiaInteractionRangePenalty = serverBuilder
                .comment("Block interaction range penalty in blocks during hypothermia (1.0 = -1.0 block)")
                .defineInRange("hypothermiaInteractionRangePenalty", 1.0, 0.0, 5.0);
        serverBuilder.pop();

        SERVER_SPEC = serverBuilder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();

        clientBuilder.comment("HUD overlay settings").push("hud");
        ForgeConfigSpec.BooleanValue enableHudOverlay = clientBuilder
                .comment("Show injury & trauma status HUD overlay on screen")
                .define("enableHudOverlay", true);
        ForgeConfigSpec.IntValue hudX = clientBuilder
                .comment("Screen X position of the injury HUD overlay")
                .defineInRange("hudX", 10, 0, 10000);
        ForgeConfigSpec.IntValue hudY = clientBuilder
                .comment("Screen Y position of the injury HUD overlay")
                .defineInRange("hudY", 10, 0, 10000);
        ForgeConfigSpec.BooleanValue enableToxinHudOverlay = clientBuilder
                .comment("Show transient soulslike toxin buildup bar above hotbar")
                .define("enableToxinHudOverlay", true);
        ForgeConfigSpec.BooleanValue enableStaminaHudOverlay = clientBuilder
                .comment("Show transient stamina / exertion bar above hotbar")
                .define("enableStaminaHudOverlay", true);
        ForgeConfigSpec.BooleanValue enableSwappableHud = clientBuilder
                .comment("Enable swappable HUD module cycling")
                .define("enableSwappableHud", true);
        ForgeConfigSpec.ConfigValue<String> defaultHudMode = clientBuilder
                .comment("Default HUD mode on launch (OFF, PHYSIOLOGY, ENVIRONMENT, INJURY, COMBINED)")
                .define("defaultHudMode", "COMBINED");
        ForgeConfigSpec.IntValue swappableHudX = clientBuilder
                .comment("Screen X position of the swappable HUD mini box")
                .defineInRange("swappableHudX", 10, 0, 10000);
        ForgeConfigSpec.IntValue swappableHudY = clientBuilder
                .comment("Screen Y position of the swappable HUD mini box")
                .defineInRange("swappableHudY", 10, 0, 10000);
        ForgeConfigSpec.BooleanValue showMetabolicDetails = clientBuilder
                .comment("Show detailed rows for digestion buffer, calcium bone healing, and cramps")
                .define("showMetabolicDetails", true);
        ForgeConfigSpec.BooleanValue showCardiacHud = clientBuilder
                .comment("Show real-time cardiac heart rate BPM on the HUD overlay")
                .define("showCardiacHud", true);
        ForgeConfigSpec.BooleanValue showActionBarEnvironmentCues = clientBuilder
                .comment("Show concise environmental cues on the action bar when HUD mode is ENVIRONMENT")
                .define("showActionBarEnvironmentCues", true);
        clientBuilder.pop();

        clientBuilder.comment("Camera and visual effects").push("camera");
        ForgeConfigSpec.BooleanValue enableCameraLimping = clientBuilder
                .comment("Enable camera wobble/tilt effect when walking with fractured leg(s)")
                .define("enableCameraLimping", true);
        ForgeConfigSpec.BooleanValue enableShiveringCamera = clientBuilder
                .comment("Enable subtle shivering screen tremor when suffering from hypothermia")
                .define("enableShiveringCamera", true);
        clientBuilder.pop();

        CLIENT_SPEC = clientBuilder.build();

        ServerConfig.ENABLE_INJURY = safe(SERVER_SPEC, enableInjury, true);
        ServerConfig.ENABLE_TEMPERATURE = safe(SERVER_SPEC, enableTemperature, true);
        ServerConfig.ENABLE_WEIGHT = safe(SERVER_SPEC, enableWeight, true);
        ServerConfig.ENABLE_PHYSIOLOGY = safe(SERVER_SPEC, enablePhysiology, true);
        ServerConfig.ENABLE_SPLINT = safe(SERVER_SPEC, enableSplint, true);
        ServerConfig.SPLINT_SPEED_REDUCTION = safe(SERVER_SPEC, splintSpeedReduction, 0.10);
        ServerConfig.SPLINT_DURABILITY_PER_50_STEPS = safe(SERVER_SPEC, splintDurabilityPer50Steps, 1);
        ServerConfig.BONE_FRACTURE_DURATION = safe(SERVER_SPEC, boneFractureDuration, 6000);
        ServerConfig.BONE_FRACTURE_CROUCH_MULTIPLIER = safe(SERVER_SPEC, boneFractureCrouchMultiplier, 0.5);
        ServerConfig.ENABLE_TOTEM_BYPASS = safe(SERVER_SPEC, enableTotemBypass, true);
        ServerConfig.ENABLE_GFORCE_BLACKOUT = safe(SERVER_SPEC, enableGForceBlackout, true);
        ServerConfig.ENABLE_KINETIC_CRASH = safe(SERVER_SPEC, enableKineticCrash, true);
        ServerConfig.ENABLE_SLEEP_DEPRIVATION = safe(SERVER_SPEC, enableSleepDeprivation, true);
        ServerConfig.SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS = safe(SERVER_SPEC, sleepDeprivationFullFatigueTicks, 72000);
        ServerConfig.SLEEP_FATIGUE_BLINK_THRESHOLD = safe(SERVER_SPEC, sleepFatigueBlinkThreshold, 0.80);
        ServerConfig.SLEEP_FATIGUE_BLINK_MIN_INTERVAL = safe(SERVER_SPEC, sleepFatigueBlinkMinInterval, 300);
        ServerConfig.SLEEP_FATIGUE_BLINK_MAX_INTERVAL = safe(SERVER_SPEC, sleepFatigueBlinkMaxInterval, 800);
        ServerConfig.ENABLE_TOXIN_BUILDUP = safe(SERVER_SPEC, enableToxinBuildup, true);
        ServerConfig.TOXIN_BASE_BUILDUP_PER_HIT = safe(SERVER_SPEC, toxinBaseBuildupPerHit, 25.0);
        ServerConfig.TOXIN_DECAY_DELAY_TICKS = safe(SERVER_SPEC, toxinDecayDelayTicks, 80);
        ServerConfig.TOXIN_DECAY_RATE_PER_SECOND = safe(SERVER_SPEC, toxinDecayRatePerSecond, 2.0);
        ServerConfig.TOXIN_RESTING_HEAT_DECAY_MULTIPLIER = safe(SERVER_SPEC, toxinRestingHeatDecayMultiplier, 3.0);
        ServerConfig.ENABLE_TOXIN_FATIGUE_SCALING = safe(SERVER_SPEC, enableToxinFatigueScaling, true);
        ServerConfig.TOXIN_FRACTURE_BUILDUP_MULTIPLIER = safe(SERVER_SPEC, toxinFractureBuildupMultiplier, 1.5);
        ServerConfig.ENABLE_PHYSICAL_EXERTION = safe(SERVER_SPEC, enablePhysicalExertion, true);
        ServerConfig.EXERTION_CONTINUOUS_MINING_TICKS = safe(SERVER_SPEC, exertionContinuousMiningTicks, 200);
        ServerConfig.EXERTION_REST_RECOVERY_TICKS = safe(SERVER_SPEC, exertionRestRecoveryTicks, 70);
        ServerConfig.EXERTION_ATTACK_SPEED_PENALTY = safe(SERVER_SPEC, exertionAttackSpeedPenalty, 0.50);
        ServerConfig.EXERTION_MINING_SPEED_PENALTY = safe(SERVER_SPEC, exertionMiningSpeedPenalty, 0.50);
        ServerConfig.EXERTION_SPRINT_DRAIN_PER_TICK = safe(SERVER_SPEC, exertionSprintDrainPerTick, 0.08);
        ServerConfig.ENABLE_SWIMMING_CRAMPS = safe(SERVER_SPEC, enableSwimmingCramps, true);
        ServerConfig.SWIMMING_CRAMP_THRESHOLD_TICKS = safe(SERVER_SPEC, swimmingCrampThresholdTicks, 600);
        ServerConfig.SWIMMING_CRAMP_SPEED_PENALTY = safe(SERVER_SPEC, swimmingCrampSpeedPenalty, 0.40);
        ServerConfig.SWIMMING_CRAMP_RECOVERY_TICKS = safe(SERVER_SPEC, swimmingCrampRecoveryTicks, 60);

        ServerConfig.ENABLE_DIGESTION_WINDOW = safe(SERVER_SPEC, enableDigestionWindow, true);
        ServerConfig.DIGESTION_INTERVAL_TICKS = safe(SERVER_SPEC, digestionIntervalTicks, 30);
        ServerConfig.ENABLE_OVEREATING_REFLEX = safe(SERVER_SPEC, enableOvereatingReflex, true);
        ServerConfig.ENABLE_INDIGESTION = safe(SERVER_SPEC, enableIndigestion, true);
        ServerConfig.ENABLE_CALCIUM_HEALING = safe(SERVER_SPEC, enableCalciumHealing, true);
        ServerConfig.CALCIUM_HEALING_MULTIPLIER = safe(SERVER_SPEC, calciumHealingMultiplier, 2.0);
        ServerConfig.ENABLE_CLAUSTROPHOBIA_PANIC = safe(SERVER_SPEC, enableClaustrophobiaPanic, true);
        ServerConfig.PANIC_THRESHOLD_TICKS = safe(SERVER_SPEC, panicThresholdTicks, 100);
        ServerConfig.ENABLE_ADRENALINE_SURGE = safe(SERVER_SPEC, enableAdrenalineSurge, true);
        ServerConfig.ADRENALINE_SURGE_DURATION_TICKS = safe(SERVER_SPEC, adrenalineSurgeDurationTicks, 80);

        ServerConfig.ENABLE_WETNESS_ACCUMULATION = safe(SERVER_SPEC, enableWetnessAccumulation, true);
        ServerConfig.WETNESS_RAIN_ACCUMULATION_RATE = safe(SERVER_SPEC, wetnessRainAccumulationRate, 0.0025);
        ServerConfig.WETNESS_WATER_ACCUMULATION_RATE = safe(SERVER_SPEC, wetnessWaterAccumulationRate, 0.015);
        ServerConfig.WETNESS_NO_HELMET_MULTIPLIER = safe(SERVER_SPEC, wetnessNoHelmetMultiplier, 2.0);
        ServerConfig.WETNESS_FREEZE_IMMUNE_REDUCTION = safe(SERVER_SPEC, wetnessFreezeImmuneReduction, 0.65);
        ServerConfig.ENABLE_HEAT_REWARMING = safe(SERVER_SPEC, enableHeatRewarming, true);
        ServerConfig.HEAT_SOURCE_RADIUS = safe(SERVER_SPEC, heatSourceRadius, 3.0);
        ServerConfig.HEAT_DRYING_TIME_SECONDS = safe(SERVER_SPEC, heatDryingTimeSeconds, 12.5);
        ServerConfig.HEAT_THAW_MULTIPLIER = safe(SERVER_SPEC, heatThawMultiplier, 5.0);
        ServerConfig.ENABLE_HYPOTHERMIA = safe(SERVER_SPEC, enableHypothermia, true);
        ServerConfig.INTERNAL_WARMING_DURATION_TICKS = safe(SERVER_SPEC, internalWarmingDurationTicks, 1600);
        ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD = safe(SERVER_SPEC, hypothermiaPenaltyThreshold, 0.60);
        ServerConfig.HYPOTHERMIA_MINING_FATIGUE = safe(SERVER_SPEC, hypothermiaMiningFatigue, true);
        ServerConfig.HYPOTHERMIA_ATTACK_SPEED_PENALTY = safe(SERVER_SPEC, hypothermiaAttackSpeedPenalty, 0.35);
        ServerConfig.HYPOTHERMIA_INTERACTION_RANGE_PENALTY = safe(SERVER_SPEC, hypothermiaInteractionRangePenalty, 1.0);

        ClientConfig.ENABLE_HUD_OVERLAY = safe(CLIENT_SPEC, enableHudOverlay, true);
        ClientConfig.HUD_X = safe(CLIENT_SPEC, hudX, 10);
        ClientConfig.HUD_Y = safe(CLIENT_SPEC, hudY, 10);
        ClientConfig.ENABLE_TOXIN_HUD_OVERLAY = safe(CLIENT_SPEC, enableToxinHudOverlay, true);
        ClientConfig.ENABLE_STAMINA_HUD_OVERLAY = safe(CLIENT_SPEC, enableStaminaHudOverlay, true);
        ClientConfig.ENABLE_SWAPPABLE_HUD = safe(CLIENT_SPEC, enableSwappableHud, true);
        ClientConfig.DEFAULT_HUD_MODE = safe(CLIENT_SPEC, defaultHudMode, "COMBINED");
        ClientConfig.SWAPPABLE_HUD_X = safe(CLIENT_SPEC, swappableHudX, 10);
        ClientConfig.SWAPPABLE_HUD_Y = safe(CLIENT_SPEC, swappableHudY, 10);
        ClientConfig.SHOW_METABOLIC_DETAILS = safe(CLIENT_SPEC, showMetabolicDetails, true);
        ClientConfig.SHOW_CARDIAC_HUD = safe(CLIENT_SPEC, showCardiacHud, true);
        ClientConfig.ENABLE_CAMERA_LIMPING = safe(CLIENT_SPEC, enableCameraLimping, true);
        ClientConfig.ENABLE_SHIVERING_CAMERA = safe(CLIENT_SPEC, enableShiveringCamera, true);
        ClientConfig.SHOW_ACTIONBAR_ENVIRONMENT_CUES = safe(CLIENT_SPEC, showActionBarEnvironmentCues, true);
    }

    private static <T> Supplier<T> safe(ForgeConfigSpec spec, ForgeConfigSpec.ConfigValue<T> value, T fallback) {
        return () -> spec.isLoaded() ? value.get() : fallback;
    }

    public static void register(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, SERVER_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}

