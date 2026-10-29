package com.alikuxac.humannature.client;

import com.alikuxac.humannature.client.gui.DiagnosticScreen;
import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.injury.LimbType;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.modules.physiology.ToxinType;
import com.alikuxac.humannature.modules.temperature.PlayerTemperatureData;
import com.alikuxac.humannature.modules.temperature.TemperatureEventHandler;
import com.alikuxac.humannature.modules.temperature.TemperatureModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SwappableHudOverlay {

    public enum HudMode {
        OFF("OFF", "§7"),
        PHYSIOLOGY("PHYSIOLOGY", "§a"),
        ENVIRONMENT("ENVIRONMENT", "§b"),
        INJURY("INJURY", "§c"),
        COMBINED("COMBINED", "§e");

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

        public static HudMode fromString(String str) {
            if (str == null) return COMBINED;
            String trimmed = str.trim();
            if (trimmed.equalsIgnoreCase("TEMPERATURE")) {
                return ENVIRONMENT;
            }
            for (HudMode mode : values()) {
                if (mode.name().equalsIgnoreCase(trimmed)) {
                    return mode;
                }
            }
            return COMBINED;
        }
    }

    private static HudMode currentMode = null;
    private static int actionBarCooldown = 0;

    public static HudMode getCurrentMode() {
        if (currentMode == null) {
            String def = ClientConfig.DEFAULT_HUD_MODE != null ? ClientConfig.DEFAULT_HUD_MODE.get() : "COMBINED";
            currentMode = HudMode.fromString(def);
        }
        return currentMode;
    }

    public static void setCurrentMode(HudMode mode) {
        currentMode = mode != null ? mode : HudMode.OFF;
    }

    public static void cycleHudMode(Player player) {
        currentMode = getCurrentMode().next();
        if (player != null) {
            Component message = Component.literal("§8[§6HUD Mode§8]: " + currentMode.getColorCode() + currentMode.getDisplayName());
            player.displayClientMessage(message, true);
            actionBarCooldown = 50;
        }
    }

    public static void onClientTick(Player player) {
        if (player == null || !ClientConfig.SHOW_ACTIONBAR_ENVIRONMENT_CUES.get()) return;
        if (getCurrentMode() != HudMode.ENVIRONMENT) return;
        if (!ServerConfig.ENABLE_TEMPERATURE.get()) return;

        if (--actionBarCooldown > 0) return;

        PlayerTemperatureData tempData = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
        float wetness = tempData.getWetnessBuffer();
        int ticksFrozen = player.getTicksFrozen();
        boolean hasBuff = tempData.hasInternalWarming();
        boolean nearHeat = tempData.isNearHeatSource();

        boolean isNormal = (wetness <= 0.0f && ticksFrozen <= 0 && !hasBuff && !nearHeat);
        if (isNormal) {
            actionBarCooldown = 20;
            return;
        }

        actionBarCooldown = 70;

        String wetLabel;
        if (wetness >= 1.0f) {
            wetLabel = "100% (Soaked)";
        } else if (wetness >= 0.5f) {
            wetLabel = String.format("%.0f%% (Damp)", wetness * 100.0f);
        } else if (wetness > 0.0f) {
            wetLabel = String.format("%.0f%% (Damp)", wetness * 100.0f);
        } else {
            wetLabel = "Dry";
        }

        String tempLabel;
        int freezeThresh = (int) Math.round(player.getTicksRequiredToFreeze() * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get());
        if (hasBuff) {
            tempLabel = "Warm (Stew Buff)";
        } else if (ticksFrozen >= freezeThresh) {
            tempLabel = "Freezing";
        } else if (ticksFrozen > 0) {
            tempLabel = "Chilly";
        } else if (nearHeat) {
            tempLabel = "Rewarming";
        } else {
            tempLabel = "Normal";
        }

        String wetColor = wetness >= 1.0f ? "§c" : (wetness >= 0.5f ? "§e" : (wetness > 0.0f ? "§b" : "§a"));
        String tempColor = ticksFrozen >= freezeThresh ? "§c" : (ticksFrozen > 0 ? "§b" : (hasBuff ? "§6" : "§a"));

        Component cue = Component.literal("§7Wetness: " + wetColor + wetLabel + " §7| Temp: " + tempColor + tempLabel);
        player.displayClientMessage(cue, true);
    }

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || player.isDeadOrDying()) return;
        if (!ClientConfig.ENABLE_SWAPPABLE_HUD.get()) return;

        HudMode mode = getCurrentMode();
        if (mode == HudMode.OFF || mode == HudMode.INJURY || mode == HudMode.ENVIRONMENT) return;

        boolean enablePhys = ServerConfig.ENABLE_PHYSIOLOGY.get();
        boolean enableTemp = ServerConfig.ENABLE_TEMPERATURE.get();
        boolean enableInj = ServerConfig.ENABLE_INJURY.get();

        if (mode == HudMode.PHYSIOLOGY && !enablePhys) return;
        if (mode == HudMode.COMBINED && !enablePhys && !enableTemp && !enableInj) return;

        List<String> lines = new ArrayList<>();

        PlayerPhysiologyData physData = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());

            if (enablePhys) {
                if (physData.isAdrenalineActive()) {
                    lines.add("§c§l⚡ ADRENALINE SURGE ⚡");
                } else if (physData.isPanicking()) {
                    lines.add("§c§l⚠ SENSORY PANIC (0 LUX) ⚠");
                }
            }

            if (mode == HudMode.COMBINED && enableInj) {
                PlayerInjuryData injuryData = InjuryModule.getOrCreateInjuryData(player.getUUID());
                boolean hasTrauma = InjuryEventHandler.isFractureActive(player) || injuryData.hasAnyInjury();
                if (hasTrauma) {
                    StringBuilder injuredLimbs = new StringBuilder();
                    for (LimbType limb : LimbType.values()) {
                        byte sev = injuryData.getSeverity(limb);
                        if (sev > 0) {
                            if (!injuredLimbs.isEmpty()) injuredLimbs.append(", ");
                            injuredLimbs.append(limb.getDisplayName()).append(" (T").append(sev).append(")");
                        }
                    }
                    String limbsTitle = injuredLimbs.isEmpty() ? "Trauma: Stabilized" : "§cInjured: §f" + injuredLimbs;
                    lines.add(limbsTitle);

                    int remainingTicks = InjuryEventHandler.getRemainingFractureTime(player);
                    if (remainingTicks > 0) {
                        int totalSeconds = remainingTicks / 20;
                        int minutes = totalSeconds / 60;
                        int seconds = totalSeconds % 60;
                        boolean isMoving = player.walkDist - player.walkDistO > 0.001F;
                        boolean isResting = player.isSleeping() || (player.isCrouching() && !isMoving);
                        String healRate = isResting ? (physData.hasCalciumBuff() ? "§bResting 4x (Calcium)" : "§aResting 2x") :
                                (isMoving ? "§cPaused" : "§eStanding 1x");
                        lines.add(String.format("§7Fracture Healing: §f%02d:%02d §8(%s§8)", minutes, seconds, healRate));
                    }
                }
            }

            if (enablePhys) {
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
                lines.add("§7Fatigue: §f" + awakeHours + "h awake " + fatigueColor + "(" + fatigueLabel + " " + String.format("%.0f%%", fatigueScore * 100.0f) + "§8)");

                if (PhysiologyModule.isToxinBuildupEnabled()) {
                    float toxinBuildup = physData.getToxinBuildup();
                    ToxinType toxinType = physData.getLastToxinType();
                    int filledSegments = Math.max(0, Math.min(10, Math.round(toxinBuildup / 10.0f)));
                    StringBuilder barBuilder = new StringBuilder();
                    for (int i = 0; i < 10; i++) {
                        barBuilder.append(i < filledSegments ? "█" : "░");
                    }
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
                    lines.add("§7Toxicity: " + toxinColor + "[" + barBuilder + "] §f" + String.format("%.0f%%", toxinBuildup) + " §8(" + stateLabel + "§8)");
                }

                if (ClientConfig.SHOW_CARDIAC_HUD != null && ClientConfig.SHOW_CARDIAC_HUD.get()) {
                    int bpm = DiagnosticScreen.calculateHeartRateBpm(player, physData);
                    String bpmColor = bpm >= 140 ? "§c" : (bpm >= 110 ? "§6" : (bpm >= 80 ? "§e" : "§a"));
                    String bpmLabel = bpm >= 140 ? "Tachycardia" : (bpm >= 110 ? "Elevated" : (bpm >= 80 ? "Moderate" : "Normal"));
                    lines.add("§7Heart Rate: " + bpmColor + "❤ " + bpm + " BPM §8(" + bpmLabel + "§8)");
                }

                float exertion = physData.getExertionScore();
                boolean isFailure = physData.isMuscleFailure();
                boolean isCramped = physData.isSwimmingCramped();
                String exertionStr = isFailure ? "§cFailure" : (isCramped ? "§cCramp" : (exertion >= 50.0f ? "§6Strained" : "§aNormal"));
                lines.add("§7Stamina: " + (isFailure ? "§c" : (exertion >= 50.0f ? "§6" : "§a")) + String.format("%.0f%%", 100.0f - exertion) + " §8(" + exertionStr + "§8)");

                boolean showMetabolic = ClientConfig.SHOW_METABOLIC_DETAILS == null || ClientConfig.SHOW_METABOLIC_DETAILS.get();
                if (showMetabolic) {
                    float pendingNutr = physData.getPendingNutrition();
                    float pendingSat = physData.getPendingSaturation();
                    if (pendingNutr > 0.0f || pendingSat > 0.0f) {
                        lines.add(String.format("§aDigestion Buffer: +%.0f🍗 / +%.1f sat §8(Absorbing...)", pendingNutr, pendingSat));
                    }

                    float indigestion = physData.getIndigestionBuildup();
                    if (indigestion > 0.0f) {
                        String crampNotice = indigestion >= 50.0f ? " §c(Cramps Active!)" : " §6(Upset)";
                        lines.add("§6Indigestion: §e" + String.format("%.0f%%", indigestion) + crampNotice);
                    }

                    if (physData.hasCalciumBuff()) {
                        long calSec = physData.getCalciumAbsorptionTicks() / 20;
                        lines.add(String.format("§bBone Calcium: %02d:%02d §a(2x Bone Heal)", calSec / 60, calSec % 60));
                    }

                    if (pendingNutr <= 0.0f && indigestion <= 0.0f && !physData.hasCalciumBuff()) {
                        var foodData = player.getFoodData();
                        String digestionState = (foodData.getSaturationLevel() > 0.0f || (player.isHurt() && foodData.getFoodLevel() >= 18)) ? "§aActive" :
                                (foodData.getFoodLevel() > 6 ? "§7Resting" : "§cHungry");
                        lines.add("§7Digestion: " + digestionState);
                    }
                }

                if (physData.isAdrenalineActive()) {
                    float remRush = physData.getAdrenalineTicks() / 20.0f;
                    lines.add(String.format("§c⚡ Rush Duration: §f%.1fs §a(+35%% Spd, Sprinting)", remRush));
                    lines.add("§c  ↳ Pain shock collapse imminent upon expiry");
                } else if (physData.getAdrenalineCooldownTicks() > 0) {
                    int remCooldown = physData.getAdrenalineCooldownTicks() / 20;
                    lines.add("§8⚡ Adrenaline Cooldown: " + remCooldown + "s");
                }

                if (physData.getClaustrophobiaTicks() > 0 || physData.isPanicking()) {
                    int panicThreshold = ServerConfig.PANIC_THRESHOLD_TICKS != null ? ServerConfig.PANIC_THRESHOLD_TICKS.get() : 100;
                    int panicPct = Math.min(100, Math.round((physData.getClaustrophobiaTicks() / (float) panicThreshold) * 100.0f));
                    String panicDesc = physData.isPanicking() ? "§c§l100% Aim Tremors & Palpitations!" : "§e" + panicPct + "% (Seek Light/Heat/Pet)";
                    lines.add("§c⚠ Claustrophobia: " + panicDesc);
                }
            }

            if (mode == HudMode.COMBINED && enableTemp) {
                PlayerTemperatureData tempData = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
                float wetness = tempData.getWetnessBuffer();
                int ticksFrozen = player.getTicksFrozen();
                if (wetness > 0.0f || ticksFrozen > 0 || tempData.isNearHeatSource()) {
                    int filledSegments = Math.max(0, Math.min(10, Math.round(wetness * 10.0f)));
                    StringBuilder barBuilder = new StringBuilder();
                    for (int i = 0; i < 10; i++) {
                        barBuilder.append(i < filledSegments ? "█" : "░");
                    }
                    String statusLabel;
                    if (tempData.isNearHeatSource()) {
                        statusLabel = "§6Drying (Heat)";
                    } else if (player.isInWater() || player.isUnderWater()) {
                        statusLabel = "§9Submerged";
                    } else if (player.level().isRainingAt(player.blockPosition())) {
                        statusLabel = "§9Rain Soaking";
                    } else if (ticksFrozen > 0) {
                        statusLabel = "§bFreezing!";
                    } else {
                        statusLabel = "§7Drying";
                    }
                    String color = wetness >= 1.0f ? "§c" : (wetness >= 0.5f ? "§e" : "§b");
                    lines.add("§7Wetness: " + color + "[" + barBuilder + "] §f" + String.format("%.0f%%", wetness * 100.0f) + " §8(" + statusLabel + "§8)");
                } else if (tempData.hasInternalWarming()) {
                    int remSec = tempData.getInternalWarmingTicks() / 20;
                    lines.add(String.format("§6♨ Internal Warming: %ds §8(Warm)", remSec));
                } else {
                    lines.add("§7Temperature: §aWarm §8(Dry)");
                }
            }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int x = ClientConfig.SWAPPABLE_HUD_X.get();
        int y = ClientConfig.SWAPPABLE_HUD_Y.get();

        if (x < 4) x = 4;
        if (y < 4) y = 4;

        int maxWrapWidth = Math.max(120, Math.min(240, screenWidth - x - 16));
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        for (String line : lines) {
            if (mc.font.width(line) <= maxWrapWidth) {
                wrappedLines.add(Component.literal(line).getVisualOrderText());
            } else {
                wrappedLines.addAll(mc.font.split(Component.literal(line), maxWrapWidth));
            }
        }

        int maxLineWidth = 0;
        for (FormattedCharSequence fcs : wrappedLines) {
            int w = mc.font.width(fcs);
            if (w > maxLineWidth) maxLineWidth = w;
        }

        int boxWidth = Math.max(120, maxLineWidth + 8);
        int boxHeight = wrappedLines.size() * 11 + 6;

        if (x + boxWidth > screenWidth - 4) {
            x = Math.max(4, screenWidth - boxWidth - 4);
        }
        if (y + boxHeight > screenHeight - 4) {
            y = Math.max(4, screenHeight - boxHeight - 4);
        }

        guiGraphics.fill(x - 4, y - 4, x + boxWidth, y + boxHeight, 0x90101010);
        guiGraphics.renderOutline(x - 4, y - 4, boxWidth + 4, boxHeight + 4, 0xFF353535);

        int curLineY = y;
        for (FormattedCharSequence fcs : wrappedLines) {
            guiGraphics.drawString(mc.font, fcs, x, curLineY, 0xFFFFFF, false);
            curLineY += 11;
        }
    }
}
