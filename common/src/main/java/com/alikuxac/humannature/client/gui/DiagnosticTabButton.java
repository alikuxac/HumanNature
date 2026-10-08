package com.alikuxac.humannature.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class DiagnosticTabButton extends Button {
    private boolean activeTab = false;

    public DiagnosticTabButton(int x, int y, OnPress onPress) {
        super(x, y, 28, 28, Component.literal(""), onPress, DEFAULT_NARRATION);
    }

    public boolean isActiveTab() {
        return activeTab;
    }

    public void setActiveTab(boolean active) {
        this.activeTab = active;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Vanilla inventory tab styling (tab attached to the left edge of inventory GUI)
        // Main container background: matches Vanilla GUI background (#C6C6C6)
        int tabBg = activeTab ? 0xFFC6C6C6 : (isHoveredOrFocused() ? 0xFFBCBCBC : 0xFFA0A0A0);
        guiGraphics.fill(x, y, x + w, y + h, tabBg);

        // Vanilla-style 3D borders
        // Top highlight (White/Light gray)
        guiGraphics.fill(x, y, x + w, y + 1, 0xFFFFFFFF);
        // Left highlight (White/Light gray)
        guiGraphics.fill(x, y, x + 1, y + h, 0xFFFFFFFF);
        // Bottom shadow (Dark gray)
        guiGraphics.fill(x, y + h - 1, x + w, y + h, 0xFF555555);

        // If inactive or standalone tab button, draw subtle border
        guiGraphics.fill(x + w - 1, y, x + w, y + h, 0xFF373737);

        // Render silhouette mini icon inside tab (clean crisp pixel art)
        int cx = x + 14;
        int cy = y + 5;
        int iconColor = isHoveredOrFocused() ? 0xFF000000 : 0xFF2B2B2B;
        int limbColor = isHoveredOrFocused() ? 0xFF333333 : 0xFF555555;

        guiGraphics.fill(cx - 3, cy, cx + 3, cy + 5, iconColor); // Head
        guiGraphics.fill(cx - 4, cy + 6, cx + 4, cy + 13, iconColor); // Torso
        guiGraphics.fill(cx - 7, cy + 6, cx - 5, cy + 13, limbColor); // Left arm
        guiGraphics.fill(cx + 5, cy + 6, cx + 7, cy + 13, limbColor); // Right arm
        guiGraphics.fill(cx - 3, cy + 14, cx - 1, cy + 20, limbColor); // Left leg
        guiGraphics.fill(cx + 1, cy + 14, cx + 3, cy + 20, limbColor); // Right leg

        if (isHoveredOrFocused()) {
            guiGraphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, Component.literal("Body Diagnostics (H)"), mouseX, mouseY);
        }
    }
}
