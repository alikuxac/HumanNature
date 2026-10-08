package com.alikuxac.humannature.client.gui;

import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.injury.LimbType;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import com.alikuxac.humannature.modules.injury.TraumaSeverity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BodySilhouettePanel {
    public static final int PANEL_WIDTH = 51;
    public static final int PANEL_HEIGHT = 72;

    public record LimbHitbox(LimbType limb, int relX, int relY, int width, int height) {
        public boolean contains(int mouseX, int mouseY, int originX, int originY) {
            int x = originX + relX;
            int y = originY + relY;
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    // Centered neatly in a 51x72 preview box
    // Center X = 25.
    // Head: 14x13 (cx=25 -> x=18..32)
    // Torso: 16x22 (cx=25 -> x=17..33)
    // Left arm: 8x22 (x=8..16)
    // Right arm: 8x22 (x=34..42)
    // Left leg: 7x26 (x=17..24)
    // Right leg: 7x26 (x=26..33)
    private static final List<LimbHitbox> HITBOXES = List.of(
            new LimbHitbox(LimbType.HEAD, 18, 5, 15, 14),
            new LimbHitbox(LimbType.TORSO, 17, 20, 17, 22),
            new LimbHitbox(LimbType.MAIN_ARM, 7, 20, 9, 22),
            new LimbHitbox(LimbType.OFF_ARM, 35, 20, 9, 22),
            new LimbHitbox(LimbType.LEFT_LEG, 17, 43, 8, 25),
            new LimbHitbox(LimbType.RIGHT_LEG, 26, 43, 8, 25)
    );

    public static void render(GuiGraphics guiGraphics, int originX, int originY, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());

        // Ensure pending draw calls are flushed and disable depth test so HUD is cleanly in front of 3D entity
        RenderSystem.disableDepthTest();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 200.0f);

        // Completely cover the vanilla 3D player entity box with solid dark background
        guiGraphics.fill(originX, originY, originX + PANEL_WIDTH, originY + PANEL_HEIGHT, 0xFF141414);
        guiGraphics.renderOutline(originX, originY, PANEL_WIDTH, PANEL_HEIGHT, 0xFF373737);

        LimbHitbox hoveredBox = null;

        for (LimbHitbox box : HITBOXES) {
            byte sevLevel = data.getSeverity(box.limb());
            TraumaSeverity severity = TraumaSeverity.fromLevel(sevLevel);
            int color = severity.getColorRgb();

            boolean isHovered = box.contains(mouseX, mouseY, originX, originY);
            if (isHovered) {
                hoveredBox = box;
                // Brighten alpha/tint on hover
                color = (color & 0x00FFFFFF) | 0xFF000000;
            }

            int bx = originX + box.relX();
            int by = originY + box.relY();
            guiGraphics.fill(bx, by, bx + box.width(), by + box.height(), color);
            guiGraphics.renderOutline(bx, by, box.width(), box.height(), isHovered ? 0xFFFFFFFF : 0xFF1E1E1E);
        }

        guiGraphics.pose().popPose();
        guiGraphics.flush();
        RenderSystem.enableDepthTest();

        // Render Tooltip on hovered limb (tooltips render with their own high z-index and depth handling)
        if (hoveredBox != null) {
            byte sev = data.getSeverity(hoveredBox.limb());
            TraumaSeverity sevEnum = TraumaSeverity.fromLevel(sev);

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("§6" + hoveredBox.limb().getDisplayName()));
            tooltip.add(Component.literal("Status: " + getSeverityColorCode(sev) + sevEnum.getDisplayName() + " (Tier " + sev + ")"));

            // Tooltip debuff info
            if (sev > 0) {
                switch (hoveredBox.limb()) {
                    case HEAD -> tooltip.add(Component.literal(sev == 1 ? "§7Penalty: Mild Disorientation" : "§cPenalty: Flashing Darkness"));
                    case TORSO -> tooltip.add(Component.literal(sev == 1 ? "§7Penalty: Increased Hunger Exhaustion" : "§cPenalty: Max Health Capped (7 Hearts)"));
                    case MAIN_ARM, OFF_ARM -> tooltip.add(Component.literal("§7Penalty: -30% Attack Speed"));
                    case LEFT_LEG, RIGHT_LEG -> tooltip.add(Component.literal(sev == 1 ? "§7Penalty: -45% Speed, Sprint Locked" : "§cPenalty: -85% Speed, Forced Crawl"));
                }
                tooltip.add(Component.literal("§e[Click with Splint to Treat]"));
            } else {
                tooltip.add(Component.literal("§aNo active fractures"));
            }

            guiGraphics.renderComponentTooltip(mc.font, tooltip, mouseX, mouseY);
        }
    }

    private static String getSeverityColorCode(int sev) {
        return switch (sev) {
            case 2 -> "§c";
            case 1 -> "§6";
            default -> "§a";
        };
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button, int originX, int originY) {
        if (button != 0) return false;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;

        for (LimbHitbox box : HITBOXES) {
            if (box.contains((int) mouseX, (int) mouseY, originX, originY)) {
                // Check if holding a splint in container cursor / hand
                ItemStack carriedStack = mc.player.containerMenu.getCarried();
                if (carriedStack.isEmpty()) {
                    carriedStack = player.getMainHandItem();
                }

                if (SplintItem.isValidSplint(carriedStack)) {
                    // Perform quick treat
                    InjuryEventHandler.handleQuickTreat(player, box.limb(), carriedStack);
                    return true;
                }
            }
        }
        return false;
    }
}
