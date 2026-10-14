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
        ClientConfig.ENABLE_CAMERA_LIMPING = enableCameraLimping::get;
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
