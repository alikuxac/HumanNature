package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.modules.physiology.ToxinType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class SwappableHudOverlay {

    public enum HudMode {
        OFF("OFF", "§7"),
        PHYSIOLOGY("PHYSIOLOGY", "§a"),
        INJURY("INJURY", "§c");

        private final String displayName;
        private final String colorCode;

        HudMode(String displayName, String colorCode) {
            this.displayName = displayName;
            this.colorCode = colorCode;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColorCode() {
            return colorCode;
        }

        public HudMode next() {
            HudMode[] vals = values();
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    private static HudMode currentMode = HudMode.OFF;

    public static HudMode getCurrentMode() {
        return currentMode;
    }

    public static void setCurrentMode(HudMode mode) {
        currentMode = mode != null ? mode : HudMode.OFF;
    }

    public static void cycleHudMode(Player player) {
        currentMode = currentMode.next();
        if (player != null) {
            Component message = Component.literal("§8[§6HUD Mode§8]: " + currentMode.getColorCode() + currentMode.getDisplayName());
            player.displayClientMessage(message, true);
        }
    }

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || player.isDeadOrDying()) return;
        if (!ClientConfig.ENABLE_SWAPPABLE_HUD.get()) return;
        if (currentMode != HudMode.PHYSIOLOGY) return;
        if (!ServerConfig.ENABLE_PHYSIOLOGY.get()) return;

        PlayerPhysiologyData physData = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        long awakeTicks = physData.getAwakeTicks();
        long awakeHours = awakeTicks / 1000L;
        float fatigueScore = physData.getFatigueScore();
        String fatigueLabel;
        String fatigueColor;
        if (fatigueScore >= 0.80f) {
            fatigueColor = "§c";
            fatigueLabel = "Drowsy";
        } else if (fatigueScore >= 0.50f) {
            fatigueColor = "§6";
            fatigueLabel = "Tired";
        } else if (fatigueScore >= 0.20f) {
            fatigueColor = "§e";
            fatigueLabel = "Mild";
        } else {
            fatigueColor = "§a";
            fatigueLabel = "Rested";
        }
        String fatigueText = "§7Fatigue: §f" + awakeHours + "h awake " + fatigueColor + "(" + fatigueLabel + "§8)";

        float toxinBuildup = physData.getToxinBuildup();
        ToxinType toxinType = physData.getLastToxinType();
        int filledSegments = Math.max(0, Math.min(10, Math.round(toxinBuildup / 10.0f)));
        StringBuilder barBuilder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            barBuilder.append(i < filledSegments ? "█" : "░");
        }
        String bar = barBuilder.toString();

        String stateLabel;
        if (toxinBuildup <= 0.0f) {
            stateLabel = "Clear";
        } else {
            int delayTicks = ServerConfig.TOXIN_DECAY_DELAY_TICKS != null ? ServerConfig.TOXIN_DECAY_DELAY_TICKS.get() : 80;
            if (physData.getTicksSinceLastExposure() >= delayTicks) {
                boolean isNearHeat = com.alikuxac.humannature.modules.physiology.PhysiologyEventHandler.isNearHeatSource(player);
                boolean isResting = player.isSleeping() || (player.isCrouching() && (player.walkDist - player.walkDistO <= 0.001F));
                stateLabel = (isNearHeat || isResting) ? "Decaying 3x" : "Decaying";
            } else {
                stateLabel = "Active";
            }
        }

        String toxinColor = toxinBuildup <= 0.0f ? "§a" : toxinType.getColorCode();
        String toxicityText = "§7Toxicity: " + toxinColor + "[" + bar + "] §f" + String.format("%.0f%%", toxinBuildup) + " §8(" + stateLabel + "§8)";

        float exertion = physData.getExertionScore();
        boolean isFailure = physData.isMuscleFailure();
        boolean isCramped = physData.isSwimmingCramped();
        String exertionStr;
        if (isFailure) {
            exertionStr = "§cFailure";
        } else if (isCramped) {
            exertionStr = "§cCramp";
        } else if (exertion >= 50.0f) {
            exertionStr = "§6Strained";
        } else if (exertion > 0.0f) {
            exertionStr = "§eNormal";
        } else {
            exertionStr = "§aNormal";
        }

        var foodData = player.getFoodData();
        String digestionStr;
        if (foodData.getSaturationLevel() > 0.0f || (player.isHurt() && foodData.getFoodLevel() >= 18)) {
            digestionStr = "§aActive";
        } else if (foodData.getFoodLevel() > 6) {
            digestionStr = "§7Resting";
        } else {
            digestionStr = "§cHungry";
        }
        String exertionDigestionText = "§7Exertion: " + exertionStr + " §8| §7Digestion: " + digestionStr;

        int headerWidth = mc.font.width("MODE: PHYSIOLOGY");
        int w1 = mc.font.width(fatigueText);
        int w2 = mc.font.width(toxicityText);
        int w3 = mc.font.width(exertionDigestionText);
        int contentWidth = Math.max(headerWidth, Math.max(w1, Math.max(w2, w3)));
        int boxWidth = contentWidth + 12;
        int boxHeight = 48;

        int x = ClientConfig.SWAPPABLE_HUD_X.get();
        int y = ClientConfig.SWAPPABLE_HUD_Y.get();

        guiGraphics.fill(x - 4, y - 4, x + boxWidth, y + boxHeight, 0x90101010);
        guiGraphics.renderOutline(x - 4, y - 4, boxWidth + 4, boxHeight + 4, 0xFF353535);

        guiGraphics.drawString(mc.font, "§6§lMODE: PHYSIOLOGY", x + 2, y, 0xFFFFFF, false);
        guiGraphics.drawString(mc.font, fatigueText, x + 2, y + 12, 0xFFFFFF, false);
        guiGraphics.drawString(mc.font, toxicityText, x + 2, y + 23, 0xFFFFFF, false);
        guiGraphics.drawString(mc.font, exertionDigestionText, x + 2, y + 34, 0xFFFFFF, false);
    }
}
