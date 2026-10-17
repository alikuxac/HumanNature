package com.alikuxac.humannature.forge.config;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ForgeConfig {

    private static final ModConfigSpec SERVER_SPEC;
    private static final ModConfigSpec CLIENT_SPEC;

    static {
        // Build Server Config
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
        serverBuilder.pop();

        SERVER_SPEC = serverBuilder.build();

        // Wire ServerConfig suppliers
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

        // Build Client Config
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
        clientBuilder.pop();

        clientBuilder.comment("Camera and visual effects").push("camera");
        ModConfigSpec.BooleanValue enableCameraLimping = clientBuilder
                .comment("Enable camera wobble/tilt effect when walking with fractured leg(s)")
                .define("enableCameraLimping", true);
        clientBuilder.pop();

        CLIENT_SPEC = clientBuilder.build();

        // Wire ClientConfig suppliers
        ClientConfig.ENABLE_HUD_OVERLAY = enableHudOverlay::get;
        ClientConfig.HUD_X = hudX::get;
        ClientConfig.HUD_Y = hudY::get;
        ClientConfig.ENABLE_TOXIN_HUD_OVERLAY = enableToxinHudOverlay::get;
        ClientConfig.ENABLE_CAMERA_LIMPING = enableCameraLimping::get;
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
