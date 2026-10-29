package com.alikuxac.humannature.client.gui;

import com.alikuxac.humannature.client.ModKeyMappings;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.injury.LimbType;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import com.alikuxac.humannature.modules.injury.TraumaSeverity;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.modules.physiology.ToxinType;
import com.alikuxac.humannature.modules.temperature.PlayerTemperatureData;
import com.alikuxac.humannature.modules.temperature.TemperatureEventHandler;
import com.alikuxac.humannature.modules.temperature.TemperatureModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.components.Button;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticScreen extends Screen {
    public static final int GUI_WIDTH = 240;
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

    private static final List<LimbHitbox> HITBOXES = List.of(
            new LimbHitbox(LimbType.HEAD, 24, 10, 22, 22),
            new LimbHitbox(LimbType.TORSO, 23, 34, 24, 36),
            new LimbHitbox(LimbType.OFF_ARM, 9, 34, 12, 36),
            new LimbHitbox(LimbType.MAIN_ARM, 49, 34, 12, 36),
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

        this.addRenderableWidget(Button.builder(
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

        RenderSystem.disableDepthTest();
        guiGraphics.fill(leftPos, topPos, leftPos + GUI_WIDTH, topPos + GUI_HEIGHT, 0xF0181818);
        guiGraphics.renderOutline(leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, 0xFF3E3E3E);

        guiGraphics.drawString(this.font, "§6§lBODY DIAGNOSTICS", leftPos + 12, topPos + 10, 0xFFFFFF, false);
        guiGraphics.fill(leftPos + 12, topPos + 24, leftPos + GUI_WIDTH - 12, topPos + 25, 0xFF353535);

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

        int infoX = leftPos + 88;
        int infoWidth = (leftPos + GUI_WIDTH - 12) - infoX;
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
            guiGraphics.drawString(this.font, "§8No fractures detected.", infoX, curY, 0x888888, false);
            curY += 14;
        } else {
            guiGraphics.drawString(this.font, "§c⚠ " + totalFractures + " Fractured Limb(s)", infoX, curY, 0xFF5555, false);
            curY += 13;

            for (LimbType limb : LimbType.values()) {
                byte sev = data.getSeverity(limb);
                if (sev > 0) {
                    String sevName = sev >= 2 ? "Broken" : "Sprained";
                    String line = "§f• " + limb.getDisplayName() + ": " + getSeverityColor(sev) + sevName;
                    guiGraphics.drawString(this.font, line, infoX, curY, 0xFFFFFF, false);
                    curY += 11;
                }
            }
            curY += 4;
        }

        List<Component> physTooltip = null;
        List<Component> toxinTooltip = null;
        List<Component> exertionTooltip = null;
        List<Component> heartRateTooltip = null;
        List<Component> metabolismTooltip = null;

        if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
            PlayerPhysiologyData physData = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
            int barWidth = infoWidth;

            guiGraphics.fill(infoX, curY, leftPos + GUI_WIDTH - 12, curY + 1, 0xFF353535);
            curY += 3;

            if (PhysiologyModule.isSleepDeprivationEnabled()) {
                float fatigueScore = physData.getFatigueScore();
                float fatiguePct = physData.getFatiguePercentage();

                String fatigueColor;
                String fatigueLabel;
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

                int physLabelY = curY;
                String physText = "§7Fatigue: " + fatigueColor + String.format("%.0f%%", fatiguePct) + " §8(" + fatigueLabel + "§8)";
                guiGraphics.drawString(this.font, physText, infoX, curY, 0xFFFFFF, false);
                curY += 9;

                int barHeight = 2;
                int barY = curY;
                guiGraphics.fill(infoX, barY, infoX + barWidth, barY + barHeight, 0xFF101010);
                guiGraphics.renderOutline(infoX, barY, barWidth, barHeight, 0xFF282828);

                int filledWidth = (int) (barWidth * Math.min(1.0f, Math.max(0.0f, fatigueScore)));
                int barColor = fatigueScore >= 0.80f ? 0xFFFF4444 : (fatigueScore >= 0.50f ? 0xFFFFAA00 : (fatigueScore >= 0.20f ? 0xFFFFFF55 : 0xFF55FF55));
                if (filledWidth > 0) {
                    guiGraphics.fill(infoX, barY, infoX + filledWidth, barY + barHeight, barColor);
                }
                curY += barHeight + 3;

                if (mouseX >= infoX && mouseX <= infoX + barWidth && mouseY >= physLabelY && mouseY <= barY + barHeight + 2) {
                    physTooltip = new ArrayList<>();
                    physTooltip.add(Component.literal("§6Sleep Fatigue: " + fatigueColor + String.format("%.1f%%", fatiguePct)));
                    long awakeTicks = physData.getAwakeTicks();
                    double awakeDays = (double) awakeTicks / 24000.0;
                    physTooltip.add(Component.literal(String.format("§7Awake: %.1f in-game day(s)", awakeDays)));
                    if (physData.isBlinkEligible()) {
                        physTooltip.add(Component.literal("§c⚠ Micro-Sleep Active: Periodic Darkness blinks while moving"));
                    } else {
                        physTooltip.add(Component.literal("§a✔ Darkness Blinks: Inactive (< 80% fatigue)"));
                    }
                    physTooltip.add(Component.literal("§8Sleep in a bed through the night to reset fatigue."));
                }
            }

            if (PhysiologyModule.isToxinBuildupEnabled()) {
                float toxinBuildup = physData.getToxinBuildup();
                ToxinType toxinType = physData.getLastToxinType();

                String toxinLabel = toxinType == ToxinType.NONE ? "Clear" : toxinType.getDisplayName();
                String toxinText = "§7Toxin: " + (toxinBuildup > 0.0f ? toxinType.getColorCode() + String.format("%.0f%%", toxinBuildup) : "§a0%")
                        + " §8(" + (toxinBuildup > 0.0f ? toxinType.getColorCode() : "§a") + toxinLabel + "§8)";

                int toxinLabelY = curY;
                guiGraphics.drawString(this.font, toxinText, infoX, curY, 0xFFFFFF, false);
                curY += 9;

                int toxinBarHeight = 2;
                int toxinBarY = curY;
                guiGraphics.fill(infoX, toxinBarY, infoX + barWidth, toxinBarY + toxinBarHeight, 0xFF101010);
                guiGraphics.renderOutline(infoX, toxinBarY, barWidth, toxinBarHeight, 0xFF282828);

                int filledToxinWidth = (int) (barWidth * (Math.min(100.0f, Math.max(0.0f, toxinBuildup)) / 100.0f));
                int toxinBarColor = toxinType == ToxinType.WITHER ? 0xFFBA68C8 : (toxinBuildup > 0.0f ? 0xFF00E676 : 0xFF44AA44);
                if (filledToxinWidth > 0) {
                    guiGraphics.fill(infoX, toxinBarY, infoX + filledToxinWidth, toxinBarY + toxinBarHeight, toxinBarColor);
                }
                curY += toxinBarHeight + 3;

                if (mouseX >= infoX && mouseX <= infoX + barWidth && mouseY >= toxinLabelY && mouseY <= toxinBarY + toxinBarHeight + 2) {
                    toxinTooltip = new ArrayList<>();
                    toxinTooltip.add(Component.literal("§6Toxin Buildup: " + (toxinBuildup > 0.0f ? toxinType.getColorCode() + String.format("%.1f%%", toxinBuildup) : "§a0.0% (Clear)")));
                    float toxinMultiplier = PhysiologyModule.calculateToxinMultiplier(player.getUUID());
                    if (toxinBuildup > 0.0f) {
                        toxinTooltip.add(Component.literal("§7Accumulated Type: " + toxinType.getColorCode() + toxinType.getDisplayName()));
                        int delayTicks = ServerConfig.TOXIN_DECAY_DELAY_TICKS != null ? ServerConfig.TOXIN_DECAY_DELAY_TICKS.get() : 80;
                        int elapsed = physData.getTicksSinceLastExposure();
                        if (elapsed < delayTicks) {
                            toxinTooltip.add(Component.literal(String.format("§7Decay Delay: %.1fs remaining", (delayTicks - elapsed) / 20.0)));
                        } else {
                            toxinTooltip.add(Component.literal("§a✔ Passive Decay Active (~2.0%/s)"));
                        }
                        toxinTooltip.add(Component.literal("§e⚡ Heat/Resting: 3x decay acceleration"));
                        toxinTooltip.add(Component.literal("§b🍶 Milk Bucket: Clears 100% | 🍯 Honey: -50%"));
                        if (toxinMultiplier > 1.01f) {
                            toxinTooltip.add(Component.literal(String.format("§c⚠ Vulnerability: %.1fx buildup (Fatigue / Trauma)", toxinMultiplier)));
                        }
                        toxinTooltip.add(Component.literal("§c⚠ Reaching 100% causes immediate outbreak!"));
                    } else {
                        toxinTooltip.add(Component.literal("§a✔ Toxin Free: Poison & Wither attacks build up here."));
                        if (toxinMultiplier > 1.01f) {
                            toxinTooltip.add(Component.literal(String.format("§c⚠ Vulnerability: %.1fx buildup (Fatigue / Trauma)", toxinMultiplier)));
                        }
                        toxinTooltip.add(Component.literal("§8Effects erupt only when the buildup reaches 100%."));
                    }
                }
            }

            if (PhysiologyModule.isExertionEnabled()) {
                float exertion = physData.getExertionScore();
                boolean isFailure = physData.isMuscleFailure();
                boolean isCramped = physData.isSwimmingCramped();

                String statusColor;
                String statusLabel;
                if (isFailure) {
                    statusColor = "§c";
                    statusLabel = "Failure";
                } else if (exertion >= 50.0f) {
                    statusColor = "§6";
                    statusLabel = "Strained";
                } else {
                    statusColor = "§a";
                    statusLabel = "Rested";
                }

                String exertionText = "§7Stamina: " + statusColor + String.format("%.0f%%", 100.0f - exertion)
                        + " §8(" + statusLabel + (isCramped ? " §c| Cramp" : "") + "§8)";

                int exertionLabelY = curY;
                guiGraphics.drawString(this.font, exertionText, infoX, curY, 0xFFFFFF, false);
                curY += 9;

                int exertionBarHeight = 2;
                int exertionBarY = curY;
                guiGraphics.fill(infoX, exertionBarY, infoX + barWidth, exertionBarY + exertionBarHeight, 0xFF101010);
                guiGraphics.renderOutline(infoX, exertionBarY, barWidth, exertionBarHeight, 0xFF282828);

                float staminaFraction = Math.max(0.0f, Math.min(1.0f, (100.0f - exertion) / 100.0f));
                int filledExertionWidth = (int) (barWidth * staminaFraction);
                int exertionBarColor = isFailure ? 0xFFFF4444 : (exertion >= 50.0f ? 0xFFFFAA00 : 0xFF55FF55);
                if (filledExertionWidth > 0) {
                    guiGraphics.fill(infoX, exertionBarY, infoX + filledExertionWidth, exertionBarY + exertionBarHeight, exertionBarColor);
                }
                curY += exertionBarHeight + 3;

                if (mouseX >= infoX && mouseX <= infoX + barWidth && mouseY >= exertionLabelY && mouseY <= exertionBarY + exertionBarHeight + 2) {
                    exertionTooltip = new ArrayList<>();
                    exertionTooltip.add(Component.literal("§6Stamina & Muscle: " + statusColor + String.format("%.0f%% Capacity", 100.0f - exertion)));
                    if (isFailure) {
                        exertionTooltip.add(Component.literal("§c⚠ Muscle Failure: -50% Attack Speed & -50% Digging Speed"));
                        exertionTooltip.add(Component.literal("§e⚡ Stand still & release mouse for 3–4s to recover 100%"));
                    } else if (exertion > 0.0f) {
                        exertionTooltip.add(Component.literal(String.format("§7Exertion: %.0f%% (Continuous labor/jumping/upstream)", exertion)));
                        exertionTooltip.add(Component.literal("§8Recovers passively or completely when resting"));
                    } else {
                        exertionTooltip.add(Component.literal("§a✔ Muscles Rested: Full attack & mining efficiency"));
                    }
                    if (isCramped) {
                        exertionTooltip.add(Component.literal("§c⚠ Swimming Cramp: -40% Swim Speed"));
                        exertionTooltip.add(Component.literal("§e⚡ Exit to shore or boat for 3s to relieve cramp"));
                    } else if (physData.getContinuousSwimmingTicks() > 200) {
                        int crampThreshold = ServerConfig.SWIMMING_CRAMP_THRESHOLD_TICKS != null ? ServerConfig.SWIMMING_CRAMP_THRESHOLD_TICKS.get() : 600;
                        int remTicks = Math.max(0, crampThreshold - physData.getContinuousSwimmingTicks());
                        exertionTooltip.add(Component.literal(String.format("§7Swimming: %.1fs until leg cramp", remTicks / 20.0)));
                    }
                }
            }

            int bpm = calculateHeartRateBpm(player, physData);
            String bpmColor;
            String bpmLabel;
            if (bpm >= 140) {
                bpmColor = "§c";
                bpmLabel = "Tachycardia";
            } else if (bpm >= 110) {
                bpmColor = "§6";
                bpmLabel = "Elevated";
            } else if (bpm >= 80) {
                bpmColor = "§e";
                bpmLabel = "Moderate";
            } else {
                bpmColor = "§a";
                bpmLabel = "Normal";
            }

            int hrLabelY = curY;
            String hrText = "§7Heart Rate: " + bpmColor + "❤ " + bpm + " BPM §8(" + bpmLabel + "§8)";
            guiGraphics.drawString(this.font, hrText, infoX, curY, 0xFFFFFF, false);
            curY += 9;

            int hrBarHeight = 2;
            int hrBarY = curY;
            guiGraphics.fill(infoX, hrBarY, infoX + barWidth, hrBarY + hrBarHeight, 0xFF101010);
            guiGraphics.renderOutline(infoX, hrBarY, barWidth, hrBarHeight, 0xFF282828);

            float hrFraction = Math.max(0.0f, Math.min(1.0f, (bpm - 55.0f) / 140.0f));
            int filledHrWidth = (int) (barWidth * hrFraction);
            int hrBarColor = bpm >= 140 ? 0xFFFF3333 : (bpm >= 110 ? 0xFFFFAA00 : (bpm >= 80 ? 0xFFFFFF55 : 0xFF55FF55));
            if (filledHrWidth > 0) {
                guiGraphics.fill(infoX, hrBarY, infoX + filledHrWidth, hrBarY + hrBarHeight, hrBarColor);
            }
            curY += hrBarHeight + 3;

            if (mouseX >= infoX && mouseX <= infoX + barWidth && mouseY >= hrLabelY && mouseY <= hrBarY + hrBarHeight + 2) {
                heartRateTooltip = new ArrayList<>();
                heartRateTooltip.add(Component.literal("§c❤ Heart Rate: " + bpmColor + bpm + " BPM (" + bpmLabel + ")"));
                heartRateTooltip.add(Component.literal("§7Resting Baseline: ~68 BPM"));
                double exertionBpm = (physData.getExertionScore() / 100.0) * 45.0;
                if (exertionBpm > 0.5) {
                    heartRateTooltip.add(Component.literal(String.format("§7• Exertion / Labor: +%.0f BPM", exertionBpm)));
                }
                float maxHp = player.getMaxHealth();
                float curHp = player.getHealth();
                if (maxHp > 0.0f && curHp < maxHp) {
                    double traumaBpm = (1.0f - (curHp / maxHp)) * 40.0;
                    heartRateTooltip.add(Component.literal(String.format("§c• Trauma / Low HP: +%.0f BPM", traumaBpm)));
                }
                if (physData.getToxinBuildup() > 0.0f) {
                    double toxinBpm = (physData.getToxinBuildup() / 100.0) * 25.0;
                    heartRateTooltip.add(Component.literal(String.format("§2• Toxin Cardiovascular Strain: +%.0f BPM", toxinBpm)));
                }
                if (physData.getFatigueScore() > 0.1f) {
                    double fatigueBpm = physData.getFatigueScore() * 15.0;
                    heartRateTooltip.add(Component.literal(String.format("§6• Fatigue Stress: +%.0f BPM", fatigueBpm)));
                }
                if (physData.isPanicking()) {
                    heartRateTooltip.add(Component.literal("§c• Underground Claustrophobia: +45 BPM"));
                }
                if (physData.isAdrenalineActive()) {
                    heartRateTooltip.add(Component.literal("§c• Adrenaline Surge: +60 BPM"));
                }
                if (physData.getIndigestionBuildup() >= 50.0f) {
                    heartRateTooltip.add(Component.literal("§6• Gut Discomfort / Indigestion: +10 BPM"));
                }
                heartRateTooltip.add(Component.literal("§8Elevated heart rate accelerates physical exhaustion."));
            }

            // Compact metabolic and mental indicators
            List<String> tags = new ArrayList<>();
            if (physData.isAdrenalineActive()) {
                tags.add("§c⚡ Adrenaline (" + String.format("%.1fs", physData.getAdrenalineTicks() / 20.0) + ")");
            } else if (physData.isPanicking()) {
                tags.add("§c⚠ Panic (0 Lux)");
            }
            if (physData.getPendingNutrition() > 0.0f) {
                tags.add("§aDigesting (+" + String.format("%.0f", physData.getPendingNutrition()) + "🍗)");
            }
            if (physData.getIndigestionBuildup() > 0.0f) {
                tags.add("§6Indigestion (" + String.format("%.0f%%", physData.getIndigestionBuildup()) + ")");
            }
            if (physData.hasCalciumBuff()) {
                long cSec = physData.getCalciumAbsorptionTicks() / 20;
                tags.add("§bCalcium (" + (cSec / 60) + "m " + (cSec % 60) + "s)");
            }

            if (ServerConfig.ENABLE_TEMPERATURE.get()) {
                PlayerTemperatureData tempData = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
                if (tempData.getWetnessBuffer() > 0.0f) {
                    tags.add("§bWet (" + String.format("%.0f%%", tempData.getWetnessBuffer() * 100.0f) + ")");
                }
                if (tempData.isNearHeatSource()) {
                    tags.add("§6Near Heat (Rewarming)");
                }
                if (player.getTicksFrozen() > 0) {
                    tags.add("§c❄ Freezing (" + player.getTicksFrozen() + "t)");
                }
            }

            int tagStartY = curY;
            if (!tags.isEmpty() && curY < topPos + 118) {
                for (String tag : tags) {
                    if (curY + 9 > topPos + 120) break;
                    guiGraphics.drawString(this.font, tag, infoX, curY, 0xFFFFFF, false);
                    curY += 9;
                }
            } else if (curY < topPos + 118) {
                guiGraphics.drawString(this.font, "§8Metabolism: Normal (Resting)", infoX, curY, 0x888888, false);
                curY += 9;
            }

            if (mouseX >= infoX && mouseX <= infoX + barWidth && mouseY >= tagStartY && mouseY <= Math.max(curY, tagStartY + 9)) {
                metabolismTooltip = new ArrayList<>();
                metabolismTooltip.add(Component.literal("§6Metabolic & Sensory Vitals"));
                if (physData.getPendingNutrition() > 0.0f || physData.getPendingSaturation() > 0.0f) {
                    metabolismTooltip.add(Component.literal(String.format("§a• Digestion Buffer: +%.0f Nutrition / +%.1f Saturation",
                            physData.getPendingNutrition(), physData.getPendingSaturation())));
                    metabolismTooltip.add(Component.literal("§7  Absorption ticks in every 1.5s to prevent meal spam."));
                } else {
                    metabolismTooltip.add(Component.literal("§a• Digestion: Clear / Resting"));
                }

                if (physData.hasCalciumBuff()) {
                    long calSec = physData.getCalciumAbsorptionTicks() / 20;
                    metabolismTooltip.add(Component.literal(String.format("§b• Calcium Absorption: %02d:%02d remaining", calSec / 60, calSec % 60)));
                    metabolismTooltip.add(Component.literal("§7  Doubles natural fracture healing speed during rest & sleep."));
                } else {
                    metabolismTooltip.add(Component.literal("§8• Calcium Buff: Inactive (Drink Hot Bone Broth or Milk)"));
                }

                if (physData.getIndigestionBuildup() > 0.0f) {
                    String crampTxt = physData.getIndigestionBuildup() >= 50.0f ? "§c[CRAMPING ACTIVE]" : "§6[Mild Discomfort]";
                    metabolismTooltip.add(Component.literal(String.format("§6• Indigestion Buildup: %.0f%% %s", physData.getIndigestionBuildup(), crampTxt)));
                    metabolismTooltip.add(Component.literal("§7  Caused by raw chicken/spoiled meat. Remedy with milk or honey."));
                }

                if (physData.isAdrenalineActive()) {
                    float remSec = physData.getAdrenalineTicks() / 20.0f;
                    metabolismTooltip.add(Component.literal(String.format("§c• Adrenaline Rush: %.1fs remaining", remSec)));
                    metabolismTooltip.add(Component.literal("§a  +35% Speed Burst & Sprint Unlocked overriding fractures."));
                    metabolismTooltip.add(Component.literal("§c⚠ Pain Shock collapse occurs immediately upon rush expiry!"));
                }

                if (physData.isPanicking() || physData.getClaustrophobiaTicks() > 0) {
                    int pThresh = ServerConfig.PANIC_THRESHOLD_TICKS != null ? ServerConfig.PANIC_THRESHOLD_TICKS.get() : 100;
                    int pPct = Math.min(100, Math.round((physData.getClaustrophobiaTicks() / (float) pThresh) * 100.0f));
                    metabolismTooltip.add(Component.literal(String.format("§c• Underground Claustrophobia: %d%% %s", pPct,
                            physData.isPanicking() ? "§c[PANIC - AIM TREMORS]" : "§e[Building]")));
                    metabolismTooltip.add(Component.literal("§7  Triggers at Y < 0 in pitch black. Calm near torches, fires, music, or pets."));
                }

                if (ServerConfig.ENABLE_TEMPERATURE.get()) {
                    PlayerTemperatureData tempData = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
                    float wetness = tempData.getWetnessBuffer();
                    int ticksFrozen = player.getTicksFrozen();
                    boolean isNearHeat = tempData.isNearHeatSource();
                    String wStatus = wetness >= 1.0f ? "§c[100% SOAKED]" : (wetness > 0.0f ? String.format("§b%.0f%%", wetness * 100.0f) : "§a0% (Dry)");
                    metabolismTooltip.add(Component.literal("§b• Wetness: " + wStatus));
                    if (isNearHeat) {
                        metabolismTooltip.add(Component.literal("§6  ↳ Near Heat Source: Rapid drying (~12.5s) & 5x thaw"));
                    }
                    if (ticksFrozen > 0) {
                        metabolismTooltip.add(Component.literal(String.format("§c  ↳ Freezing Ticks: %d / %d (Hypothermia)", ticksFrozen, player.getTicksRequiredToFreeze())));
                    }
                    float red = TemperatureEventHandler.calculateFreezeImmuneReduction(player);
                    boolean noHelm = player.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
                    metabolismTooltip.add(Component.literal(String.format("§7  ↳ Insulation: %.0f%% reduction | Head: %s", red * 100.0f, noHelm ? "§cMissing (2x Soak)" : "§aProtected")));
                }
            }
        }

        int splintDividerY = Math.max(curY + 2, topPos + 124);
        guiGraphics.fill(infoX, splintDividerY, leftPos + GUI_WIDTH - 12, splintDividerY + 1, 0xFF353535);
        ItemStack availableSplint = InjuryEventHandler.findFirstSplint(player);
        boolean hasSplint = !availableSplint.isEmpty();
        boolean isOnCooldown = hasSplint && player.getCooldowns().isOnCooldown(availableSplint.getItem());

        if (hasSplint) {
            if (isOnCooldown) {
                guiGraphics.drawString(this.font, "§c⌛ Treating...", infoX, splintDividerY + 5, 0xFF6666, false);
            } else {
                guiGraphics.drawString(this.font, "§eClick limb to treat", infoX, splintDividerY + 5, 0xFFFF55, false);
            }
            String splintName = this.font.plainSubstrByWidth(availableSplint.getHoverName().getString(), infoWidth - 36);
            guiGraphics.drawString(this.font, "§7Using: " + splintName, infoX, splintDividerY + 17, 0xAAAAAA, false);
        } else {
            guiGraphics.drawString(this.font, "§8No splint in inventory", infoX, splintDividerY + 5, 0x888888, false);
            guiGraphics.drawString(this.font, "§8[E] Return to Inventory", infoX, splintDividerY + 17, 0x666666, false);
        }

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
                    case MAIN_ARM -> tooltip.add(Component.literal("§cPenalty: -50% Attack/Mine Speed, Spasm Drop, Draw Lock"));
                    case OFF_ARM -> tooltip.add(Component.literal("§cPenalty: Offhand Slot Locked, Shield Block Disabled"));
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
        } else if (toxinTooltip != null) {
            guiGraphics.renderComponentTooltip(this.font, toxinTooltip, mouseX, mouseY);
        } else if (physTooltip != null) {
            guiGraphics.renderComponentTooltip(this.font, physTooltip, mouseX, mouseY);
        } else if (exertionTooltip != null) {
            guiGraphics.renderComponentTooltip(this.font, exertionTooltip, mouseX, mouseY);
        } else if (heartRateTooltip != null) {
            guiGraphics.renderComponentTooltip(this.font, heartRateTooltip, mouseX, mouseY);
        } else if (metabolismTooltip != null) {
            guiGraphics.renderComponentTooltip(this.font, metabolismTooltip, mouseX, mouseY);
        }

        RenderSystem.enableDepthTest();
    }

    public static int calculateHeartRateBpm(Player player, PlayerPhysiologyData physData) {
        if (player == null || physData == null) return 68;

        double bpm = 68.0;
        bpm += (physData.getExertionScore() / 100.0) * 45.0;

        float maxHp = player.getMaxHealth();
        float curHp = player.getHealth();
        if (maxHp > 0.0f) {
            float healthFraction = Math.max(0.0f, Math.min(1.0f, curHp / maxHp));
            bpm += (1.0f - healthFraction) * 40.0;
        }

        bpm += (Math.min(100.0f, physData.getToxinBuildup()) / 100.0) * 25.0;
        bpm += physData.getFatigueScore() * 15.0;

        if (physData.isPanicking()) {
            bpm += 45.0;
        }

        if (physData.isAdrenalineActive()) {
            bpm += 60.0;
        }

        if (physData.getIndigestionBuildup() >= 50.0f) {
            bpm += 10.0;
        }

        return (int) Math.round(Math.max(55.0, Math.min(205.0, bpm)));
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
        if (ModKeyMappings.OPEN_DIAGNOSTICS.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

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
