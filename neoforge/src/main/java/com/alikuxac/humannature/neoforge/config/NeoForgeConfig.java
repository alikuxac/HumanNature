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

        serverBuilder.comment("Master module toggles for Human Nature").push("general");
        ModConfigSpec.BooleanValue enableInjury = serverBuilder
                .comment("Master toggle for the injury and trauma module")
                .define("enableInjury", true);
        ModConfigSpec.BooleanValue enableTemperature = serverBuilder
                .comment("Master toggle for the body temperature and climate module")
                .define("enableTemperature", true);
        ModConfigSpec.BooleanValue enableWeight = serverBuilder
                .comment("Master toggle for the inventory weight and encumbrance module")
                .define("enableWeight", true);
        ModConfigSpec.BooleanValue enablePhysiology = serverBuilder
                .comment("Master toggle for the physiology module (sleep deprivation, stamina, toxin buildup)")
                .define("enablePhysiology", true);
        serverBuilder.pop();

        serverBuilder.comment("Injury and limb trauma mechanics").push("injury");
        ModConfigSpec.BooleanValue enableSplint = serverBuilder
                .comment("Enable splint mechanics and limb stabilization")
                .define("enableSplint", true);
        ModConfigSpec.IntValue boneFractureDurationTicks = serverBuilder
                .comment("Base natural recovery duration for bone fractures in ticks (6000 ticks = 5 minutes)")
                .defineInRange("boneFractureDurationTicks", 6000, 100, 72000);
        ModConfigSpec.BooleanValue enableTotemBypass = serverBuilder
                .comment("Enable trauma totem bypass on lethal fall or blunt trauma")
                .define("enableTotemBypass", true);
        ModConfigSpec.BooleanValue enableGForceBlackout = serverBuilder
                .comment("Enable high G-force blackout on sharp turns during high-speed elytra flight")
                .define("enableGForceBlackout", true);
        ModConfigSpec.BooleanValue enableKineticCrash = serverBuilder
                .comment("Enable kinetic crash trauma when impacting blocks at high speed with elytra")
                .define("enableKineticCrash", true);
        serverBuilder.pop();

        serverBuilder.comment("Body temperature, wetness, and climate mechanics").push("temperature");
        ModConfigSpec.DoubleValue temperatureChangeRateMultiplier = serverBuilder
                .comment("Global multiplier for rate of temperature change, wetness accumulation, and heating/cooling rates")
                .defineInRange("temperatureChangeRateMultiplier", 1.0, 0.1, 10.0);
        ModConfigSpec.DoubleValue hypothermiaThreshold = serverBuilder
                .comment("Fraction of freeze ticks required to trigger hypothermia physical penalties and shivering (0.60 = 60%)")
                .defineInRange("hypothermiaThreshold", 0.60, 0.1, 1.0);
        ModConfigSpec.DoubleValue heatExhaustionThreshold = serverBuilder
                .comment("Core body temperature threshold in degrees Celsius triggering heat exhaustion debuffs (panting, slowness)")
                .defineInRange("heatExhaustionThreshold", 39.5, 37.5, 43.0);
        ModConfigSpec.DoubleValue heatDamageThreshold = serverBuilder
                .comment("Core body temperature threshold in degrees Celsius triggering periodic lethal hyperthermia damage")
                .defineInRange("heatDamageThreshold", 42.0, 39.0, 45.0);
        serverBuilder.pop();

        serverBuilder.comment("Inventory weight density and encumbrance mechanics").push("weight");
        ModConfigSpec.DoubleValue maxCarryCapacityKg = serverBuilder
                .comment("Baseline maximum carrying capacity in kilograms before encumbrance penalties apply")
                .defineInRange("maxCarryCapacityKg", 500.0, 10.0, 10000.0);
        ModConfigSpec.DoubleValue weightPenaltyMultiplier = serverBuilder
                .comment("Global multiplier for encumbrance penalties (speed reduction, sinking pull in water, and fall damage)")
                .defineInRange("weightPenaltyMultiplier", 1.0, 0.0, 10.0);
        serverBuilder.pop();

        serverBuilder.comment("Physiology, fatigue, and stamina mechanics").push("physiology");
        ModConfigSpec.BooleanValue enableSleepDeprivation = serverBuilder
                .comment("Enable sleep deprivation and fatigue buildup over time")
                .define("enableSleepDeprivation", true);
        ModConfigSpec.IntValue sleepDeprivationFullFatigueTicks = serverBuilder
                .comment("Ticks of being awake required to reach 100% fatigue (72000 ticks = 3 in-game days)")
                .defineInRange("sleepDeprivationFullFatigueTicks", 72000, 20, 720000);
        ModConfigSpec.BooleanValue enableToxinBuildup = serverBuilder
                .comment("Enable soulslike bad effect (poison/wither) buildup and transient gauge")
                .define("enableToxinBuildup", true);
        ModConfigSpec.BooleanValue enablePhysicalExertion = serverBuilder
                .comment("Enable physical exertion and muscle failure from continuous mining and sprinting")
                .define("enablePhysicalExertion", true);
        serverBuilder.pop();

        SERVER_SPEC = serverBuilder.build();

        ServerConfig.ENABLE_INJURY = enableInjury::get;
        ServerConfig.ENABLE_TEMPERATURE = enableTemperature::get;
        ServerConfig.ENABLE_WEIGHT = enableWeight::get;
        ServerConfig.ENABLE_PHYSIOLOGY = enablePhysiology::get;

        ServerConfig.ENABLE_SPLINT = enableSplint::get;
        ServerConfig.BONE_FRACTURE_DURATION = boneFractureDurationTicks::get;
        ServerConfig.ENABLE_TOTEM_BYPASS = enableTotemBypass::get;
        ServerConfig.ENABLE_GFORCE_BLACKOUT = enableGForceBlackout::get;
        ServerConfig.ENABLE_KINETIC_CRASH = enableKineticCrash::get;

        ServerConfig.TEMPERATURE_CHANGE_RATE_MULTIPLIER = temperatureChangeRateMultiplier::get;
        ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD = hypothermiaThreshold::get;
        ServerConfig.HEAT_STRESS_OVERHEAT_THRESHOLD = heatExhaustionThreshold::get;
        ServerConfig.HEAT_STRESS_DAMAGE_THRESHOLD = heatDamageThreshold::get;

        ServerConfig.WEIGHT_MAX_CAPACITY = maxCarryCapacityKg::get;
        ServerConfig.WEIGHT_PENALTY_MULTIPLIER = weightPenaltyMultiplier::get;

        ServerConfig.ENABLE_SLEEP_DEPRIVATION = enableSleepDeprivation::get;
        ServerConfig.SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS = sleepDeprivationFullFatigueTicks::get;
        ServerConfig.ENABLE_TOXIN_BUILDUP = enableToxinBuildup::get;
        ServerConfig.ENABLE_PHYSICAL_EXERTION = enablePhysicalExertion::get;

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
        ModConfigSpec.BooleanValue enableWeightTooltip = clientBuilder
                .comment("Show individual and stack weight in item tooltips")
                .define("enableWeightTooltip", true);
        ModConfigSpec.BooleanValue enableSwappableHud = clientBuilder
                .comment("Enable swappable HUD module cycling")
                .define("enableSwappableHud", true);
        ModConfigSpec.BooleanValue autoHideHealthyHud = clientBuilder
                .comment("Automatically hide HUD overlays when player is fully healthy")
                .define("autoHideHealthyHud", false);
        ModConfigSpec.ConfigValue<String> defaultHudMode = clientBuilder
                .comment("Default HUD mode on launch (OFF, PHYSIOLOGY, ENVIRONMENT, INJURY, WEIGHT, COMBINED)")
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
        ModConfigSpec.BooleanValue showActionBarEnvironmentCues = clientBuilder
                .comment("Show concise environmental cues on the action bar when HUD mode is ENVIRONMENT")
                .define("showActionBarEnvironmentCues", true);
        ModConfigSpec.BooleanValue showActionBarWeightWarnings = clientBuilder
                .comment("Show action bar warning when transitioning into Overburdened encumbrance")
                .define("showActionBarWeightWarnings", true);
        clientBuilder.pop();

        clientBuilder.comment("Camera and visual effects").push("camera");
        ModConfigSpec.BooleanValue enableCameraLimping = clientBuilder
                .comment("Enable camera wobble/tilt effect when walking with fractured leg(s)")
                .define("enableCameraLimping", true);
        ModConfigSpec.BooleanValue enableShiveringCamera = clientBuilder
                .comment("Enable subtle shivering screen tremor when suffering from hypothermia")
                .define("enableShiveringCamera", true);
        clientBuilder.pop();

        CLIENT_SPEC = clientBuilder.build();

        ClientConfig.ENABLE_HUD_OVERLAY = enableHudOverlay::get;
        ClientConfig.HUD_X = hudX::get;
        ClientConfig.HUD_Y = hudY::get;
        ClientConfig.ENABLE_TOXIN_HUD_OVERLAY = enableToxinHudOverlay::get;
        ClientConfig.ENABLE_STAMINA_HUD_OVERLAY = enableStaminaHudOverlay::get;
        ClientConfig.ENABLE_WEIGHT_TOOLTIP = enableWeightTooltip::get;
        ClientConfig.ENABLE_SWAPPABLE_HUD = enableSwappableHud::get;
        ClientConfig.AUTO_HIDE_HEALTHY_HUD = autoHideHealthyHud::get;
        ClientConfig.DEFAULT_HUD_MODE = defaultHudMode::get;
        ClientConfig.SWAPPABLE_HUD_X = swappableHudX::get;
        ClientConfig.SWAPPABLE_HUD_Y = swappableHudY::get;
        ClientConfig.SHOW_METABOLIC_DETAILS = showMetabolicDetails::get;
        ClientConfig.SHOW_CARDIAC_HUD = showCardiacHud::get;
        ClientConfig.SHOW_ACTIONBAR_ENVIRONMENT_CUES = showActionBarEnvironmentCues::get;
        ClientConfig.SHOW_ACTIONBAR_WEIGHT_WARNINGS = showActionBarWeightWarnings::get;
        ClientConfig.ENABLE_CAMERA_LIMPING = enableCameraLimping::get;
        ClientConfig.ENABLE_SHIVERING_CAMERA = enableShiveringCamera::get;
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
