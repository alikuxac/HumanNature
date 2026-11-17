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

        serverBuilder.comment("Master module toggles for Human Nature").push("general");
        ForgeConfigSpec.BooleanValue enableInjury = serverBuilder
                .comment("Master toggle for the injury and trauma module")
                .define("enableInjury", true);
        ForgeConfigSpec.BooleanValue enableTemperature = serverBuilder
                .comment("Master toggle for the body temperature and climate module")
                .define("enableTemperature", true);
        ForgeConfigSpec.BooleanValue enableWeight = serverBuilder
                .comment("Master toggle for the inventory weight and encumbrance module")
                .define("enableWeight", true);
        ForgeConfigSpec.BooleanValue enablePhysiology = serverBuilder
                .comment("Master toggle for the physiology module (sleep deprivation, stamina, toxin buildup)")
                .define("enablePhysiology", true);
        serverBuilder.pop();

        serverBuilder.comment("Injury and limb trauma mechanics").push("injury");
        ForgeConfigSpec.BooleanValue enableSplint = serverBuilder
                .comment("Enable splint mechanics and limb stabilization")
                .define("enableSplint", true);
        ForgeConfigSpec.IntValue boneFractureDurationTicks = serverBuilder
                .comment("Base natural recovery duration for bone fractures in ticks (6000 ticks = 5 minutes)")
                .defineInRange("boneFractureDurationTicks", 6000, 100, 72000);
        ForgeConfigSpec.BooleanValue enableTotemBypass = serverBuilder
                .comment("Enable trauma totem bypass on lethal fall or blunt trauma")
                .define("enableTotemBypass", true);
        ForgeConfigSpec.BooleanValue enableGForceBlackout = serverBuilder
                .comment("Enable high G-force blackout on sharp turns during high-speed elytra flight")
                .define("enableGForceBlackout", true);
        ForgeConfigSpec.BooleanValue enableKineticCrash = serverBuilder
                .comment("Enable kinetic crash trauma when impacting blocks at high speed with elytra")
                .define("enableKineticCrash", true);
        serverBuilder.pop();

        serverBuilder.comment("Body temperature, wetness, and climate mechanics").push("temperature");
        ForgeConfigSpec.DoubleValue temperatureChangeRateMultiplier = serverBuilder
                .comment("Global multiplier for rate of temperature change, wetness accumulation, and heating/cooling rates")
                .defineInRange("temperatureChangeRateMultiplier", 1.0, 0.1, 10.0);
        ForgeConfigSpec.DoubleValue hypothermiaThreshold = serverBuilder
                .comment("Fraction of freeze ticks required to trigger hypothermia physical penalties and shivering (0.60 = 60%)")
                .defineInRange("hypothermiaThreshold", 0.60, 0.1, 1.0);
        ForgeConfigSpec.DoubleValue heatExhaustionThreshold = serverBuilder
                .comment("Core body temperature threshold in degrees Celsius triggering heat exhaustion debuffs (panting, slowness)")
                .defineInRange("heatExhaustionThreshold", 39.5, 37.5, 43.0);
        ForgeConfigSpec.DoubleValue heatDamageThreshold = serverBuilder
                .comment("Core body temperature threshold in degrees Celsius triggering periodic lethal hyperthermia damage")
                .defineInRange("heatDamageThreshold", 42.0, 39.0, 45.0);
        serverBuilder.pop();

        serverBuilder.comment("Inventory weight density and encumbrance mechanics").push("weight");
        ForgeConfigSpec.DoubleValue maxCarryCapacityKg = serverBuilder
                .comment("Baseline maximum carrying capacity in kilograms before encumbrance penalties apply")
                .defineInRange("maxCarryCapacityKg", 500.0, 10.0, 10000.0);
        ForgeConfigSpec.DoubleValue weightPenaltyMultiplier = serverBuilder
                .comment("Global multiplier for encumbrance penalties (speed reduction, sinking pull in water, and fall damage)")
                .defineInRange("weightPenaltyMultiplier", 1.0, 0.0, 10.0);
        serverBuilder.pop();

        serverBuilder.comment("Physiology, fatigue, and stamina mechanics").push("physiology");
        ForgeConfigSpec.BooleanValue enableSleepDeprivation = serverBuilder
                .comment("Enable sleep deprivation and fatigue buildup over time")
                .define("enableSleepDeprivation", true);
        ForgeConfigSpec.IntValue sleepDeprivationFullFatigueTicks = serverBuilder
                .comment("Ticks of being awake required to reach 100% fatigue (72000 ticks = 3 in-game days)")
                .defineInRange("sleepDeprivationFullFatigueTicks", 72000, 20, 720000);
        ForgeConfigSpec.BooleanValue enableToxinBuildup = serverBuilder
                .comment("Enable soulslike bad effect (poison/wither) buildup and transient gauge")
                .define("enableToxinBuildup", true);
        ForgeConfigSpec.BooleanValue enablePhysicalExertion = serverBuilder
                .comment("Enable physical exertion and muscle failure from continuous mining and sprinting")
                .define("enablePhysicalExertion", true);
        serverBuilder.pop();

        SERVER_SPEC = serverBuilder.build();

        ServerConfig.ENABLE_INJURY = safe(SERVER_SPEC, enableInjury, true);
        ServerConfig.ENABLE_TEMPERATURE = safe(SERVER_SPEC, enableTemperature, true);
        ServerConfig.ENABLE_WEIGHT = safe(SERVER_SPEC, enableWeight, true);
        ServerConfig.ENABLE_PHYSIOLOGY = safe(SERVER_SPEC, enablePhysiology, true);

        ServerConfig.ENABLE_SPLINT = safe(SERVER_SPEC, enableSplint, true);
        ServerConfig.BONE_FRACTURE_DURATION = safe(SERVER_SPEC, boneFractureDurationTicks, 6000);
        ServerConfig.ENABLE_TOTEM_BYPASS = safe(SERVER_SPEC, enableTotemBypass, true);
        ServerConfig.ENABLE_GFORCE_BLACKOUT = safe(SERVER_SPEC, enableGForceBlackout, true);
        ServerConfig.ENABLE_KINETIC_CRASH = safe(SERVER_SPEC, enableKineticCrash, true);

        ServerConfig.TEMPERATURE_CHANGE_RATE_MULTIPLIER = safe(SERVER_SPEC, temperatureChangeRateMultiplier, 1.0);
        ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD = safe(SERVER_SPEC, hypothermiaThreshold, 0.60);
        ServerConfig.HEAT_STRESS_OVERHEAT_THRESHOLD = safe(SERVER_SPEC, heatExhaustionThreshold, 39.5);
        ServerConfig.HEAT_STRESS_DAMAGE_THRESHOLD = safe(SERVER_SPEC, heatDamageThreshold, 42.0);

        ServerConfig.WEIGHT_MAX_CAPACITY = safe(SERVER_SPEC, maxCarryCapacityKg, 500.0);
        ServerConfig.WEIGHT_PENALTY_MULTIPLIER = safe(SERVER_SPEC, weightPenaltyMultiplier, 1.0);

        ServerConfig.ENABLE_SLEEP_DEPRIVATION = safe(SERVER_SPEC, enableSleepDeprivation, true);
        ServerConfig.SLEEP_DEPRIVATION_FULL_FATIGUE_TICKS = safe(SERVER_SPEC, sleepDeprivationFullFatigueTicks, 72000);
        ServerConfig.ENABLE_TOXIN_BUILDUP = safe(SERVER_SPEC, enableToxinBuildup, true);
        ServerConfig.ENABLE_PHYSICAL_EXERTION = safe(SERVER_SPEC, enablePhysicalExertion, true);

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
        ForgeConfigSpec.BooleanValue enableWeightTooltip = clientBuilder
                .comment("Show individual and stack weight in item tooltips")
                .define("enableWeightTooltip", true);
        ForgeConfigSpec.BooleanValue enableSwappableHud = clientBuilder
                .comment("Enable swappable HUD module cycling")
                .define("enableSwappableHud", true);
        ForgeConfigSpec.BooleanValue autoHideHealthyHud = clientBuilder
                .comment("Automatically hide HUD overlays when player is fully healthy")
                .define("autoHideHealthyHud", false);
        ForgeConfigSpec.ConfigValue<String> defaultHudMode = clientBuilder
                .comment("Default HUD mode on launch (OFF, PHYSIOLOGY, ENVIRONMENT, INJURY, WEIGHT, COMBINED)")
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
        ForgeConfigSpec.BooleanValue showActionBarWeightWarnings = clientBuilder
                .comment("Show action bar warning when transitioning into Overburdened encumbrance")
                .define("showActionBarWeightWarnings", true);
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

        ClientConfig.ENABLE_HUD_OVERLAY = safe(CLIENT_SPEC, enableHudOverlay, true);
        ClientConfig.HUD_X = safe(CLIENT_SPEC, hudX, 10);
        ClientConfig.HUD_Y = safe(CLIENT_SPEC, hudY, 10);
        ClientConfig.ENABLE_TOXIN_HUD_OVERLAY = safe(CLIENT_SPEC, enableToxinHudOverlay, true);
        ClientConfig.ENABLE_STAMINA_HUD_OVERLAY = safe(CLIENT_SPEC, enableStaminaHudOverlay, true);
        ClientConfig.ENABLE_WEIGHT_TOOLTIP = safe(CLIENT_SPEC, enableWeightTooltip, true);
        ClientConfig.ENABLE_SWAPPABLE_HUD = safe(CLIENT_SPEC, enableSwappableHud, true);
        ClientConfig.AUTO_HIDE_HEALTHY_HUD = safe(CLIENT_SPEC, autoHideHealthyHud, false);
        ClientConfig.DEFAULT_HUD_MODE = safe(CLIENT_SPEC, defaultHudMode, "COMBINED");
        ClientConfig.SWAPPABLE_HUD_X = safe(CLIENT_SPEC, swappableHudX, 10);
        ClientConfig.SWAPPABLE_HUD_Y = safe(CLIENT_SPEC, swappableHudY, 10);
        ClientConfig.SHOW_METABOLIC_DETAILS = safe(CLIENT_SPEC, showMetabolicDetails, true);
        ClientConfig.SHOW_CARDIAC_HUD = safe(CLIENT_SPEC, showCardiacHud, true);
        ClientConfig.SHOW_ACTIONBAR_ENVIRONMENT_CUES = safe(CLIENT_SPEC, showActionBarEnvironmentCues, true);
        ClientConfig.SHOW_ACTIONBAR_WEIGHT_WARNINGS = safe(CLIENT_SPEC, showActionBarWeightWarnings, true);
        ClientConfig.ENABLE_CAMERA_LIMPING = safe(CLIENT_SPEC, enableCameraLimping, true);
        ClientConfig.ENABLE_SHIVERING_CAMERA = safe(CLIENT_SPEC, enableShiveringCamera, true);
    }

    private static <T> Supplier<T> safe(ForgeConfigSpec spec, ForgeConfigSpec.ConfigValue<T> value, T fallback) {
        return () -> spec.isLoaded() ? value.get() : fallback;
    }

    public static void register(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, SERVER_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
