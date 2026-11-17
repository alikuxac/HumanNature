package com.alikuxac.humannature.client.gui;

import com.alikuxac.humannature.client.ModKeyMappings;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.init.ModEffects;
import com.alikuxac.humannature.init.ModEnchantments;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.item.SplintTier;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.network.NetworkHandler;
import com.alikuxac.humannature.network.TreatLimbPayload;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import com.alikuxac.humannature.modules.injury.TraumaSeverity;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.modules.physiology.ToxinType;
import com.alikuxac.humannature.modules.temperature.PlayerTemperatureData;
import com.alikuxac.humannature.modules.temperature.TemperatureEventHandler;
import com.alikuxac.humannature.modules.temperature.TemperatureModule;
import com.alikuxac.humannature.modules.weight.EncumbranceTier;
import com.alikuxac.humannature.modules.weight.PlayerWeightData;
import com.alikuxac.humannature.modules.weight.WeightCalculator;
import com.alikuxac.humannature.modules.weight.WeightModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.ChatFormatting;
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
        super(Component.translatable("gui.humannature.diagnostic.title"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.humannature.diagnostic.back_inventory"),
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

        guiGraphics.drawString(this.font, Component.translatable("gui.humannature.diagnostic.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), leftPos + 12, topPos + 10, 0xFFFFFF, false);
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

        int infoX = leftPos + 84;
        int infoWidth = (leftPos + GUI_WIDTH - 12) - infoX;
        int curY = topPos + 30;

        int totalFractures = 0;
        for (LimbType limb : LimbType.values()) {
            byte sev = data.getSeverity(limb);
            if (sev > 0) totalFractures++;
        }

        if (totalFractures == 0) {
            guiGraphics.drawString(this.font, Component.translatable("gui.humannature.diagnostic.all_healthy").withStyle(ChatFormatting.GREEN), infoX, curY, 0x55FF55, false);
            curY += 12;
        } else {
            guiGraphics.drawString(this.font, Component.translatable("gui.humannature.diagnostic.fractured_count", totalFractures).withStyle(ChatFormatting.RED), infoX, curY, 0xFF5555, false);
            curY += 13;

            for (LimbType limb : LimbType.values()) {
                byte sev = data.getSeverity(limb);
                if (sev > 0) {
                    ChatFormatting sevColor = sev >= 2 ? ChatFormatting.RED : ChatFormatting.GOLD;
                    Component sevName = Component.translatable(sev >= 2 ? "humannature.severity.broken" : "humannature.severity.sprained").withStyle(sevColor);
                    Component limbName = Component.translatable(limb.getTranslationKey()).withStyle(ChatFormatting.WHITE);
                    Component line = Component.translatable("gui.humannature.diagnostic.limb_status", limbName, sevName).withStyle(ChatFormatting.WHITE);
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
        List<Component> weightTooltip = null;

        if (ServerConfig.ENABLE_WEIGHT.get() || ServerConfig.ENABLE_PHYSIOLOGY.get()) {
            guiGraphics.fill(infoX, curY, leftPos + GUI_WIDTH - 12, curY + 1, 0xFF353535);
            curY += 3;
        }

        if (ServerConfig.ENABLE_WEIGHT.get()) {
            PlayerWeightData weightData = WeightModule.getOrCreateWeightData(player.getUUID());
            float curWeight = weightData.getCachedWeight();
            if (curWeight <= 0.0f) {
                curWeight = WeightCalculator.calculateWeight(player);
                weightData.setCachedWeight(curWeight);
            }
            float maxWeight = WeightCalculator.calculateMaxCapacity(player);
            float baseWeight = ServerConfig.WEIGHT_MAX_CAPACITY != null ? ServerConfig.WEIGHT_MAX_CAPACITY.get().floatValue() : 500.0f;
            float ratio = maxWeight > 0 ? curWeight / maxWeight : 0.0f;
            float weightPct = ratio * 100.0f;
            EncumbranceTier tier = EncumbranceTier.fromWeightRatio(ratio);

            ChatFormatting tierColor = tier.getChatFormatting();
            Component curWeightComp = Component.literal(String.format(java.util.Locale.ROOT, "%.1f", curWeight)).withStyle(tierColor);
            Component maxWeightComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f", maxWeight)).withStyle(ChatFormatting.GRAY);
            Component tierComp = Component.translatable(tier.getTranslationKey()).withStyle(tierColor);

            Component weightText = Component.translatable("gui.humannature.diagnostic.weight_bar",
                    curWeightComp,
                    maxWeightComp,
                    tierComp).withStyle(ChatFormatting.GRAY);
            int weightLabelY = curY;
            guiGraphics.drawString(this.font, weightText, infoX, curY, 0xFFFFFF, false);
            curY += 9;

            int barHeight = 2;
            int barY = curY;
            int barWidth = infoWidth;
            guiGraphics.fill(infoX, barY, infoX + barWidth, barY + barHeight, 0xFF101010);
            guiGraphics.renderOutline(infoX, barY, barWidth, barHeight, 0xFF282828);

            int filledWeightWidth = (int) (barWidth * Math.min(1.0f, Math.max(0.0f, ratio)));
            int barColor = switch (tier) {
                case OVERBURDENED -> 0xFFFF4444;
                case MEDIUM -> 0xFFFFAA00;
                case LIGHT -> 0xFFFFFF55;
                default -> 0xFF55FF55;
            };
            if (filledWeightWidth > 0) {
                guiGraphics.fill(infoX, barY, infoX + filledWeightWidth, barY + barHeight, barColor);
            }
            curY += barHeight + 3;

            if (mouseX >= infoX && mouseX <= infoX + barWidth && mouseY >= weightLabelY && mouseY <= barY + barHeight + 2) {
                weightTooltip = new ArrayList<>();
                Component pctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.1f%%", weightPct)).withStyle(tierColor);
                weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.header",
                        curWeightComp,
                        Component.literal(String.format(java.util.Locale.ROOT, "%.1f", maxWeight)).withStyle(ChatFormatting.GOLD),
                        pctComp).withStyle(ChatFormatting.GOLD));
                weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.status", tierComp).withStyle(ChatFormatting.GRAY));

                if (Math.abs(maxWeight - baseWeight) > 0.5f) {
                    weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.base_capacity", String.format(java.util.Locale.ROOT, "%.0f", baseWeight)).withStyle(ChatFormatting.GRAY));
                    if (ModEffects.BURDEN_BEARER != null) {
                        var holder = ModEffects.BURDEN_BEARER.get();
                        if (holder != null) {
                            var burden = player.getEffect(holder);
                            if (burden != null) {
                                double bonus = (burden.getAmplifier() + 1) * ServerConfig.BURDEN_BEARER_CAPACITY_BONUS;
                                weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.burden_bearer", String.format(java.util.Locale.ROOT, "%.0f", bonus)).withStyle(ChatFormatting.GREEN));
                            }
                        }
                    }
                    int totalLoadBearing = 0;
                    for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                        totalLoadBearing += WeightCalculator.getEnchantmentLevel(player.getItemBySlot(slot), ModEnchantments.LOAD_BEARING);
                    }
                    if (totalLoadBearing > 0) {
                        double bonus = totalLoadBearing * ServerConfig.LOAD_BEARING_CAPACITY_BONUS;
                        weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.load_bearing", String.format(java.util.Locale.ROOT, "%.0f", bonus)).withStyle(ChatFormatting.AQUA));
                    }
                }
                if (tier == EncumbranceTier.NORMAL) {
                    weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.unencumbered").withStyle(ChatFormatting.GREEN));
                } else {
                    weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.penalty_speed", String.format(java.util.Locale.ROOT, "%.0f", tier.getSpeedPenalty() * 100.0f)).withStyle(ChatFormatting.RED));
                    weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.penalty_sink", String.format(java.util.Locale.ROOT, "%.0f", tier.getSinkPull() * 1000.0f)).withStyle(ChatFormatting.RED));
                    weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.penalty_fall", String.format(java.util.Locale.ROOT, "%.2f", tier.getFallDamageMultiplier())).withStyle(ChatFormatting.RED));
                }
                weightTooltip.add(Component.translatable("gui.humannature.tooltip.weight.thresholds").withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
            PlayerPhysiologyData physData = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
            int barWidth = infoWidth;

            if (PhysiologyModule.isSleepDeprivationEnabled()) {
                float fatigueScore = physData.getFatigueScore();
                float fatiguePct = physData.getFatiguePercentage();

                ChatFormatting fatigueFormatting;
                String fatigueKey;
                if (fatigueScore >= 0.80f) {
                    fatigueFormatting = ChatFormatting.RED;
                    fatigueKey = "humannature.fatigue.drowsy";
                } else if (fatigueScore >= 0.50f) {
                    fatigueFormatting = ChatFormatting.GOLD;
                    fatigueKey = "humannature.fatigue.tired";
                } else if (fatigueScore >= 0.20f) {
                    fatigueFormatting = ChatFormatting.YELLOW;
                    fatigueKey = "humannature.fatigue.mild";
                } else {
                    fatigueFormatting = ChatFormatting.GREEN;
                    fatigueKey = "humannature.fatigue.rested";
                }

                int physLabelY = curY;
                Component fatiguePctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", fatiguePct)).withStyle(fatigueFormatting);
                Component fatigueLabelComp = Component.translatable(fatigueKey).withStyle(fatigueFormatting);
                Component physText = Component.translatable("gui.humannature.diagnostic.fatigue_bar",
                        fatiguePctComp,
                        fatigueLabelComp).withStyle(ChatFormatting.GRAY);
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
                    Component headerPct = Component.literal(String.format(java.util.Locale.ROOT, "%.1f%%", fatiguePct)).withStyle(fatigueFormatting);
                    physTooltip.add(Component.translatable("gui.humannature.tooltip.fatigue.header", headerPct).withStyle(ChatFormatting.GOLD));
                    long awakeTicks = physData.getAwakeTicks();
                    double awakeDays = (double) awakeTicks / 24000.0;
                    physTooltip.add(Component.translatable("gui.humannature.tooltip.fatigue.awake", String.format(java.util.Locale.ROOT, "%.1f", awakeDays)).withStyle(ChatFormatting.GRAY));
                    if (physData.isBlinkEligible()) {
                        physTooltip.add(Component.translatable("gui.humannature.tooltip.fatigue.microsleep_active").withStyle(ChatFormatting.RED));
                    } else {
                        physTooltip.add(Component.translatable("gui.humannature.tooltip.fatigue.microsleep_inactive").withStyle(ChatFormatting.GREEN));
                    }
                    physTooltip.add(Component.translatable("gui.humannature.tooltip.fatigue.reset_hint").withStyle(ChatFormatting.DARK_GRAY));
                }
            }

            if (PhysiologyModule.isToxinBuildupEnabled()) {
                float toxinBuildup = physData.getToxinBuildup();
                ToxinType toxinType = physData.getLastToxinType();

                ChatFormatting toxinFormatting = toxinType == ToxinType.WITHER ? ChatFormatting.DARK_PURPLE : (toxinBuildup > 0.0f ? ChatFormatting.GREEN : ChatFormatting.GREEN);
                Component toxinPctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", toxinBuildup)).withStyle(toxinFormatting);
                Component toxinLabelComp = Component.translatable(toxinType == ToxinType.NONE ? "humannature.toxin.none" : toxinType.getTranslationKey()).withStyle(toxinFormatting);

                Component toxinText = Component.translatable("gui.humannature.diagnostic.toxin_bar",
                        toxinPctComp,
                        toxinLabelComp).withStyle(ChatFormatting.GRAY);

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
                    Component toxinHeaderVal = toxinBuildup > 0.0f
                            ? Component.literal(String.format(java.util.Locale.ROOT, "%.1f%%", toxinBuildup)).withStyle(toxinFormatting)
                            : Component.translatable("gui.humannature.tooltip.toxin.clear").withStyle(ChatFormatting.GREEN);
                    toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.header", toxinHeaderVal).withStyle(ChatFormatting.GOLD));
                    float toxinMultiplier = PhysiologyModule.calculateToxinMultiplier(player.getUUID());
                    if (toxinBuildup > 0.0f) {
                        Component accTypeComp = Component.translatable(toxinType.getTranslationKey()).withStyle(toxinFormatting);
                        toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.accumulated", accTypeComp).withStyle(ChatFormatting.GRAY));
                        int delayTicks = ServerConfig.TOXIN_DECAY_DELAY_TICKS;
                        int elapsed = physData.getTicksSinceLastExposure();
                        if (elapsed < delayTicks) {
                            toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.decay_delay", String.format(java.util.Locale.ROOT, "%.1f", (delayTicks - elapsed) / 20.0)).withStyle(ChatFormatting.GRAY));
                        } else {
                            toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.decay_active").withStyle(ChatFormatting.GREEN));
                        }
                        toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.heat_resting").withStyle(ChatFormatting.YELLOW));
                        toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.remedy").withStyle(ChatFormatting.AQUA));
                        if (toxinMultiplier > 1.01f) {
                            toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.vulnerability", String.format(java.util.Locale.ROOT, "%.1f", toxinMultiplier)).withStyle(ChatFormatting.RED));
                        }
                        toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.imminent_outbreak").withStyle(ChatFormatting.RED));
                    } else {
                        toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.free").withStyle(ChatFormatting.GREEN));
                        if (toxinMultiplier > 1.01f) {
                            toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.vulnerability", String.format(java.util.Locale.ROOT, "%.1f", toxinMultiplier)).withStyle(ChatFormatting.RED));
                        }
                        toxinTooltip.add(Component.translatable("gui.humannature.tooltip.toxin.erupt_hint").withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            }

            if (PhysiologyModule.isExertionEnabled()) {
                float exertion = physData.getExertionScore();
                boolean isFailure = physData.isMuscleFailure();
                boolean isCramped = physData.isSwimmingCramped();

                ChatFormatting staminaFormatting;
                String statusKey;
                if (isFailure) {
                    staminaFormatting = ChatFormatting.RED;
                    statusKey = "humannature.stamina.failure";
                } else if (exertion >= 50.0f) {
                    staminaFormatting = ChatFormatting.GOLD;
                    statusKey = "humannature.stamina.strained";
                } else {
                    staminaFormatting = ChatFormatting.GREEN;
                    statusKey = "humannature.stamina.rested";
                }

                Component parenLabel = isCramped
                        ? Component.translatable("humannature.stamina.cramp").withStyle(ChatFormatting.RED)
                        : Component.translatable(statusKey).withStyle(staminaFormatting);
                Component staminaPctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", 100.0f - exertion)).withStyle(staminaFormatting);
                Component exertionText = Component.translatable("gui.humannature.diagnostic.stamina_bar",
                        staminaPctComp,
                        parenLabel).withStyle(ChatFormatting.GRAY);

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
                    Component capComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", 100.0f - exertion)).withStyle(staminaFormatting);
                    exertionTooltip.add(Component.translatable("gui.humannature.tooltip.stamina.header", capComp).withStyle(ChatFormatting.GOLD));
                    if (isFailure) {
                        exertionTooltip.add(Component.translatable("gui.humannature.tooltip.stamina.failure").withStyle(ChatFormatting.RED));
                        exertionTooltip.add(Component.translatable("gui.humannature.tooltip.stamina.failure_recovery").withStyle(ChatFormatting.YELLOW));
                    } else if (exertion > 0.0f) {
                        exertionTooltip.add(Component.translatable("gui.humannature.tooltip.stamina.exertion", String.format(java.util.Locale.ROOT, "%.0f", exertion)).withStyle(ChatFormatting.GRAY));
                        exertionTooltip.add(Component.translatable("gui.humannature.tooltip.stamina.recovery_passive").withStyle(ChatFormatting.DARK_GRAY));
                    } else {
                        exertionTooltip.add(Component.translatable("gui.humannature.tooltip.stamina.rested").withStyle(ChatFormatting.GREEN));
                    }
                    if (isCramped) {
                        exertionTooltip.add(Component.translatable("gui.humannature.tooltip.stamina.cramp").withStyle(ChatFormatting.RED));
                        exertionTooltip.add(Component.translatable("gui.humannature.tooltip.stamina.cramp_relief").withStyle(ChatFormatting.YELLOW));
                    } else if (physData.getContinuousSwimmingTicks() > 200) {
                        int crampThreshold = ServerConfig.SWIMMING_CRAMP_THRESHOLD_TICKS;
                        int remTicks = Math.max(0, crampThreshold - physData.getContinuousSwimmingTicks());
                        exertionTooltip.add(Component.translatable("gui.humannature.tooltip.stamina.swimming_countdown", String.format(java.util.Locale.ROOT, "%.1f", remTicks / 20.0)).withStyle(ChatFormatting.GRAY));
                    }
                }
            }

            int bpm = calculateHeartRateBpm(player, physData);
            ChatFormatting bpmFormatting;
            String bpmKey;
            if (bpm >= 140) {
                bpmFormatting = ChatFormatting.RED;
                bpmKey = "humannature.heart.tachycardia";
            } else if (bpm >= 110) {
                bpmFormatting = ChatFormatting.GOLD;
                bpmKey = "humannature.heart.elevated";
            } else if (bpm >= 80) {
                bpmFormatting = ChatFormatting.YELLOW;
                bpmKey = "humannature.heart.moderate";
            } else {
                bpmFormatting = ChatFormatting.GREEN;
                bpmKey = "humannature.heart.normal";
            }

            int hrLabelY = curY;
            Component bpmComp = Component.literal("❤ " + bpm + " BPM").withStyle(bpmFormatting);
            Component bpmLabelComp = Component.translatable(bpmKey).withStyle(bpmFormatting);
            Component hrText = Component.translatable("gui.humannature.diagnostic.heart_bar", bpmComp, bpmLabelComp).withStyle(ChatFormatting.GRAY);
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
                heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.header", bpmComp, bpmLabelComp).withStyle(ChatFormatting.RED));
                heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.baseline").withStyle(ChatFormatting.GRAY));
                double exertionBpm = (physData.getExertionScore() / 100.0) * 45.0;
                if (exertionBpm > 0.5) {
                    heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.exertion", String.format(java.util.Locale.ROOT, "%.0f", exertionBpm)).withStyle(ChatFormatting.GRAY));
                }
                float maxHp = player.getMaxHealth();
                float curHp = player.getHealth();
                if (maxHp > 0.0f && curHp < maxHp) {
                    double traumaBpm = (1.0f - (curHp / maxHp)) * 40.0;
                    heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.trauma", String.format(java.util.Locale.ROOT, "%.0f", traumaBpm)).withStyle(ChatFormatting.RED));
                }
                if (physData.getToxinBuildup() > 0.0f) {
                    double toxinBpm = (physData.getToxinBuildup() / 100.0) * 25.0;
                    heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.toxin", String.format(java.util.Locale.ROOT, "%.0f", toxinBpm)).withStyle(ChatFormatting.DARK_GREEN));
                }
                if (physData.getFatigueScore() > 0.1f) {
                    double fatigueBpm = physData.getFatigueScore() * 15.0;
                    heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.fatigue", String.format(java.util.Locale.ROOT, "%.0f", fatigueBpm)).withStyle(ChatFormatting.GOLD));
                }
                if (physData.isPanicking()) {
                    heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.claustrophobia").withStyle(ChatFormatting.RED));
                }
                if (physData.isAdrenalineActive()) {
                    heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.adrenaline").withStyle(ChatFormatting.RED));
                }
                if (physData.getIndigestionBuildup() >= 50.0f) {
                    heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.indigestion").withStyle(ChatFormatting.GOLD));
                }
                heartRateTooltip.add(Component.translatable("gui.humannature.tooltip.heart.exhaustion_warning").withStyle(ChatFormatting.DARK_GRAY));
            }

            List<Component> tags = new ArrayList<>();
            if (physData.isAdrenalineActive()) {
                tags.add(Component.translatable("gui.humannature.tag.adrenaline", String.format(java.util.Locale.ROOT, "%.1f", physData.getAdrenalineTicks() / 20.0f)).withStyle(ChatFormatting.RED));
            } else if (physData.isPanicking()) {
                tags.add(Component.translatable("gui.humannature.tag.panic").withStyle(ChatFormatting.RED));
            }
            if (physData.getPendingNutrition() > 0.0f) {
                tags.add(Component.translatable("gui.humannature.tag.digesting", String.format(java.util.Locale.ROOT, "%.0f", physData.getPendingNutrition())).withStyle(ChatFormatting.GREEN));
            }
            if (physData.getIndigestionBuildup() > 0.0f) {
                tags.add(Component.translatable("gui.humannature.tag.indigestion", String.format(java.util.Locale.ROOT, "%.0f", physData.getIndigestionBuildup())).withStyle(ChatFormatting.GOLD));
            }
            if (physData.hasCalciumBuff()) {
                long cSec = physData.getCalciumAbsorptionTicks() / 20;
                tags.add(Component.translatable("gui.humannature.tag.calcium", cSec / 60, cSec % 60).withStyle(ChatFormatting.AQUA));
            }

            if (ServerConfig.ENABLE_TEMPERATURE.get()) {
                PlayerTemperatureData tempData = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
                float wetness = tempData.getWetnessBuffer();
                int ticksFrozen = player.getTicksFrozen();
                boolean hasBuff = tempData.hasInternalWarming();
                boolean nearHeat = tempData.isNearHeatSource();

                float coreTemp = tempData.getCoreTemperature();
                if (coreTemp > 42.0f) {
                    tags.add(Component.translatable("gui.humannature.tag.hyperthermia", String.format(java.util.Locale.ROOT, "%.1f", coreTemp)).withStyle(ChatFormatting.RED));
                } else if (coreTemp > 39.5f) {
                    tags.add(Component.translatable("gui.humannature.tag.heat_exhaustion", String.format(java.util.Locale.ROOT, "%.1f", coreTemp)).withStyle(ChatFormatting.RED));
                } else if (hasBuff) {
                    tags.add(Component.translatable("gui.humannature.tag.internal_warming", tempData.getInternalWarmingTicks() / 20).withStyle(ChatFormatting.GOLD));
                } else if (ticksFrozen >= (int) Math.round(player.getTicksRequiredToFreeze() * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get())) {
                    tags.add(Component.translatable("gui.humannature.tag.freezing").withStyle(ChatFormatting.RED));
                } else if (ticksFrozen > 0) {
                    tags.add(Component.translatable("gui.humannature.tag.chilly").withStyle(ChatFormatting.AQUA));
                }

                if (wetness >= 1.0f) {
                    tags.add(Component.translatable("gui.humannature.tag.soaked").withStyle(ChatFormatting.RED));
                } else if (wetness > 0.0f) {
                    tags.add(Component.translatable("gui.humannature.tag.damp", String.format(java.util.Locale.ROOT, "%.0f", wetness * 100.0f)).withStyle(ChatFormatting.AQUA));
                }

                if (nearHeat) {
                    tags.add(Component.translatable("gui.humannature.tag.near_heat").withStyle(ChatFormatting.GOLD));
                }
            }

            int tagStartY = curY;
            if (!tags.isEmpty() && curY < topPos + 118) {
                for (Component tag : tags) {
                    if (curY + 9 > topPos + 120) break;
                    guiGraphics.drawString(this.font, tag, infoX, curY, 0xFFFFFF, false);
                    curY += 9;
                }
            } else if (curY < topPos + 118) {
                guiGraphics.drawString(this.font, Component.translatable("gui.humannature.diagnostic.metabolism_normal").withStyle(ChatFormatting.DARK_GRAY), infoX, curY, 0x888888, false);
                curY += 9;
            }

            if (mouseX >= infoX && mouseX <= infoX + barWidth && mouseY >= tagStartY && mouseY <= Math.max(curY, tagStartY + 9)) {
                metabolismTooltip = new ArrayList<>();
                metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.title").withStyle(ChatFormatting.GOLD));
                if (physData.getPendingNutrition() > 0.0f || physData.getPendingSaturation() > 0.0f) {
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.digestion_buffer",
                            String.format(java.util.Locale.ROOT, "%.0f", physData.getPendingNutrition()),
                            String.format(java.util.Locale.ROOT, "%.1f", physData.getPendingSaturation())).withStyle(ChatFormatting.GREEN));
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.digestion_hint").withStyle(ChatFormatting.GRAY));
                } else {
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.digestion_resting").withStyle(ChatFormatting.GREEN));
                }

                if (physData.hasCalciumBuff()) {
                    long calSec = physData.getCalciumAbsorptionTicks() / 20;
                    String timeStr = String.format(java.util.Locale.ROOT, "%02d:%02d", calSec / 60, calSec % 60);
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.calcium_remaining", timeStr).withStyle(ChatFormatting.AQUA));
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.calcium_hint").withStyle(ChatFormatting.GRAY));
                } else {
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.calcium_inactive").withStyle(ChatFormatting.DARK_GRAY));
                }

                if (physData.getIndigestionBuildup() > 0.0f) {
                    Component crampTxt = physData.getIndigestionBuildup() >= 50.0f
                            ? Component.translatable("gui.humannature.tooltip.metabolism.indigestion_cramp_active").withStyle(ChatFormatting.RED)
                            : Component.translatable("gui.humannature.tooltip.metabolism.indigestion_mild").withStyle(ChatFormatting.GOLD);
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.indigestion_buildup",
                            String.format(java.util.Locale.ROOT, "%.0f", physData.getIndigestionBuildup()),
                            crampTxt).withStyle(ChatFormatting.GOLD));
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.indigestion_hint").withStyle(ChatFormatting.GRAY));
                }

                if (physData.isAdrenalineActive()) {
                    float remSec = physData.getAdrenalineTicks() / 20.0f;
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.adrenaline_remaining", String.format(java.util.Locale.ROOT, "%.1f", remSec)).withStyle(ChatFormatting.RED));
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.adrenaline_buff").withStyle(ChatFormatting.GREEN));
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.adrenaline_collapse").withStyle(ChatFormatting.RED));
                }

                if (physData.isPanicking() || physData.getClaustrophobiaTicks() > 0) {
                    int pThresh = ServerConfig.PANIC_THRESHOLD_TICKS;
                    int pPct = Math.min(100, Math.round((physData.getClaustrophobiaTicks() / (float) pThresh) * 100.0f));
                    Component pTxt = physData.isPanicking()
                            ? Component.translatable("gui.humannature.tooltip.metabolism.claustrophobia_panic").withStyle(ChatFormatting.RED)
                            : Component.translatable("gui.humannature.tooltip.metabolism.claustrophobia_building").withStyle(ChatFormatting.YELLOW);
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.claustrophobia_status", pPct, pTxt).withStyle(ChatFormatting.RED));
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.claustrophobia_hint").withStyle(ChatFormatting.GRAY));
                }

                if (ServerConfig.ENABLE_TEMPERATURE.get()) {
                    PlayerTemperatureData tempData = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
                    float wetness = tempData.getWetnessBuffer();
                    int ticksFrozen = player.getTicksFrozen();
                    boolean isNearHeat = tempData.isNearHeatSource();

                    float coreTemp = tempData.getCoreTemperature();
                    Component tempStatus;
                    if (coreTemp > 42.0f) {
                        tempStatus = Component.translatable("humannature.temperature.hyperthermia", String.format(java.util.Locale.ROOT, "%.1f", coreTemp)).withStyle(ChatFormatting.RED);
                    } else if (coreTemp > 39.5f) {
                        tempStatus = Component.translatable("humannature.temperature.heat_exhaustion", String.format(java.util.Locale.ROOT, "%.1f", coreTemp)).withStyle(ChatFormatting.RED);
                    } else if (tempData.hasInternalWarming()) {
                        tempStatus = Component.translatable("humannature.temperature.warm_stew", String.format(java.util.Locale.ROOT, "%.1f", coreTemp)).withStyle(ChatFormatting.GOLD);
                    } else if (ticksFrozen >= (int) Math.round(player.getTicksRequiredToFreeze() * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get())) {
                        tempStatus = Component.translatable("humannature.temperature.freezing", String.format(java.util.Locale.ROOT, "%.1f", coreTemp)).withStyle(ChatFormatting.RED);
                    } else if (ticksFrozen > 0 || (wetness >= 0.5f && !isNearHeat)) {
                        tempStatus = Component.translatable("humannature.temperature.chilly", String.format(java.util.Locale.ROOT, "%.1f", coreTemp)).withStyle(ChatFormatting.AQUA);
                    } else {
                        tempStatus = Component.translatable("humannature.temperature.normal", String.format(java.util.Locale.ROOT, "%.1f", coreTemp)).withStyle(ChatFormatting.GREEN);
                    }

                    Component wStatus = wetness >= 1.0f
                            ? Component.translatable("humannature.wetness.soaked").withStyle(ChatFormatting.RED)
                            : (wetness > 0.0f
                            ? Component.translatable("humannature.wetness.damp", String.format(java.util.Locale.ROOT, "%.0f", wetness * 100.0f)).withStyle(ChatFormatting.AQUA)
                            : Component.translatable("humannature.wetness.dry").withStyle(ChatFormatting.GREEN));

                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.body_temp", tempStatus).withStyle(ChatFormatting.GOLD));
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.wetness", wStatus).withStyle(ChatFormatting.AQUA));

                    if (tempData.hasInternalWarming()) {
                        int remSec = tempData.getInternalWarmingTicks() / 20;
                        metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.internal_warming", remSec).withStyle(ChatFormatting.GOLD));
                    }
                    if (isNearHeat) {
                        metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.near_heat").withStyle(ChatFormatting.GOLD));
                    }
                    if (ticksFrozen > 0) {
                        metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.freezing_ticks", ticksFrozen, player.getTicksRequiredToFreeze()).withStyle(ChatFormatting.RED));
                    }
                    float red = TemperatureEventHandler.calculateFreezeImmuneReduction(player);
                    boolean noHelm = player.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
                    Component helmStatus = noHelm
                            ? Component.translatable("gui.humannature.tooltip.metabolism.insulation_missing").withStyle(ChatFormatting.RED)
                            : Component.translatable("gui.humannature.tooltip.metabolism.insulation_protected").withStyle(ChatFormatting.GREEN);
                    metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.insulation", String.format(java.util.Locale.ROOT, "%.0f", red * 100.0f), helmStatus).withStyle(ChatFormatting.GRAY));

                    int leatherCount = TemperatureEventHandler.countLeatherPieces(player);
                    int metalCount = TemperatureEventHandler.countMetallicPieces(player);
                    if (leatherCount > 0) {
                        double maxRed = ServerConfig.LEATHER_INSULATION_FREEZE_REDUCTION;
                        metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.leather_insulation",
                                String.format(java.util.Locale.ROOT, "%.0f", (leatherCount / 4.0f) * maxRed * 100.0f),
                                leatherCount).withStyle(ChatFormatting.GREEN));
                    }
                    if (metalCount > 0) {
                        double condMult = ServerConfig.METALLIC_ARMOR_COLD_CONDUCTION_MULTIPLIER;
                        float extraCond = (metalCount / 4.0f) * (float) (condMult - 1.0) * 100.0f;
                        metabolismTooltip.add(Component.translatable("gui.humannature.tooltip.metabolism.metal_cold",
                                String.format(java.util.Locale.ROOT, "%.0f", extraCond),
                                metalCount).withStyle(ChatFormatting.AQUA));
                    }
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
                guiGraphics.drawString(this.font, Component.translatable("gui.humannature.diagnostic.splint_treating").withStyle(ChatFormatting.RED), infoX, splintDividerY + 5, 0xFF6666, false);
            } else {
                guiGraphics.drawString(this.font, Component.translatable("gui.humannature.diagnostic.splint_click_treat").withStyle(ChatFormatting.YELLOW), infoX, splintDividerY + 5, 0xFFFF55, false);
            }
            String splintName = this.font.plainSubstrByWidth(availableSplint.getHoverName().getString(), infoWidth - 36);
            guiGraphics.drawString(this.font, Component.translatable("gui.humannature.diagnostic.splint_using", splintName).withStyle(ChatFormatting.GRAY), infoX, splintDividerY + 17, 0xAAAAAA, false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("gui.humannature.diagnostic.splint_none").withStyle(ChatFormatting.DARK_GRAY), infoX, splintDividerY + 5, 0x888888, false);
            guiGraphics.drawString(this.font, Component.translatable("gui.humannature.diagnostic.return_inventory").withStyle(ChatFormatting.DARK_GRAY), infoX, splintDividerY + 17, 0x666666, false);
        }

        if (hoveredBox != null) {
            byte sev = data.getSeverity(hoveredBox.limb());
            TraumaSeverity sevEnum = TraumaSeverity.fromLevel(sev);

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(hoveredBox.limb().getTranslationKey()).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("gui.humannature.tooltip.limb.status",
                    Component.translatable(sevEnum.getTranslationKey()).withStyle(sevEnum.getChatFormatting()),
                    sev).withStyle(ChatFormatting.GRAY));

            if (sev > 0) {
                switch (hoveredBox.limb()) {
                    case HEAD -> tooltip.add(Component.translatable(sev == 1 ? "gui.humannature.tooltip.limb.penalty_head_mild" : "gui.humannature.tooltip.limb.penalty_head_severe").withStyle(sev == 1 ? ChatFormatting.GRAY : ChatFormatting.RED));
                    case TORSO -> tooltip.add(Component.translatable(sev == 1 ? "gui.humannature.tooltip.limb.penalty_torso_mild" : "gui.humannature.tooltip.limb.penalty_torso_severe").withStyle(sev == 1 ? ChatFormatting.GRAY : ChatFormatting.RED));
                    case MAIN_ARM -> tooltip.add(Component.translatable("gui.humannature.tooltip.limb.penalty_main_arm").withStyle(ChatFormatting.RED));
                    case OFF_ARM -> tooltip.add(Component.translatable("gui.humannature.tooltip.limb.penalty_off_arm").withStyle(ChatFormatting.RED));
                    case LEFT_LEG, RIGHT_LEG -> tooltip.add(Component.translatable(sev == 1 ? "gui.humannature.tooltip.limb.penalty_leg_mild" : "gui.humannature.tooltip.limb.penalty_leg_severe").withStyle(sev == 1 ? ChatFormatting.GRAY : ChatFormatting.RED));
                }
                if (data.isSplinted(hoveredBox.limb())) {
                    SplintTier st = data.getSplint(hoveredBox.limb());
                    Component stName = st != null ? Component.translatable(st.getTranslationKey()) : Component.translatable("humannature.splint_tier.generic");
                    tooltip.add(Component.translatable("gui.humannature.tooltip.limb.splinted", stName, data.getSplintDurability(hoveredBox.limb())).withStyle(ChatFormatting.AQUA));
                    if (PhysiologyModule.hasCalciumBuff(player.getUUID())) {
                        tooltip.add(Component.translatable("gui.humannature.tooltip.limb.osteogenesis").withStyle(ChatFormatting.LIGHT_PURPLE));
                    }
                    if (hasSplint) {
                        if (isOnCooldown) {
                            tooltip.add(Component.translatable("gui.humannature.tooltip.limb.splint_cooldown").withStyle(ChatFormatting.RED));
                        } else {
                            tooltip.add(Component.translatable("gui.humannature.tooltip.limb.click_replace", availableSplint.getHoverName()).withStyle(ChatFormatting.YELLOW));
                        }
                    }
                } else if (hasSplint) {
                    if (isOnCooldown) {
                        tooltip.add(Component.translatable("gui.humannature.tooltip.limb.splint_cooldown").withStyle(ChatFormatting.RED));
                    } else {
                        tooltip.add(Component.translatable("gui.humannature.tooltip.limb.click_treat", availableSplint.getHoverName()).withStyle(ChatFormatting.YELLOW));
                    }
                } else {
                    tooltip.add(Component.translatable("gui.humannature.tooltip.limb.requires_splint").withStyle(ChatFormatting.GRAY));
                }
            } else {
                tooltip.add(Component.translatable("gui.humannature.tooltip.limb.no_fractures").withStyle(ChatFormatting.GREEN));
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
        } else if (weightTooltip != null) {
            guiGraphics.renderComponentTooltip(this.font, weightTooltip, mouseX, mouseY);
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

            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());

            for (LimbHitbox box : HITBOXES) {
                if (box.contains((int) mouseX, (int) mouseY, silX, silY)) {
                    if (data.getSeverity(box.limb()) > 0) {
                        ItemStack splint = InjuryEventHandler.findFirstSplint(player);
                        if (!splint.isEmpty()) {
                            InjuryEventHandler.handleQuickTreat(player, box.limb(), splint);
                            NetworkHandler.sendToServer(new TreatLimbPayload((byte) box.limb().getIndex()));
                            return true;
                        }
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
