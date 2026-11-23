package com.alikuxac.humannature.client;

import com.alikuxac.humannature.client.gui.DiagnosticAssets;
import com.alikuxac.humannature.client.gui.DiagnosticScreen;
import com.alikuxac.humannature.client.gui.HudAtlasRenderer;
import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import com.alikuxac.humannature.modules.physiology.PhysiologyEventHandler;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
        WEIGHT("WEIGHT", "§6"),
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

        public String getTranslationKey() {
            return "humannature.hud_mode." + name().toLowerCase(java.util.Locale.ROOT);
        }

        public String getColorCode() {
            return colorCode;
        }

        public ChatFormatting getChatFormatting() {
            return switch (this) {
                case OFF -> ChatFormatting.DARK_GRAY;
                case PHYSIOLOGY -> ChatFormatting.AQUA;
                case ENVIRONMENT -> ChatFormatting.YELLOW;
                case INJURY -> ChatFormatting.RED;
                case WEIGHT -> ChatFormatting.GOLD;
                case COMBINED -> ChatFormatting.GREEN;
            };
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
            Component modeComp = Component.translatable(currentMode.getTranslationKey()).withStyle(currentMode.getChatFormatting());
            Component message = Component.translatable(
                    "message.humannature.hud_mode",
                    modeComp
            ).withStyle(ChatFormatting.DARK_GRAY);
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

        float coreTemp = tempData.getCoreTemperature();
        boolean isNormal = (wetness <= 0.0f && ticksFrozen <= 0 && !hasBuff && !nearHeat && coreTemp <= 37.5f && coreTemp >= 36.5f);
        if (isNormal) {
            actionBarCooldown = 20;
            return;
        }

        actionBarCooldown = 70;

        Component wetComponent;
        if (wetness >= 1.0f) {
            wetComponent = Component.translatable("actionbar.humannature.wetness_soaked");
        } else if (wetness > 0.0f) {
            wetComponent = Component.translatable("actionbar.humannature.wetness_damp", String.format(java.util.Locale.ROOT, "%.0f", wetness * 100.0f));
        } else {
            wetComponent = Component.translatable("actionbar.humannature.wetness_dry");
        }

        Component tempComponent;
        ChatFormatting tempChatColor;
        int freezeThresh = (int) Math.round(player.getTicksRequiredToFreeze() * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get());
        String coreTempStr = String.format(java.util.Locale.ROOT, "%.1f", coreTemp);
        if (coreTemp > 42.0f) {
            tempComponent = Component.translatable("actionbar.humannature.temp_hyperthermia", coreTempStr);
            tempChatColor = ChatFormatting.RED;
        } else if (coreTemp > 39.5f) {
            tempComponent = Component.translatable("actionbar.humannature.temp_heat_exhaustion", coreTempStr);
            tempChatColor = ChatFormatting.RED;
        } else if (hasBuff) {
            tempComponent = Component.translatable("actionbar.humannature.temp_warm_buff");
            tempChatColor = ChatFormatting.GOLD;
        } else if (ticksFrozen >= freezeThresh) {
            tempComponent = Component.translatable("actionbar.humannature.temp_freezing");
            tempChatColor = ChatFormatting.RED;
        } else if (ticksFrozen > 0) {
            tempComponent = Component.translatable("actionbar.humannature.temp_chilly");
            tempChatColor = ChatFormatting.AQUA;
        } else if (nearHeat) {
            tempComponent = Component.translatable("actionbar.humannature.temp_rewarming");
            tempChatColor = ChatFormatting.GOLD;
        } else if (coreTemp > 37.5f) {
            tempComponent = Component.translatable("actionbar.humannature.temp_elevated", coreTempStr);
            tempChatColor = ChatFormatting.YELLOW;
        } else {
            tempComponent = Component.translatable("actionbar.humannature.temp_normal");
            tempChatColor = ChatFormatting.GREEN;
        }

        ChatFormatting wetChatColor = wetness >= 1.0f ? ChatFormatting.RED : (wetness >= 0.5f ? ChatFormatting.YELLOW : (wetness > 0.0f ? ChatFormatting.AQUA : ChatFormatting.GREEN));

        Component cue = Component.translatable("message.humannature.environment_cue",
                wetComponent.copy().withStyle(wetChatColor),
                tempComponent.copy().withStyle(tempChatColor)
        ).withStyle(ChatFormatting.GRAY);
        player.displayClientMessage(cue, true);
    }

    public static boolean isPlayerFullyHealthy(Player player) {
        if (player == null) return false;

        if (ServerConfig.ENABLE_INJURY.get()) {
            if (InjuryEventHandler.isFractureActive(player)) return false;
            PlayerInjuryData injuryData = InjuryModule.getOrCreateInjuryData(player.getUUID());
            if (injuryData.hasAnyInjury()) return false;
        }

        if (ServerConfig.ENABLE_TEMPERATURE.get()) {
            PlayerTemperatureData tempData = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
            if (tempData.getWetnessBuffer() > 0.0f) return false;
            if (player.getTicksFrozen() > 0) return false;
            if (tempData.hasInternalWarming()) return false;
            if (tempData.isNearHeatSource()) return false;
            if (tempData.isHypothermiaDebuffActive()) return false;
            float coreTemp = tempData.getCoreTemperature();
            if (coreTemp < 36.5f || coreTemp > 37.5f) return false;
        }

        if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
            PlayerPhysiologyData physData = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
            if (physData.getToxinBuildup() > 0.0f) return false;
            if (physData.getExertionScore() > 0.0f) return false;
            if (physData.isMuscleFailure() || physData.isSwimmingCramped()) return false;
            if (physData.isAdrenalineActive() || physData.isPanicking()) return false;
            if (physData.getIndigestionBuildup() > 0.0f) return false;
        }

        if (ServerConfig.ENABLE_WEIGHT.get()) {
            PlayerWeightData weightData = WeightModule.getOrCreateWeightData(player.getUUID());
            float curWeight = weightData.getCachedWeight();
            if (curWeight <= 0.0f) {
                curWeight = WeightCalculator.calculateWeight(player);
                weightData.setCachedWeight(curWeight);
            }
            float maxWeight = WeightCalculator.calculateMaxCapacity(player);
            float ratio = maxWeight > 0 ? curWeight / maxWeight : 0.0f;
            EncumbranceTier tier = EncumbranceTier.fromWeightRatio(ratio);
            if (tier != EncumbranceTier.NORMAL && tier != EncumbranceTier.LIGHT) {
                return false;
            }
        }

        return true;
    }

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || player.isDeadOrDying()) return;
        if (!ClientConfig.ENABLE_SWAPPABLE_HUD.get()) return;
        if (ClientConfig.AUTO_HIDE_HEALTHY_HUD.get() && isPlayerFullyHealthy(player)) return;

        HudMode mode = getCurrentMode();
        if (mode == HudMode.OFF || mode == HudMode.INJURY || mode == HudMode.ENVIRONMENT) return;

        boolean enablePhys = ServerConfig.ENABLE_PHYSIOLOGY.get();
        boolean enableTemp = ServerConfig.ENABLE_TEMPERATURE.get();
        boolean enableInj = ServerConfig.ENABLE_INJURY.get();
        boolean enableWeight = ServerConfig.ENABLE_WEIGHT.get();

        if (mode == HudMode.WEIGHT && !enableWeight) return;
        if (mode == HudMode.PHYSIOLOGY && !enablePhys) return;
        if (mode == HudMode.COMBINED && !enablePhys && !enableTemp && !enableInj && !enableWeight) return;

        List<Component> lines = new ArrayList<>();

        if (mode == HudMode.WEIGHT) {
            PlayerWeightData weightData = WeightModule.getOrCreateWeightData(player.getUUID());
            float curWeight = weightData.getCachedWeight();
            if (curWeight <= 0.0f) {
                curWeight = WeightCalculator.calculateWeight(player);
                weightData.setCachedWeight(curWeight);
            }
            float maxWeight = WeightCalculator.calculateMaxCapacity(player);
            float ratio = maxWeight > 0 ? curWeight / maxWeight : 0.0f;
            float weightPct = ratio * 100.0f;
            EncumbranceTier tier = EncumbranceTier.fromWeightRatio(ratio);

            lines.add(Component.translatable("hud.humannature.weight.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

            int totalSegments = 12;
            int filledSegments = Math.max(0, Math.min(totalSegments, Math.round(ratio * totalSegments)));
            StringBuilder barBuilder = new StringBuilder();
            for (int i = 0; i < totalSegments; i++) {
                barBuilder.append(i < filledSegments ? "█" : "░");
            }
            ChatFormatting tierChatColor = tier.getChatFormatting();
            Component barComp = Component.literal("[" + barBuilder + "]").withStyle(tierChatColor);
            Component curWeightComp = Component.literal(String.format(java.util.Locale.ROOT, "%.1f", curWeight)).withStyle(tierChatColor);
            Component maxWeightComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f", maxWeight)).withStyle(ChatFormatting.GRAY);
            Component weightPctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f", weightPct)).withStyle(tierChatColor);

            lines.add(Component.translatable("hud.humannature.weight.bar_line",
                    barComp,
                    curWeightComp,
                    maxWeightComp,
                    weightPctComp).withStyle(ChatFormatting.GRAY));

            lines.add(Component.translatable("hud.humannature.weight.tier_line",
                    Component.translatable(tier.getTranslationKey()).withStyle(tierChatColor)).withStyle(ChatFormatting.GRAY));

            if (tier == EncumbranceTier.NORMAL) {
                lines.add(Component.translatable("hud.humannature.weight.full_agility").withStyle(ChatFormatting.GREEN));
            } else {
                lines.add(Component.translatable("hud.humannature.weight.speed_penalty",
                        String.format(java.util.Locale.ROOT, "%.0f", tier.getSpeedPenalty() * 100.0f)).withStyle(ChatFormatting.RED));
                if (tier.getSinkPull() > 0.0) {
                    lines.add(Component.translatable("hud.humannature.weight.sink_penalty",
                            String.format(java.util.Locale.ROOT, "%.0f", tier.getSinkPull() * 1000.0f)).withStyle(ChatFormatting.RED));
                }
                if (tier.getFallDamageMultiplier() > 1.0) {
                    lines.add(Component.translatable("hud.humannature.weight.fall_penalty",
                            String.format(java.util.Locale.ROOT, "%.2f", tier.getFallDamageMultiplier())).withStyle(ChatFormatting.RED));
                }
            }

            float armorWeight = 0.0f;
            for (var stack : player.getInventory().armor) {
                armorWeight += WeightCalculator.calculateStackWeight(stack);
            }
            float offhandWeight = 0.0f;
            for (var stack : player.getInventory().offhand) {
                offhandWeight += WeightCalculator.calculateStackWeight(stack);
            }
            float invWeight = Math.max(0.0f, curWeight - armorWeight - offhandWeight);

            lines.add(Component.translatable("hud.humannature.weight.load_breakdown",
                    String.format(java.util.Locale.ROOT, "%.1f", invWeight),
                    String.format(java.util.Locale.ROOT, "%.1f", armorWeight)));

            double baseCap = ServerConfig.WEIGHT_MAX_CAPACITY != null ? ServerConfig.WEIGHT_MAX_CAPACITY.get() : 500.0;
            if (maxWeight > baseCap) {
                lines.add(Component.translatable("hud.humannature.weight.bonus_capacity",
                        String.format(java.util.Locale.ROOT, "%.0f", maxWeight - baseCap)));
            }
        } else {
            PlayerPhysiologyData physData = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());

            if (enablePhys) {
                if (physData.isAdrenalineActive()) {
                    lines.add(Component.translatable("hud.humannature.physiology.adrenaline_surge").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                } else if (physData.isPanicking()) {
                    lines.add(Component.translatable("hud.humannature.physiology.sensory_panic").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                }
            }

            if (mode == HudMode.COMBINED && enableInj) {
                PlayerInjuryData injuryData = InjuryModule.getOrCreateInjuryData(player.getUUID());
                boolean hasTrauma = InjuryEventHandler.isFractureActive(player) || injuryData.hasAnyInjury();
                if (hasTrauma) {
                    MutableComponent injuredLimbs = Component.empty();
                    boolean hasInjured = false;
                    for (LimbType limb : LimbType.values()) {
                        byte sev = injuryData.getSeverity(limb);
                        if (sev > 0) {
                            if (hasInjured) injuredLimbs.append(", ");
                            hasInjured = true;
                            injuredLimbs.append(Component.translatable(limb.getTranslationKey())).append(" (T" + sev + ")");
                        }
                    }
                    Component limbsTitle = !hasInjured ? Component.translatable("hud.humannature.injury.trauma_stabilized").withStyle(ChatFormatting.GOLD)
                            : Component.translatable("hud.humannature.injury.injured_prefix", injuredLimbs.withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.RED);
                    lines.add(limbsTitle);

                    int remainingTicks = InjuryEventHandler.getRemainingFractureTime(player);
                    if (remainingTicks > 0) {
                        int totalSeconds = remainingTicks / 20;
                        int minutes = totalSeconds / 60;
                        int seconds = totalSeconds % 60;
                        boolean isMoving = player.walkDist - player.walkDistO > 0.001F;
                        boolean isResting = player.isSleeping() || (player.isCrouching() && !isMoving);
                        boolean isSplinted = injuryData.hasAnySplint();
                        boolean hasCalcium = physData.hasCalciumBuff();
                        Component healRate;
                        if (isResting) {
                            healRate = (isSplinted && hasCalcium) ? Component.translatable("hud.humannature.injury.heal_rate.resting_osteo") : Component.translatable("hud.humannature.injury.heal_rate.resting");
                        } else if (isMoving) {
                            healRate = isSplinted ? (hasCalcium ? Component.translatable("hud.humannature.injury.heal_rate.moving_osteo") : Component.translatable("hud.humannature.injury.heal_rate.moving_splint")) : Component.translatable("hud.humannature.injury.heal_rate.paused");
                        } else {
                            healRate = (isSplinted && hasCalcium) ? Component.translatable("hud.humannature.injury.heal_rate.standing_osteo") : Component.translatable("hud.humannature.injury.heal_rate.standing");
                        }
                        String timeStr = String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds);
                        lines.add(Component.translatable("hud.humannature.injury.fracture_healing", timeStr, healRate));
                    }
                }
            }

            if (enablePhys) {
                long awakeTicks = physData.getAwakeTicks();
                long awakeHours = awakeTicks / 1000L;
                float fatigueScore = physData.getFatigueScore();
                String fatigueKey;
                ChatFormatting fatigueChatColor;
                if (fatigueScore >= 0.80f) {
                    fatigueChatColor = ChatFormatting.RED;
                    fatigueKey = "drowsy";
                } else if (fatigueScore >= 0.50f) {
                    fatigueChatColor = ChatFormatting.GOLD;
                    fatigueKey = "tired";
                } else if (fatigueScore >= 0.20f) {
                    fatigueChatColor = ChatFormatting.YELLOW;
                    fatigueKey = "mild";
                } else {
                    fatigueChatColor = ChatFormatting.GREEN;
                    fatigueKey = "rested";
                }
                Component awakeText = Component.translatable("hud.humannature.physiology.fatigue_awake", awakeHours).withStyle(fatigueChatColor);
                Component fatigueLabel = Component.translatable("humannature.fatigue." + fatigueKey).withStyle(fatigueChatColor);
                Component fatiguePctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", fatigueScore * 100.0f)).withStyle(fatigueChatColor);
                lines.add(Component.translatable("hud.humannature.physiology.fatigue_line",
                        awakeText, fatigueLabel, fatiguePctComp).withStyle(ChatFormatting.GRAY));

                if (PhysiologyModule.isToxinBuildupEnabled()) {
                    float toxinBuildup = physData.getToxinBuildup();
                    ToxinType toxinType = physData.getLastToxinType();
                    Component stateLabel;
                    if (toxinBuildup <= 0.0f) {
                        stateLabel = Component.translatable("humannature.toxin.none");
                    } else {
                        int delayTicks = ServerConfig.TOXIN_DECAY_DELAY_TICKS.get();
                        if (physData.getTicksSinceLastExposure() >= delayTicks) {
                            boolean isNearHeat = PhysiologyEventHandler.isNearHeatSource(player);
                            boolean isResting = player.isSleeping() || (player.isCrouching() && (player.walkDist - player.walkDistO <= 0.001F));
                            stateLabel = (isNearHeat || isResting) ? Component.translatable("hud.humannature.physiology.toxicity_decaying_3x") : Component.translatable("hud.humannature.physiology.toxicity_decaying");
                        } else {
                            stateLabel = Component.translatable("hud.humannature.physiology.toxicity_active");
                        }
                    }
                    ChatFormatting toxinChatColor = toxinBuildup <= 0.0f ? ChatFormatting.GREEN : (toxinType == ToxinType.WITHER ? ChatFormatting.DARK_PURPLE : ChatFormatting.RED);
                    Component toxinPctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", toxinBuildup)).withStyle(toxinChatColor);
                    Component toxinStateComp = stateLabel.copy().withStyle(toxinChatColor);
                    if (mode == HudMode.COMBINED) {
                        lines.add(Component.translatable("hud.humannature.physiology.toxicity_compact_line",
                                toxinPctComp, toxinStateComp).withStyle(ChatFormatting.GRAY));
                    } else {
                        int filledSegments = Math.max(0, Math.min(10, Math.round(toxinBuildup / 10.0f)));
                        StringBuilder barBuilder = new StringBuilder();
                        for (int i = 0; i < 10; i++) {
                            barBuilder.append(i < filledSegments ? "█" : "░");
                        }
                        Component toxinBarComp = Component.literal("[" + barBuilder + "]").withStyle(toxinChatColor);
                        lines.add(Component.translatable("hud.humannature.physiology.toxicity_line",
                                toxinBarComp, toxinPctComp, toxinStateComp).withStyle(ChatFormatting.GRAY));
                    }
                }

                if (ClientConfig.SHOW_CARDIAC_HUD != null && ClientConfig.SHOW_CARDIAC_HUD.get()) {
                    int bpm = DiagnosticScreen.calculateHeartRateBpm(player, physData);
                    ChatFormatting bpmChatColor = bpm >= 140 ? ChatFormatting.RED : (bpm >= 110 ? ChatFormatting.GOLD : (bpm >= 80 ? ChatFormatting.YELLOW : ChatFormatting.GREEN));
                    String bpmKey = bpm >= 140 ? "tachycardia" : (bpm >= 110 ? "elevated" : (bpm >= 80 ? "moderate" : "normal"));
                    Component bpmComp = Component.literal("❤ " + bpm + " BPM").withStyle(bpmChatColor);
                    Component bpmLabelComp = Component.translatable("humannature.heart." + bpmKey).copy().withStyle(bpmChatColor);
                    lines.add(Component.translatable("hud.humannature.physiology.heart_line", bpmComp, bpmLabelComp).withStyle(ChatFormatting.GRAY));
                }

                float exertion = physData.getExertionScore();
                boolean isFailure = physData.isMuscleFailure();
                boolean isCramped = physData.isSwimmingCramped();
                ChatFormatting staminaChatColor = isFailure ? ChatFormatting.RED : (exertion >= 50.0f ? ChatFormatting.GOLD : ChatFormatting.GREEN);
                Component exertionStr = isFailure ? Component.translatable("humannature.stamina.failure").copy().withStyle(ChatFormatting.RED)
                        : (isCramped ? Component.translatable("humannature.stamina.cramp").copy().withStyle(ChatFormatting.RED)
                        : (exertion >= 50.0f ? Component.translatable("humannature.stamina.strained").copy().withStyle(ChatFormatting.GOLD)
                        : Component.translatable("humannature.stamina.normal").copy().withStyle(ChatFormatting.GREEN)));
                Component staminaPctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", 100.0f - exertion)).withStyle(staminaChatColor);
                lines.add(Component.translatable("hud.humannature.physiology.stamina_line",
                        staminaPctComp,
                        exertionStr).withStyle(ChatFormatting.GRAY));

                boolean showMetabolic = ClientConfig.SHOW_METABOLIC_DETAILS == null || ClientConfig.SHOW_METABOLIC_DETAILS.get();
                if (showMetabolic) {
                    float pendingNutr = physData.getPendingNutrition();
                    float pendingSat = physData.getPendingSaturation();
                    if (pendingNutr > 0.0f || pendingSat > 0.0f) {
                        lines.add(Component.translatable("hud.humannature.physiology.digestion_absorbing",
                                String.format(java.util.Locale.ROOT, "%.0f", pendingNutr),
                                String.format(java.util.Locale.ROOT, "%.1f", pendingSat)));
                    }

                    float indigestion = physData.getIndigestionBuildup();
                    if (indigestion > 0.0f) {
                        Component crampNotice = indigestion >= 50.0f ? Component.translatable("hud.humannature.physiology.cramps_active") : Component.translatable("hud.humannature.physiology.cramps_upset");
                        lines.add(Component.translatable("hud.humannature.physiology.indigestion_line",
                                String.format(java.util.Locale.ROOT, "%.0f", indigestion),
                                crampNotice));
                    }

                    if (physData.hasCalciumBuff()) {
                        long calSec = physData.getCalciumAbsorptionTicks() / 20;
                        lines.add(Component.translatable("hud.humannature.physiology.osteogenesis_line",
                                String.format(java.util.Locale.ROOT, "%02d:%02d", calSec / 60, calSec % 60)));
                    }

                    if (pendingNutr <= 0.0f && indigestion <= 0.0f && !physData.hasCalciumBuff()) {
                        var foodData = player.getFoodData();
                        ChatFormatting digColor = (foodData.getSaturationLevel() > 0.0f || (player.isHurt() && foodData.getFoodLevel() >= 18)) ? ChatFormatting.GREEN :
                                (foodData.getFoodLevel() > 6 ? ChatFormatting.GRAY : ChatFormatting.RED);
                        Component digestionState = ((foodData.getSaturationLevel() > 0.0f || (player.isHurt() && foodData.getFoodLevel() >= 18)) ? Component.translatable("hud.humannature.physiology.digestion_active") :
                                (foodData.getFoodLevel() > 6 ? Component.translatable("hud.humannature.physiology.digestion_resting") : Component.translatable("hud.humannature.physiology.digestion_hungry")))
                                .copy().withStyle(digColor);
                        lines.add(Component.translatable("hud.humannature.physiology.digestion_line", digestionState).withStyle(ChatFormatting.GRAY));
                    }
                }

                if (physData.isAdrenalineActive()) {
                    float remRush = physData.getAdrenalineTicks() / 20.0f;
                    lines.add(Component.translatable("hud.humannature.physiology.rush_duration", String.format(java.util.Locale.ROOT, "%.1f", remRush)));
                    lines.add(Component.translatable("hud.humannature.physiology.rush_collapse"));
                } else if (physData.getAdrenalineCooldownTicks() > 0) {
                    int remCooldown = physData.getAdrenalineCooldownTicks() / 20;
                    lines.add(Component.translatable("hud.humannature.physiology.adrenaline_cooldown", remCooldown));
                }

                if (physData.getClaustrophobiaTicks() > 0 || physData.isPanicking()) {
                    int panicThreshold = ServerConfig.PANIC_THRESHOLD_TICKS.get();
                    int panicPct = Math.min(100, Math.round((physData.getClaustrophobiaTicks() / (float) panicThreshold) * 100.0f));
                    Component panicDesc = physData.isPanicking() ? Component.translatable("hud.humannature.physiology.claustrophobia_tremors") : Component.translatable("hud.humannature.physiology.claustrophobia_seeking", panicPct);
                    lines.add(Component.translatable("hud.humannature.physiology.claustrophobia_line", panicDesc));
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
                    Component statusLabel;
                    if (tempData.isNearHeatSource()) {
                        statusLabel = Component.translatable("hud.humannature.temperature.status_drying_heat");
                    } else if (player.isInWater() || player.isUnderWater()) {
                        statusLabel = Component.translatable("hud.humannature.temperature.status_submerged");
                    } else if (player.level().isRainingAt(player.blockPosition())) {
                        statusLabel = Component.translatable("hud.humannature.temperature.status_rain");
                    } else if (ticksFrozen > 0) {
                        statusLabel = Component.translatable("hud.humannature.temperature.status_freezing");
                    } else {
                        statusLabel = Component.translatable("hud.humannature.temperature.status_drying");
                    }
                    ChatFormatting wetChatColor = wetness >= 1.0f ? ChatFormatting.RED : (wetness >= 0.5f ? ChatFormatting.YELLOW : ChatFormatting.AQUA);
                    Component wetBarComp = Component.literal("[" + barBuilder + "]").withStyle(wetChatColor);
                    Component wetPctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", wetness * 100.0f)).withStyle(wetChatColor);
                    lines.add(Component.translatable("hud.humannature.temperature.wetness_line", wetBarComp, wetPctComp, statusLabel.copy().withStyle(wetChatColor)).withStyle(ChatFormatting.GRAY));
                } else {
                    float coreTemp = tempData.getCoreTemperature();
                    String coreTempStr = String.format(java.util.Locale.ROOT, "%.1f°C", coreTemp);
                    if (coreTemp > 42.0f) {
                        Component tempVal = Component.literal(coreTempStr).withStyle(ChatFormatting.RED);
                        Component tempStatus = Component.translatable("hud.humannature.temperature.status.hyperthermia").withStyle(ChatFormatting.RED);
                        lines.add(Component.translatable("hud.humannature.temperature.line", tempVal, tempStatus).withStyle(ChatFormatting.GRAY));
                    } else if (coreTemp > 39.5f) {
                        Component tempVal = Component.literal(coreTempStr).withStyle(ChatFormatting.RED);
                        Component tempStatus = Component.translatable("hud.humannature.temperature.status.heat_exhaustion").withStyle(ChatFormatting.RED);
                        lines.add(Component.translatable("hud.humannature.temperature.line", tempVal, tempStatus).withStyle(ChatFormatting.GRAY));
                    } else if (tempData.hasInternalWarming()) {
                        int remSec = tempData.getInternalWarmingTicks() / 20;
                        Component tempVal = Component.literal(coreTempStr).withStyle(ChatFormatting.GOLD);
                        Component tempStatus = Component.translatable("hud.humannature.temperature.status.warming", remSec).withStyle(ChatFormatting.GOLD);
                        lines.add(Component.translatable("hud.humannature.temperature.line", tempVal, tempStatus).withStyle(ChatFormatting.GRAY));
                    } else if (ticksFrozen > 0) {
                        Component tempVal = Component.literal(coreTempStr).withStyle(ChatFormatting.AQUA);
                        Component tempStatus = Component.translatable("hud.humannature.temperature.status.freezing").withStyle(ChatFormatting.AQUA);
                        lines.add(Component.translatable("hud.humannature.temperature.line", tempVal, tempStatus).withStyle(ChatFormatting.GRAY));
                    } else if (coreTemp < 35.0f) {
                        Component tempVal = Component.literal(coreTempStr).withStyle(ChatFormatting.AQUA);
                        Component tempStatus = Component.translatable("hud.humannature.temperature.status.hypothermia").withStyle(ChatFormatting.AQUA);
                        lines.add(Component.translatable("hud.humannature.temperature.line", tempVal, tempStatus).withStyle(ChatFormatting.GRAY));
                    } else {
                        Component tempVal = Component.literal(coreTempStr).withStyle(ChatFormatting.GREEN);
                        Component tempStatus = Component.translatable("hud.humannature.temperature.status.normal").withStyle(ChatFormatting.GREEN);
                        lines.add(Component.translatable("hud.humannature.temperature.line", tempVal, tempStatus).withStyle(ChatFormatting.GRAY));
                    }
                }
            }

            if (mode == HudMode.COMBINED && enableWeight) {
                PlayerWeightData weightData = WeightModule.getOrCreateWeightData(player.getUUID());
                float curWeight = weightData.getCachedWeight();
                if (curWeight <= 0.0f) {
                    curWeight = WeightCalculator.calculateWeight(player);
                    weightData.setCachedWeight(curWeight);
                }
                float maxWeight = WeightCalculator.calculateMaxCapacity(player);
                float ratio = maxWeight > 0 ? curWeight / maxWeight : 0.0f;
                float weightPct = ratio * 100.0f;
                EncumbranceTier tier = EncumbranceTier.fromWeightRatio(ratio);

                ChatFormatting tierChatColor = tier.getChatFormatting();
                Component curWeightComp = Component.literal(String.format(java.util.Locale.ROOT, "%.1f", curWeight)).withStyle(tierChatColor);
                Component maxWeightComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f", maxWeight)).withStyle(ChatFormatting.GRAY);
                Component tierComp = Component.translatable(tier.getTranslationKey()).withStyle(tierChatColor);
                Component pctComp = Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", weightPct)).withStyle(tierChatColor);

                lines.add(Component.translatable("hud.humannature.weight.compact_line",
                        curWeightComp,
                        maxWeightComp,
                        tierComp,
                        pctComp).withStyle(ChatFormatting.GRAY));
                if (tier != EncumbranceTier.NORMAL) {
                    lines.add(Component.translatable("hud.humannature.weight.compact_penalties",
                            String.format(java.util.Locale.ROOT, "%.0f", tier.getSpeedPenalty() * 100.0f),
                            String.format(java.util.Locale.ROOT, "%.0f", tier.getSinkPull() * 1000.0f),
                            String.format(java.util.Locale.ROOT, "%.2f", tier.getFallDamageMultiplier())).withStyle(ChatFormatting.RED));
                }
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
        for (Component line : lines) {
            wrappedLines.addAll(mc.font.split(line, maxWrapWidth));
        }

        int maxLineWidth = 0;
        for (FormattedCharSequence fcs : wrappedLines) {
            int w = mc.font.width(fcs);
            if (w > maxLineWidth) maxLineWidth = w;
        }

        int boxWidth = Math.max(120, maxLineWidth + 8);
        int boxHeight = wrappedLines.size() * 11 + 3 + 42 + 6;

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

        drawAtlasStrip(guiGraphics, player, x, curLineY + 3);
    }

    private static void drawAtlasStrip(GuiGraphics gui, Player player, int x, int y) {
        DiagnosticAssets.Uv tempIcon = DiagnosticAssets.THERMO_NORMAL;
        float freezeFraction = 0.0f;
        int tempColor = DiagnosticAssets.COLOR_OK;
        if (ServerConfig.ENABLE_TEMPERATURE.get()) {
            PlayerTemperatureData tempData = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
            int frozen = player.getTicksFrozen();
            int threshold = Math.max(1, (int) Math.round(player.getTicksRequiredToFreeze()
                    * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get()));
            freezeFraction = Math.min(1.0f, frozen / (float) threshold);
            if (frozen > 0 || tempData.getWetnessBuffer() >= 0.5f) {
                tempIcon = DiagnosticAssets.THERMO_COLD;
                tempColor = freezeFraction >= 0.6f ? DiagnosticAssets.COLOR_CRITICAL : DiagnosticAssets.COLOR_WARN;
            } else if (tempData.isNearHeatSource()) {
                tempIcon = DiagnosticAssets.THERMO_HOT;
                tempColor = DiagnosticAssets.COLOR_WARN;
            }
        }
        HudAtlasRenderer.icon(gui, tempIcon, x, y);
        HudAtlasRenderer.bar(gui, x + 12, y + 2, 72, freezeFraction, tempColor);

        DiagnosticAssets.Uv loadIcon = DiagnosticAssets.LOAD_LIGHT;
        float loadFraction = 0.0f;
        int loadColor = DiagnosticAssets.COLOR_OK;
        if (ServerConfig.ENABLE_WEIGHT.get()) {
            PlayerWeightData data = WeightModule.getOrCreateWeightData(player.getUUID());
            float current = data.getCachedWeight();
            if (current <= 0.0f) {
                current = WeightCalculator.calculateWeight(player);
                data.setCachedWeight(current);
            }
            float max = WeightCalculator.calculateMaxCapacity(player);
            loadFraction = max > 0 ? current / max : 0;
            EncumbranceTier tier = EncumbranceTier.fromWeightRatio(loadFraction);
            loadColor = switch (tier) {
                case OVERBURDENED -> DiagnosticAssets.COLOR_CRITICAL;
                case MEDIUM -> DiagnosticAssets.COLOR_WARN;
                default -> DiagnosticAssets.COLOR_OK;
            };
            loadIcon = loadFraction >= 0.85f ? DiagnosticAssets.LOAD_HEAVY
                    : (loadFraction >= 0.70f ? DiagnosticAssets.LOAD_MEDIUM : DiagnosticAssets.LOAD_LIGHT);
        }
        HudAtlasRenderer.icon(gui, loadIcon, x, y + 14);
        HudAtlasRenderer.bar(gui, x + 12, y + 16, 72, loadFraction, loadColor);

        if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
            PlayerPhysiologyData phys = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
            float stamina = Math.max(0.0f, Math.min(100.0f, 100.0f - phys.getExertionScore())) / 100.0f;
            HudAtlasRenderer.bar(gui, x + 12, y + 30, 72, stamina,
                    phys.isMuscleFailure() ? DiagnosticAssets.COLOR_CRITICAL : DiagnosticAssets.COLOR_OK);
        }
    }
}
