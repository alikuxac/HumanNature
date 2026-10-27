package com.alikuxac.humannature.neoforge.config;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgeConfig {

    private static final ModConfigSpec SERVER_SPEC;
    private static final ModConfigSpec CLIENT_SPEC;

    static {
        ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();

        serverBuilder.comment("Module master toggles").push("modules");
        ModConfigSpec.BooleanValue enableInjury = serverBuilder
                .comment("Enable injury & trauma module")
                .define("enableInjury", true);
        ModConfigSpec.BooleanValue enableTemperature = serverBuilder
                .comment("Enable body temperature module")
                .define("enableTemperature", true);
        ModConfigSpec.BooleanValue enableWeight = serverBuilder
                .comment("Enable inventory weight & encumbrance module")
                .define("enableWeight", true);
        ModConfigSpec.BooleanValue enablePhysiology = serverBuilder
                .comment("Enable physiology module")
                .define("enablePhysiology", true);
        serverBuilder.pop();

        serverBuilder.comment("Splint & limb stabilization mechanics").push("splint");
        ModConfigSpec.BooleanValue enableSplint = serverBuilder
                .comment("Enable splint mechanics")
                .define("enableSplint", true);
        ModConfigSpec.DoubleValue splintSpeedReduction = serverBuilder
                .comment("Movement speed penalty when wearing a splint")
                .defineInRange("splintSpeedReduction", 0.10, 0.0, 1.0);
        ModConfigSpec.IntValue splintDurabilityPer50Steps = serverBuilder
                .comment("Durability consumed per 50 steps walked with active splint")
                .defineInRange("splintDurabilityPer50Steps", 1, 0, 100);
        serverBuilder.pop();

        serverBuilder.comment("Bone fracture & trauma mechanics").push("fracture");
        ModConfigSpec.IntValue boneFractureDuration = serverBuilder
                .comment("Base natural recovery duration in ticks (6000 ticks = 5 minutes)")
                .defineInRange("boneFractureDurationTicks", 6000, 100, 72000);
        ModConfigSpec.DoubleValue boneFractureCrouchMultiplier = serverBuilder
                .comment("Movement speed multiplier when crouching with a fractured leg")
                .defineInRange("boneFractureCrouchMultiplier", 0.5, 0.0, 1.0);
        ModConfigSpec.BooleanValue enableTotemBypass = serverBuilder
                .comment("Enable trauma totem bypass on lethal fall / blunt trauma")
                .define("enableTotemBypass", true);
        ModConfigSpec.BooleanValue enableGForceBlackout = serverBuilder
                .comment("Enable high G-force blackout on tight turns during high-speed elytra flight")
                .define("enableGForceBlackout", true);
        ModConfigSpec.BooleanValue enableKineticCrash = serverBuilder
                .comment("Enable kinetic crash trauma when impacting blocks at high speed with elytra")
                .define("enableKineticCrash", true);
        serverBuilder.pop();

        serverBuilder.comment("Physiology & sleep deprivation mechanics").push("physiology");
        ModConfigSpec.BooleanValue enableSleepDeprivation = serverBuilder
                .comment("Enable sleep deprivation and fatigue buildup")
                .define("enableSleepDeprivation", true);
        ModConfigSpec.IntValue sleepDeprivationFullFatigueTicks = serverBuilder
                .comment("Ticks of being awake required to reach 100% fatigue (72000 ticks = 3 in-game days)")
                .defineInRange("sleepDeprivationFullFatigueTicks", 72000, 20, 720000);
        ModConfigSpec.DoubleValue sleepFatigueBlinkThreshold = serverBuilder
                .comment("Fatigue score threshold (0.0 to 1.0) above which darkness blinks begin occurring")
                .defineInRange("sleepFatigueBlinkThreshold", 0.80, 0.0, 1.0);
        ModConfigSpec.IntValue sleepFatigueBlinkMinInterval = serverBuilder
                .comment("Minimum interval in ticks between darkness blinks (20 ticks = 1s)")
                .defineInRange("sleepFatigueBlinkMinIntervalTicks", 300, 20, 72000);
        ModConfigSpec.IntValue sleepFatigueBlinkMaxInterval = serverBuilder
                .comment("Maximum interval in ticks between darkness blinks")
                .defineInRange("sleepFatigueBlinkMaxIntervalTicks", 800, 40, 72000);

        ModConfigSpec.BooleanValue enableToxinBuildup = serverBuilder
                .comment("Enable soulslike bad effect (poison/wither) buildup and transient gauge")
                .define("enableToxinBuildup", true);
        ModConfigSpec.DoubleValue toxinBaseBuildupPerHit = serverBuilder
                .comment("Base minimum toxin buildup percentage added per exposure")
                .defineInRange("toxinBaseBuildupPerHit", 25.0, 1.0, 100.0);
        ModConfigSpec.IntValue toxinDecayDelayTicks = serverBuilder
                .comment("Ticks of delay without exposure before toxin buildup begins decaying (80 ticks = 4s)")
                .defineInRange("toxinDecayDelayTicks", 80, 1, 72000);
        ModConfigSpec.DoubleValue toxinDecayRatePerSecond = serverBuilder
                .comment("Passive percentage decay of toxin buildup per second")
                .defineInRange("toxinDecayRatePerSecond", 2.0, 0.1, 100.0);
        ModConfigSpec.DoubleValue toxinRestingHeatDecayMultiplier = serverBuilder
                .comment("Toxin decay rate multiplier when near heat sources (campfires, lava) or resting")
                .defineInRange("toxinRestingHeatDecayMultiplier", 3.0, 1.0, 20.0);
        ModConfigSpec.BooleanValue enableToxinFatigueScaling = serverBuilder
                .comment("Whether sleep deprivation and fatigue score increase toxin buildup accumulation rate")
                .define("enableToxinFatigueScaling", true);
        ModConfigSpec.DoubleValue toxinFractureBuildupMultiplier = serverBuilder
                .comment("Toxin buildup multiplier when suffering from bone fractures or active trauma")
                .defineInRange("toxinFractureBuildupMultiplier", 1.5, 1.0, 5.0);

        ModConfigSpec.BooleanValue enablePhysicalExertion = serverBuilder
                .comment("Enable physical exertion and muscle failure")
                .define("enablePhysicalExertion", true);
        ModConfigSpec.IntValue exertionContinuousMiningTicks = serverBuilder
                .comment("Ticks of continuous chopping or mining required to trigger muscle failure (200 ticks = 10s)")
                .defineInRange("exertionContinuousMiningTicks", 200, 20, 72000);
        ModConfigSpec.IntValue exertionRestRecoveryTicks = serverBuilder
                .comment("Ticks standing still without actions to recover 100% muscle capacity (70 ticks = 3.5s)")
                .defineInRange("exertionRestRecoveryTicks", 70, 10, 1200);
        ModConfigSpec.DoubleValue exertionAttackSpeedPenalty = serverBuilder
                .comment("Attack speed penalty fraction during muscle failure (0.50 = -50%)")
                .defineInRange("exertionAttackSpeedPenalty", 0.50, 0.0, 1.0);
        ModConfigSpec.DoubleValue exertionMiningSpeedPenalty = serverBuilder
                .comment("Mining break speed penalty fraction during muscle failure (0.50 = -50%)")
                .defineInRange("exertionMiningSpeedPenalty", 0.50, 0.0, 1.0);
        ModConfigSpec.DoubleValue exertionSprintDrainPerTick = serverBuilder
                .comment("Stamina / exertion drain per tick while sprinting (0.08 = ~1.6% per sec, ~60s to deplete)")
                .defineInRange("exertionSprintDrainPerTick", 0.08, 0.001, 10.0);
        ModConfigSpec.BooleanValue enableSwimmingCramps = serverBuilder
                .comment("Enable temporary swimming cramps from prolonged continuous swimming/diving")
                .define("enableSwimmingCramps", true);
        ModConfigSpec.IntValue swimmingCrampThresholdTicks = serverBuilder
                .comment("Ticks of continuous swimming before leg cramp occurs (600 ticks = 30s)")
                .defineInRange("swimmingCrampThresholdTicks", 600, 40, 72000);
        ModConfigSpec.DoubleValue swimmingCrampSpeedPenalty = serverBuilder
                .comment("Swimming speed penalty fraction during cramp (0.40 = -40%)")
                .defineInRange("swimmingCrampSpeedPenalty", 0.40, 0.0, 1.0);
        ModConfigSpec.IntValue swimmingCrampRecoveryTicks = serverBuilder
                .comment("Ticks resting on land or boat to recover from leg cramp (60 ticks = 3s)")
                .defineInRange("swimmingCrampRecoveryTicks", 60, 10, 1200);

        ModConfigSpec.BooleanValue enableDigestionWindow = serverBuilder
                .comment("Enable gradual digestion absorption window for eaten food")
                .define("enableDigestionWindow", true);
        ModConfigSpec.IntValue digestionIntervalTicks = serverBuilder
                .comment("Interval in ticks between digestion nutrition absorption (30 ticks = 1.5s)")
                .defineInRange("digestionIntervalTicks", 30, 5, 200);
        ModConfigSpec.BooleanValue enableOvereatingReflex = serverBuilder
                .comment("Enable overeating hiccups, speed debuff, and vomiting reflex when gorging food")
                .define("enableOvereatingReflex", true);
        ModConfigSpec.BooleanValue enableIndigestion = serverBuilder
                .comment("Enable indigestion accumulation and stomach ache from raw/rotten meat")
                .define("enableIndigestion", true);
        ModConfigSpec.BooleanValue enableCalciumHealing = serverBuilder
                .comment("Enable calcium absorption from bone broth and milk accelerating bone healing")
                .define("enableCalciumHealing", true);
        ModConfigSpec.DoubleValue calciumHealingMultiplier = serverBuilder
                .comment("Healing rate multiplier for Tier 1 fractures with active calcium buff")
                .defineInRange("calciumHealingMultiplier", 2.0, 1.0, 5.0);
        ModConfigSpec.BooleanValue enableClaustrophobiaPanic = serverBuilder
                .comment("Enable underground claustrophobia panic, heart rate spike, and aim tremor in dark caves (Y < 0, 0 lux)")
                .define("enableClaustrophobiaPanic", true);
        ModConfigSpec.IntValue panicThresholdTicks = serverBuilder
                .comment("Ticks in pitch dark deep underground before panic triggers (100 ticks = 5s)")
                .defineInRange("panicThresholdTicks", 100, 20, 1200);
        ModConfigSpec.BooleanValue enableAdrenalineSurge = serverBuilder
                .comment("Enable 4-second adrenaline surge burst upon severe bone fracture")
                .define("enableAdrenalineSurge", true);
        ModConfigSpec.IntValue adrenalineSurgeDurationTicks = serverBuilder
                .comment("Duration in ticks of the adrenaline surge (80 ticks = 4.0s)")
                .defineInRange("adrenalineSurgeDurationTicks", 80, 20, 400);
        serverBuilder.pop();

        SERVER_SPEC = serverBuilder.build();

        ServerConfig.ENABLE_INJURY = enableInjury::get;
        ServerConfig.ENABLE_TEMPERATURE = enableTemperature::get;
        ServerConfig.ENABLE_WEIGHT = enableWeight::get;
        ServerConfig.ENABLE_PHYSIOLOGY = enablePhysiology::get;
        ServerConfig.ENABLE_SPLINT = enableSplint::get;
        ServerConfig.SPLINT_SPEED_REDUCTION = splintSpeedReduction::get;
        ServerConfig.SPLINT_DURABILITY_PER_50_STEPS = splintDurabilityPer50Steps::get;
        ServerConfig.BONE_FRACTURE_DURATION = boneFractureDuration::get;
        ServerConfig.BONE_FRACTURE_CROUCH_MULTIPLIER = boneFractureCrouchMultiplier::get;
        ServerConfig.ENABLE_TOTEM_BYPASS = enableTotemBypass::get;
        ServerConfig.ENABLE_GFORCE_BLACKOUT = enableGForceBlackout::get;
        ServerConfig.ENABLE_KINETIC_CRASH = enableKineticCrash::get;
        ServerConfig.ENABLE_SLEEP_DEPRIVATION = enableSleepDeprivation::get;
        ServerConfig.SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS = sleepDeprivationFullFatigueTicks::get;
        ServerConfig.SLEEP_FATIGUE_BLINK_THRESHOLD = sleepFatigueBlinkThreshold::get;
        ServerConfig.SLEEP_FATIGUE_BLINK_MIN_INTERVAL = sleepFatigueBlinkMinInterval::get;
        ServerConfig.SLEEP_FATIGUE_BLINK_MAX_INTERVAL = sleepFatigueBlinkMaxInterval::get;
        ServerConfig.ENABLE_TOXIN_BUILDUP = enableToxinBuildup::get;
        ServerConfig.TOXIN_BASE_BUILDUP_PER_HIT = toxinBaseBuildupPerHit::get;
        ServerConfig.TOXIN_DECAY_DELAY_TICKS = toxinDecayDelayTicks::get;
        ServerConfig.TOXIN_DECAY_RATE_PER_SECOND = toxinDecayRatePerSecond::get;
        ServerConfig.TOXIN_RESTING_HEAT_DECAY_MULTIPLIER = toxinRestingHeatDecayMultiplier::get;
        ServerConfig.ENABLE_TOXIN_FATIGUE_SCALING = enableToxinFatigueScaling::get;
        ServerConfig.TOXIN_FRACTURE_BUILDUP_MULTIPLIER = toxinFractureBuildupMultiplier::get;
        ServerConfig.ENABLE_PHYSICAL_EXERTION = enablePhysicalExertion::get;
        ServerConfig.EXERTION_CONTINUOUS_MINING_TICKS = exertionContinuousMiningTicks::get;
        ServerConfig.EXERTION_REST_RECOVERY_TICKS = exertionRestRecoveryTicks::get;
        ServerConfig.EXERTION_ATTACK_SPEED_PENALTY = exertionAttackSpeedPenalty::get;
        ServerConfig.EXERTION_MINING_SPEED_PENALTY = exertionMiningSpeedPenalty::get;
        ServerConfig.EXERTION_SPRINT_DRAIN_PER_TICK = exertionSprintDrainPerTick::get;
        ServerConfig.ENABLE_SWIMMING_CRAMPS = enableSwimmingCramps::get;
        ServerConfig.SWIMMING_CRAMP_THRESHOLD_TICKS = swimmingCrampThresholdTicks::get;
        ServerConfig.SWIMMING_CRAMP_SPEED_PENALTY = swimmingCrampSpeedPenalty::get;
        ServerConfig.SWIMMING_CRAMP_RECOVERY_TICKS = swimmingCrampRecoveryTicks::get;

        ServerConfig.ENABLE_DIGESTION_WINDOW = enableDigestionWindow::get;
        ServerConfig.DIGESTION_INTERVAL_TICKS = digestionIntervalTicks::get;
        ServerConfig.ENABLE_OVEREATING_REFLEX = enableOvereatingReflex::get;
        ServerConfig.ENABLE_INDIGESTION = enableIndigestion::get;
        ServerConfig.ENABLE_CALCIUM_HEALING = enableCalciumHealing::get;
        ServerConfig.CALCIUM_HEALING_MULTIPLIER = calciumHealingMultiplier::get;
        ServerConfig.ENABLE_CLAUSTROPHOBIA_PANIC = enableClaustrophobiaPanic::get;
        ServerConfig.PANIC_THRESHOLD_TICKS = panicThresholdTicks::get;
        ServerConfig.ENABLE_ADRENALINE_SURGE = enableAdrenalineSurge::get;
        ServerConfig.ADRENALINE_SURGE_DURATION_TICKS = adrenalineSurgeDurationTicks::get;

        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();

        clientBuilder.comment("HUD overlay settings").push("hud");
        ModConfigSpec.BooleanValue enableHudOverlay = clientBuilder
                .comment("Show injury & trauma status HUD overlay on screen")
                .define("enableHudOverlay", true);
        ModConfigSpec.IntValue hudX = clientBuilder
                .comment("Screen X position of the injury HUD overlay")
                .defineInRange("hudX", 10, 0, 10000);
        ModConfigSpec.IntValue hudY = clientBuilder
                .comment("Screen Y position of the injury HUD overlay")
                .defineInRange("hudY", 10, 0, 10000);
        ModConfigSpec.BooleanValue enableToxinHudOverlay = clientBuilder
                .comment("Show transient soulslike toxin buildup bar above hotbar")
                .define("enableToxinHudOverlay", true);
        ModConfigSpec.BooleanValue enableStaminaHudOverlay = clientBuilder
                .comment("Show transient stamina / exertion bar above hotbar")
                .define("enableStaminaHudOverlay", true);
        ModConfigSpec.BooleanValue enableSwappableHud = clientBuilder
                .comment("Enable swappable HUD module cycling")
                .define("enableSwappableHud", true);
        ModConfigSpec.ConfigValue<String> defaultHudMode = clientBuilder
                .comment("Default HUD mode on launch (OFF, PHYSIOLOGY, INJURY, COMBINED)")
                .define("defaultHudMode", "COMBINED");
        ModConfigSpec.IntValue swappableHudX = clientBuilder
                .comment("Screen X position of the swappable HUD mini box")
                .defineInRange("swappableHudX", 10, 0, 10000);
        ModConfigSpec.IntValue swappableHudY = clientBuilder
                .comment("Screen Y position of the swappable HUD mini box")
                .defineInRange("swappableHudY", 10, 0, 10000);
        ModConfigSpec.BooleanValue showMetabolicDetails = clientBuilder
                .comment("Show detailed rows for digestion buffer, calcium bone healing, and cramps")
                .define("showMetabolicDetails", true);
        ModConfigSpec.BooleanValue showCardiacHud = clientBuilder
                .comment("Show real-time cardiac heart rate BPM on the HUD overlay")
                .define("showCardiacHud", true);
        clientBuilder.pop();

        clientBuilder.comment("Camera and visual effects").push("camera");
        ModConfigSpec.BooleanValue enableCameraLimping = clientBuilder
                .comment("Enable camera wobble/tilt effect when walking with fractured leg(s)")
                .define("enableCameraLimping", true);
        clientBuilder.pop();

        CLIENT_SPEC = clientBuilder.build();

        ClientConfig.ENABLE_HUD_OVERLAY = enableHudOverlay::get;
        ClientConfig.HUD_X = hudX::get;
        ClientConfig.HUD_Y = hudY::get;
        ClientConfig.ENABLE_TOXIN_HUD_OVERLAY = enableToxinHudOverlay::get;
        ClientConfig.ENABLE_STAMINA_HUD_OVERLAY = enableStaminaHudOverlay::get;
        ClientConfig.ENABLE_SWAPPABLE_HUD = enableSwappableHud::get;
        ClientConfig.DEFAULT_HUD_MODE = defaultHudMode::get;
        ClientConfig.SWAPPABLE_HUD_X = swappableHudX::get;
        ClientConfig.SWAPPABLE_HUD_Y = swappableHudY::get;
        ClientConfig.SHOW_METABOLIC_DETAILS = showMetabolicDetails::get;
        ClientConfig.SHOW_CARDIAC_HUD = showCardiacHud::get;
        ClientConfig.ENABLE_CAMERA_LIMPING = enableCameraLimping::get;
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
