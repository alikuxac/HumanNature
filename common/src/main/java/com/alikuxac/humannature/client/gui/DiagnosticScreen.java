package com.alikuxac.humannature.client.gui;

import com.alikuxac.humannature.client.ModKeyMappings;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.injury.LimbType;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import com.alikuxac.humannature.modules.injury.TraumaSeverity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticScreen extends Screen {
    public static final int GUI_WIDTH = 220;
    public static final int GUI_HEIGHT = 166;

    private int leftPos;
    private int topPos;

    public record LimbHitbox(LimbType limb, int relX, int relY, int width, int height) {
        public boolean contains(int mouseX, int mouseY, int originX, int originY) {
            int x = originX + relX;
            int y = originY + relY;
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    // Centered silhouette inside a 70x120 area
    // Center X = 35
    // Head: 22x22 (cx=35 -> x=24..46, y=10..32)
    // Torso: 24x36 (cx=35 -> x=23..47, y=34..70)
    // Left arm: 12x36 (x=9..21, y=34..70)
    // Right arm: 12x36 (x=49..61, y=34..70)
    // Left leg: 11x44 (x=23..34, y=72..116)
    // Right leg: 11x44 (x=36..47, y=72..116)
    private static final List<LimbHitbox> HITBOXES = List.of(
            new LimbHitbox(LimbType.HEAD, 24, 10, 22, 22),
            new LimbHitbox(LimbType.TORSO, 23, 34, 24, 36),
            new LimbHitbox(LimbType.MAIN_ARM, 9, 34, 12, 36),
            new LimbHitbox(LimbType.OFF_ARM, 49, 34, 12, 36),
            new LimbHitbox(LimbType.LEFT_LEG, 23, 72, 11, 44),
            new LimbHitbox(LimbType.RIGHT_LEG, 36, 72, 11, 44)
    );

    public DiagnosticScreen() {
        super(Component.literal("Skeleton & Body Diagnostics"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        // Button to return back to Inventory View
        this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                Component.literal("← Inventory"),
                btn -> {
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
                    }
                })
                .bounds(leftPos + GUI_WIDTH - 82, topPos + 6, 70, 16)
                .build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Minecraft mc = this.minecraft;
        if (mc == null) return;
        Player player = mc.player;
        if (player == null) return;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());

        // Dark modern panel background
        RenderSystem.disableDepthTest();
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xF0181818);
        guiGraphics.renderOutline(leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, 0xFF3E3E3E);

        // Header Title
        guiGraphics.drawString(this.font, "§6§lBODY DIAGNOSTICS", leftPos + 12, topPos + 10, 0xFFFFFF, false);
        guiGraphics.fill(leftPos + 12, topPos + 24, leftPos + GUI_WIDTH - 12, topPos + 25, 0xFF353535);

        // Sub-box for Silhouette Model (70x126)
        int silX = leftPos + 12;
        int silY = topPos + 30;
        guiGraphics.fill(silX, silY, silX + 70, silY + 124, 0xFF101010);
        guiGraphics.renderOutline(silX, silY, 70, 124, 0xFF2A2A2A);

        LimbHitbox hoveredBox = null;

        for (LimbHitbox box : HITBOXES) {
            byte sevLevel = data.getSeverity(box.limb());
            TraumaSeverity severity = TraumaSeverity.fromLevel(sevLevel);
            int color = severity.getColorRgb();

            boolean isHovered = box.contains(mouseX, mouseY, silX, silY);
            if (isHovered) {
                hoveredBox = box;
                color = (color & 0x00FFFFFF) | 0xFF000000;
            }

            int bx = silX + box.relX();
            int by = silY + box.relY();
            guiGraphics.fill(bx, by, bx + box.width(), by + box.height(), color);
            guiGraphics.renderOutline(bx, by, box.width(), box.height(), isHovered ? 0xFFFFFFFF : 0xFF222222);
        }

        // Details Panel on the right (x = leftPos + 88 to GUI_WIDTH - 12)
        int infoX = leftPos + 88;
        int curY = topPos + 30;

        guiGraphics.drawString(this.font, "§7Status Overview:", infoX, curY, 0xAAAAAA, false);
        curY += 13;

        int totalFractures = 0;
        for (LimbType limb : LimbType.values()) {
            byte sev = data.getSeverity(limb);
            if (sev > 0) totalFractures++;
        }

        if (totalFractures == 0) {
            guiGraphics.drawString(this.font, "§a✔ All limbs healthy", infoX, curY, 0x55FF55, false);
            curY += 12;
            guiGraphics.drawString(this.font, "§8No bone fractures detected.", infoX, curY, 0x888888, false);
            curY += 14;
        } else {
            guiGraphics.drawString(this.font, "§c⚠ " + totalFractures + " Fractured Limb(s)", infoX, curY, 0xFF5555, false);
            curY += 13;

            for (LimbType limb : LimbType.values()) {
                byte sev = data.getSeverity(limb);
                if (sev > 0) {
                    TraumaSeverity sevEnum = TraumaSeverity.fromLevel(sev);
                    String line = "§f• " + limb.getDisplayName() + ": " + getSeverityColor(sev) + sevEnum.getDisplayName();
                    guiGraphics.drawString(this.font, line, infoX, curY, 0xFFFFFF, false);
                    curY += 11;
                }
            }
            curY += 4;
        }

        // Help & Splint Treatment Tip
        guiGraphics.fill(infoX, topPos + 118, leftPos + GUI_WIDTH - 12, topPos + 119, 0xFF353535);
        ItemStack availableSplint = InjuryEventHandler.findFirstSplint(player);
        boolean hasSplint = !availableSplint.isEmpty();
        boolean isOnCooldown = hasSplint && player.getCooldowns().isOnCooldown(availableSplint.getItem());

        if (hasSplint) {
            if (isOnCooldown) {
                guiGraphics.drawString(this.font, "§c⌛ Treating... Please wait", infoX, topPos + 124, 0xFF6666, false);
            } else {
                guiGraphics.drawString(this.font, "§e[Ready] Click limb to treat", infoX, topPos + 124, 0xFFFF55, false);
            }
            guiGraphics.drawString(this.font, "§7Using: " + availableSplint.getHoverName().getString(), infoX, topPos + 136, 0xAAAAAA, false);
        } else {
            guiGraphics.drawString(this.font, "§8No Splint in inventory", infoX, topPos + 124, 0x888888, false);
            guiGraphics.drawString(this.font, "§8[E] Return to Inventory", infoX, topPos + 136, 0x666666, false);
        }

        // Hover tooltip
        if (hoveredBox != null) {
            byte sev = data.getSeverity(hoveredBox.limb());
            TraumaSeverity sevEnum = TraumaSeverity.fromLevel(sev);

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("§6" + hoveredBox.limb().getDisplayName()));
            tooltip.add(Component.literal("Status: " + getSeverityColor(sev) + sevEnum.getDisplayName() + " (Tier " + sev + ")"));

            if (sev > 0) {
                switch (hoveredBox.limb()) {
                    case HEAD -> tooltip.add(Component.literal(sev == 1 ? "§7Penalty: Mild Disorientation" : "§cPenalty: Flashing Darkness"));
                    case TORSO -> tooltip.add(Component.literal(sev == 1 ? "§7Penalty: Increased Hunger Exhaustion" : "§cPenalty: Max Health Capped (7 Hearts)"));
                    case MAIN_ARM, OFF_ARM -> tooltip.add(Component.literal("§7Penalty: -30% Attack Speed"));
                    case LEFT_LEG, RIGHT_LEG -> tooltip.add(Component.literal(sev == 1 ? "§7Penalty: -45% Speed, Sprint Locked" : "§cPenalty: -85% Speed, Forced Crawl"));
                }
                if (hasSplint) {
                    if (isOnCooldown) {
                        tooltip.add(Component.literal("§c[Splint on Cooldown]"));
                    } else {
                        tooltip.add(Component.literal("§e[Click to Treat with " + availableSplint.getHoverName().getString() + "]"));
                    }
                } else {
                    tooltip.add(Component.literal("§7[Requires Splint in inventory]"));
                }
            } else {
                tooltip.add(Component.literal("§aNo active fractures"));
            }

            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }

        RenderSystem.enableDepthTest();
    }

    private static String getSeverityColor(int sev) {
        return switch (sev) {
            case 2 -> "§c";
            case 1 -> "§6";
            default -> "§a";
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.minecraft != null && this.minecraft.player != null) {
            Player player = this.minecraft.player;
            int silX = leftPos + 12;
            int silY = topPos + 30;

            for (LimbHitbox box : HITBOXES) {
                if (box.contains((int) mouseX, (int) mouseY, silX, silY)) {
                    ItemStack splint = InjuryEventHandler.findFirstSplint(player);
                    if (!splint.isEmpty()) {
                        InjuryEventHandler.handleQuickTreat(player, box.limb(), splint);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Pressing H toggles and closes the screen
        if (ModKeyMappings.OPEN_DIAGNOSTICS.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        // Pressing E (Inventory Key) returns directly to Inventory View
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (this.minecraft.player != null) {
                this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
