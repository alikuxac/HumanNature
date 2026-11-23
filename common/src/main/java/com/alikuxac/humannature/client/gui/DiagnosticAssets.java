package com.alikuxac.humannature.client.gui;

import com.alikuxac.humannature.api.injury.LimbType;
import net.minecraft.resources.ResourceLocation;

public final class DiagnosticAssets {
    private DiagnosticAssets() {}

    public static final int GUI_WIDTH = 176;
    public static final int GUI_HEIGHT = 166;

    public static final int COLOR_FRAME = 0xFF0F172A;
    public static final int COLOR_HIGHLIGHT = 0xFF475569;
    public static final int COLOR_SHADOW = 0xFF1E293B;
    public static final int COLOR_PANEL = 0xFF28303D;
    public static final int COLOR_WELL = 0xFF181D26;
    public static final int COLOR_WELL_LIGHT = 0xFF11151C;
    public static final int COLOR_WELL_DARK = 0xFF3B4659;
    public static final int COLOR_COPPER = 0xFFB87333;
    public static final int COLOR_COPPER_SHADOW = 0xFF6E3F1F;
    public static final int COLOR_OK = 0xFF4ADE80;
    public static final int COLOR_WARN = 0xFFFACC15;
    public static final int COLOR_CRITICAL = 0xFFF87171;

    public static final int WELL_X = 8;
    public static final int WELL_Y = 25;
    public static final int WELL_W = 68;
    public static final int WELL_H = 133;
    public static final int COLUMN_X = 80;
    public static final int COLUMN_Y = 25;
    public static final int COLUMN_W = 88;
    public static final int COLUMN_H = 133;
    public static final int SPRITE_X = WELL_X + 2;
    public static final int SPRITE_Y = WELL_Y + 2;
    public static final int SLOT_X = COLUMN_X + 2;
    public static final int SLOT_Y = COLUMN_Y + 3;
    public static final int SLOT_PITCH = 17;
    public static final int SLOT_COUNT = 6;
    public static final int SLOT_BAR_W = 84;
    public static final int SLOT_BAR_H = 5;
    public static final int SLOT_TEXT_X = SLOT_X + 11;
    public static final int DIVIDER_Y = 133;

    public static final ResourceLocation BACKGROUND = rl("textures/gui/diagnostic_bg.png");
    public static final ResourceLocation HUD_ATLAS = rl("textures/gui/hud_atlas.png");
    public static final ResourceLocation FROST_VIGNETTE = rl("textures/misc/frost_vignette.png");
    public static final ResourceLocation HEAT_VIGNETTE = rl("textures/misc/heat_vignette.png");
    public static final ResourceLocation SPLINT_OVERLAY = rl("textures/gui/splint_overlay.png");

    public record Uv(int u, int v, int width, int height) {}
    public record Rect(int x, int y, int width, int height) {}

    public static final Uv THERMO_COLD = new Uv(0, 0, 9, 9);
    public static final Uv THERMO_NORMAL = new Uv(10, 0, 9, 9);
    public static final Uv THERMO_HOT = new Uv(20, 0, 9, 9);
    public static final Uv LOAD_LIGHT = new Uv(30, 0, 9, 9);
    public static final Uv LOAD_MEDIUM = new Uv(40, 0, 9, 9);
    public static final Uv LOAD_HEAVY = new Uv(50, 0, 9, 9);
    public static final Uv BAR_FRAME = new Uv(0, 10, 64, 5);

    public static final Rect[] LIMB_SPRITES = {
            new Rect(24, 6, 16, 18),
            new Rect(21, 25, 22, 42),
            new Rect(11, 26, 9, 43),
            new Rect(44, 26, 9, 43),
            new Rect(33, 68, 10, 54),
            new Rect(21, 68, 10, 54)
    };

    private static final ResourceLocation[] SILHOUETTES = {
            rl("textures/gui/silhouette_head.png"),
            rl("textures/gui/silhouette_torso.png"),
            rl("textures/gui/silhouette_right_arm.png"),
            rl("textures/gui/silhouette_left_arm.png"),
            rl("textures/gui/silhouette_left_leg.png"),
            rl("textures/gui/silhouette_right_leg.png")
    };

    public static ResourceLocation silhouette(LimbType limb) {
        return SILHOUETTES[limb.getIndex()];
    }

    public static Rect limbRect(LimbType limb) {
        return LIMB_SPRITES[limb.getIndex()];
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("humannature", path);
    }
}
