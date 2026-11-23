package com.alikuxac.humannature.client.gui;

import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.client.ModKeyMappings;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.item.ClothBandageItem;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
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
import com.alikuxac.humannature.network.TreatLimbPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticScreen extends Screen {
    public static final int GUI_WIDTH = DiagnosticAssets.GUI_WIDTH;
    public static final int GUI_HEIGHT = DiagnosticAssets.GUI_HEIGHT;

    private enum Tab { SUM, INJ, MET, BLD }

    private static final int[] TAB_X = { 84, 105, 126, 147 };
    private static final String[] TAB_TEXT = { "SUM", "INJ", "MET", "BLD" };

    private record Metric(String label, String value, float fill, int color,
                          DiagnosticAssets.Uv icon, List<Component> tooltip) {}

    private int leftPos;
    private int topPos;
    private Tab activeTab = Tab.SUM;
    private LimbType hoveredLimb;
    private LimbType selectedLimb = LimbType.HEAD;
    private Metric hoveredMetric;

    public DiagnosticScreen() {
        super(Component.literal("Body Diagnostics"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        Minecraft mc = this.minecraft;
        if (mc == null || mc.player == null) return;
        Player player = mc.player;

        hoveredLimb = null;
        hoveredMetric = null;

        RenderSystem.disableDepthTest();
        gui.blit(DiagnosticAssets.BACKGROUND, leftPos, topPos, GUI_WIDTH, GUI_HEIGHT,
                0.0f, 0.0f, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        gui.drawString(this.font, "DIAGNOSTICS", leftPos + 8, topPos + 8, 0xFFD7DEE8, false);

        drawTabs(gui, mouseX, mouseY);
        drawSilhouettes(gui, player, mouseX, mouseY);

        List<Metric> metrics = buildMetrics(player);
        drawMetrics(gui, metrics, mouseX, mouseY);
        drawFooter(gui, player);

        if (hoveredLimb != null) {
            gui.renderComponentTooltip(this.font, limbTooltip(player, hoveredLimb), mouseX, mouseY);
        } else if (hoveredMetric != null && !hoveredMetric.tooltip().isEmpty()) {
            gui.renderComponentTooltip(this.font, hoveredMetric.tooltip(), mouseX, mouseY);
        }
        RenderSystem.enableDepthTest();
    }

    private void drawTabs(GuiGraphics gui, int mouseX, int mouseY) {
        for (int i = 0; i < Tab.values().length; i++) {
            int x = leftPos + TAB_X[i];
            boolean selected = Tab.values()[i] == activeTab;
            gui.fill(x, topPos + 7, x + 20, topPos + 19,
                    selected ? DiagnosticAssets.COLOR_PANEL : DiagnosticAssets.COLOR_SHADOW);
            if (selected) {
                gui.fill(x, topPos + 18, x + 20, topPos + 19, DiagnosticAssets.COLOR_COPPER);
            }
            gui.drawString(this.font, tabLabel(Tab.values()[i]), x + 1, topPos + 9,
                    selected ? 0xFFFFFFFF : 0xFF94A3B8, false);
        }
    }

    private String tabLabel(Tab tab) {
        if (tab == Tab.BLD && !PhysiologyModule.isBloodSystemEnabled()) return "SYS";
        return TAB_TEXT[tab.ordinal()];
    }

    private void drawSilhouettes(GuiGraphics gui, Player player, int mouseX, int mouseY) {
        PlayerInjuryData injury = InjuryModule.getOrCreateInjuryData(player.getUUID());
        int originX = leftPos + DiagnosticAssets.SPRITE_X;
        int originY = topPos + DiagnosticAssets.SPRITE_Y;
        byte bleedTier = PhysiologyModule.isBloodSystemEnabled()
                ? PhysiologyModule.getOrCreatePhysiologyData(player.getUUID()).getBleedingTier() : 0;
        boolean dropVisible = (net.minecraft.Util.getMillis() / 400) % 2 == 0;

        for (LimbType limb : LimbType.values()) {
            byte severity = injury.getSeverity(limb);
            float[] tint = severityTint(severity);
            HudAtlasRenderer.tintedBlit(gui, DiagnosticAssets.silhouette(limb),
                    originX, originY, 64, 128, 64, 128, tint[0], tint[1], tint[2]);

            DiagnosticAssets.Rect rect = DiagnosticAssets.limbRect(limb);
            boolean hovered = mouseX >= originX + rect.x() && mouseX < originX + rect.x() + rect.width()
                    && mouseY >= originY + rect.y() && mouseY < originY + rect.y() + rect.height();
            if (hovered) {
                hoveredLimb = limb;
                gui.renderOutline(originX + rect.x(), originY + rect.y(), rect.width(), rect.height(),
                        DiagnosticAssets.COLOR_HIGHLIGHT);
            }
            if (bleedTier > 0 && severity > 0 && dropVisible) {
                gui.fill(originX + rect.x() + rect.width() - 3, originY + rect.y(),
                        originX + rect.x() + rect.width() - 1, originY + rect.y() + 2, 0xFFF87171);
            }
            if (severity > 0) {
                gui.enableScissor(originX + rect.x(), originY + rect.y(),
                        originX + rect.x() + rect.width(), originY + rect.y() + rect.height());
                gui.blit(DiagnosticAssets.SPLINT_OVERLAY, originX, originY, 64, 128,
                        0.0f, 0.0f, 64, 128, 64, 128);
                gui.disableScissor();
            }
        }
    }

    private static float[] severityTint(byte severity) {
        int argb = TraumaSeverity.fromLevel(severity).getColorRgb();
        return new float[] {
                ((argb >> 16) & 0xFF) / 255.0f,
                ((argb >> 8) & 0xFF) / 255.0f,
                (argb & 0xFF) / 255.0f
        };
    }

    private void drawMetrics(GuiGraphics gui, List<Metric> metrics, int mouseX, int mouseY) {
        for (int i = 0; i < metrics.size(); i++) {
            int slotY = topPos + DiagnosticAssets.SLOT_Y + i * DiagnosticAssets.SLOT_PITCH;
            int slotX = leftPos + DiagnosticAssets.SLOT_X;
            Metric metric = metrics.get(i);

            HudAtlasRenderer.icon(gui, metric.icon(), slotX, slotY);

            String label = metric.label();
            String value = metric.value();
            int labelWidth = this.font.width(label);
            int maxValueWidth = DiagnosticAssets.SLOT_BAR_W - labelWidth - 4;
            if (maxValueWidth < 12) {
                label = this.font.plainSubstrByWidth(label, DiagnosticAssets.SLOT_BAR_W - 24);
                labelWidth = this.font.width(label);
                maxValueWidth = DiagnosticAssets.SLOT_BAR_W - labelWidth - 4;
            }
            if (this.font.width(value) > maxValueWidth) {
                value = this.font.plainSubstrByWidth(value, Math.max(6, maxValueWidth));
            }

            gui.drawString(this.font, label, leftPos + DiagnosticAssets.SLOT_X + 11,
                    slotY, 0xFF94A3B8, false);
            gui.drawString(this.font, value,
                    leftPos + DiagnosticAssets.SLOT_X + DiagnosticAssets.SLOT_BAR_W - this.font.width(value),
                    slotY, metric.color(), false);
            HudAtlasRenderer.bar(gui, slotX, slotY + 9, DiagnosticAssets.SLOT_BAR_W, metric.fill(), metric.color());

            if (mouseX >= slotX && mouseX < slotX + DiagnosticAssets.SLOT_BAR_W
                    && mouseY >= slotY && mouseY < slotY + 14) {
                hoveredMetric = metric;
            }
        }
    }

    private void drawFooter(GuiGraphics gui, Player player) {
        int x = leftPos + DiagnosticAssets.SLOT_X;
        int y = topPos + DiagnosticAssets.DIVIDER_Y;
        gui.fill(x, y, leftPos + DiagnosticAssets.COLUMN_X + DiagnosticAssets.COLUMN_W - 2, y + 1,
                DiagnosticAssets.COLOR_HIGHLIGHT);
        ItemStack splint = InjuryEventHandler.findFirstSplint(player);
        boolean hasSplint = !splint.isEmpty();
        boolean onCooldown = hasSplint && player.getCooldowns().isOnCooldown(splint.getItem());
        if (hasSplint && !onCooldown) {
            gui.drawString(this.font, "Click limb", x, y + 5, 0xFFFFD166, false);
            gui.drawString(this.font, "E Inventory", x, y + 15, 0xFF94A3B8, false);
        } else if (onCooldown) {
            gui.drawString(this.font, "Treating...", x, y + 5, 0xFFF87171, false);
        } else {
            gui.drawString(this.font, "E Inventory", x, y + 5, 0xFF64748B, false);
        }
    }

    private List<Metric> buildMetrics(Player player) {
        PlayerInjuryData injury = InjuryModule.getOrCreateInjuryData(player.getUUID());
        PlayerPhysiologyData phys = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        boolean blood = PhysiologyModule.isBloodSystemEnabled();

        return switch (activeTab) {
            case SUM -> buildSummary(player, injury, phys, blood);
            case INJ -> buildInjury(injury);
            case MET -> buildMetabolic(player, phys);
            case BLD -> blood ? buildBlood(phys) : buildSystem(player, phys);
        };
    }

    private List<Metric> buildSummary(Player player, PlayerInjuryData injury,
                                      PlayerPhysiologyData phys, boolean blood) {
        LimbType focus = hoveredLimb != null ? hoveredLimb : selectedLimb;
        byte sev = injury.getSeverity(focus);
        String sevText = sev == 0 ? "OK" : (sev == 1 ? "SPRAIN" : "FRACT");
        int sevColor = severityColor(sev);

        List<Metric> list = new ArrayList<>();
        list.add(new Metric(abbrev(focus), sevText, sev / 2.0f, sevColor, null,
                List.of(Component.literal("§7" + focus.getDisplayName() + " — "
                        + (sev == 0 ? "works normally." : sev == 1 ? "sprained, reduced strength." : "fractured, needs a splint.")))));

        DiagnosticAssets.Uv tempIcon = DiagnosticAssets.THERMO_NORMAL;
        String tempText = "OK";
        int tempColor = DiagnosticAssets.COLOR_OK;
        if (ServerConfig.ENABLE_TEMPERATURE.get()) {
            PlayerTemperatureData tempData = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
            int frozen = player.getTicksFrozen();
            int threshold = (int) Math.round(player.getTicksRequiredToFreeze()
                    * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get());
            if (frozen >= threshold) {
                tempText = "LOW"; tempColor = DiagnosticAssets.COLOR_CRITICAL;
                tempIcon = DiagnosticAssets.THERMO_COLD;
            } else if (frozen > 0) {
                tempText = "COOL"; tempColor = DiagnosticAssets.COLOR_WARN;
                tempIcon = DiagnosticAssets.THERMO_COLD;
            } else if (tempData.isNearHeatSource()) {
                tempText = "HIGH"; tempColor = DiagnosticAssets.COLOR_WARN;
                tempIcon = DiagnosticAssets.THERMO_HOT;
            }
        }
        list.add(new Metric("TEMP", tempText, 0.5f, tempColor, tempIcon,
                List.of(Component.literal("Low temperature slows mining and drains warmth."))));

        String loadText = "OK";
        int loadColor = DiagnosticAssets.COLOR_OK;
        DiagnosticAssets.Uv loadIcon = DiagnosticAssets.LOAD_LIGHT;
        float loadFill = 0.0f;
        if (ServerConfig.ENABLE_WEIGHT.get()) {
            PlayerWeightData data = WeightModule.getOrCreateWeightData(player.getUUID());
            float current = data.getCachedWeight();
            if (current <= 0.0f) {
                current = WeightCalculator.calculateWeight(player);
                data.setCachedWeight(current);
            }
            float max = WeightCalculator.calculateMaxCapacity(player);
            loadFill = max > 0 ? current / max : 0;
            EncumbranceTier tier = EncumbranceTier.fromWeightRatio(loadFill);
            loadText = String.format("%.0f%%", loadFill * 100.0f);
            loadColor = switch (tier) {
                case OVERBURDENED -> DiagnosticAssets.COLOR_CRITICAL;
                case MEDIUM -> DiagnosticAssets.COLOR_WARN;
                default -> DiagnosticAssets.COLOR_OK;
            };
            loadIcon = loadFill >= 0.85f ? DiagnosticAssets.LOAD_HEAVY
                    : (loadFill >= 0.70f ? DiagnosticAssets.LOAD_MEDIUM : DiagnosticAssets.LOAD_LIGHT);
        }
        list.add(new Metric("LOAD", loadText, loadFill, loadColor, loadIcon,
                List.of(Component.literal("Heavier load lowers speed and increases fall damage."))));

        if (blood) {
            int volume = phys.getBloodVolume();
            BloodType type = phys.getBloodType();
            float fraction = Mth.clamp(volume / 5000.0f, 0.0f, 1.0f);
            int color = volume >= 4000 ? 0xFFFF8A65 : (volume >= 2500 ? DiagnosticAssets.COLOR_WARN : DiagnosticAssets.COLOR_CRITICAL);
            list.add(new Metric("BLOOD", type != null ? type.getDisplayName() : "?", fraction, color, null,
                    List.of(Component.literal(volume + " / 5,000 ml. Below 40% causes blackout."))));
            int bpm = calculateHeartRateBpm(player, phys);
            list.add(new Metric("HR", bpm + " BPM", (bpm - 55) / 140.0f,
                    bpm >= 140 ? DiagnosticAssets.COLOR_CRITICAL : DiagnosticAssets.COLOR_OK, null,
                    List.of(Component.literal("High heart rate drains stamina faster."))));
        } else {
            int hp = Math.round(player.getHealth());
            list.add(new Metric("HP", String.valueOf(hp), player.getHealth() / Math.max(1.0f, player.getMaxHealth()),
                    DiagnosticAssets.COLOR_OK, null, List.of(Component.literal("Health points remaining."))));
        }
        return list;
    }

    private List<Metric> buildInjury(PlayerInjuryData injury) {
        List<Metric> list = new ArrayList<>();
        for (LimbType limb : LimbType.values()) {
            byte sev = injury.getSeverity(limb);
            String text = sev == 0 ? "OK" : (sev == 1 ? "SPRAIN" : "FRACT");
            String effect = sev == 0 ? "Works normally."
                    : (sev == 1 ? "Reduced strength and stamina in this limb."
                    : "Broken: major penalty until splinted.");
            list.add(new Metric(abbrev(limb), text, sev / 2.0f, severityColor(sev), null,
                    List.of(Component.literal("§7" + limb.getDisplayName() + ": " + effect))));
        }
        return list;
    }

    private List<Metric> buildMetabolic(Player player, PlayerPhysiologyData phys) {
        List<Metric> list = new ArrayList<>();
        if (ServerConfig.ENABLE_WEIGHT.get()) {
            float fat = Mth.clamp(player.getFoodData().getSaturationLevel() * 5.0f, 0.0f, 100.0f);
            list.add(new Metric("FAT", pct(fat), fat / 100.0f, fat >= 50 ? DiagnosticAssets.COLOR_WARN : DiagnosticAssets.COLOR_CRITICAL, null,
                    List.of(Component.literal("Energy reserve; low food slows stamina recovery."))));
        } else {
            list.add(placeholder("FAT"));
        }
        float stamina = Mth.clamp(100.0f - phys.getExertionScore(), 0.0f, 100.0f);
        list.add(new Metric("STA", pct(stamina), stamina / 100.0f,
                phys.isMuscleFailure() ? DiagnosticAssets.COLOR_CRITICAL : DiagnosticAssets.COLOR_OK, null,
                List.of(Component.literal("Empty stamina blocks sprinting and weakens hits."))));

        float toxin = Mth.clamp(phys.getToxinBuildup(), 0.0f, 100.0f);
        ToxinType type = phys.getLastToxinType();
        list.add(new Metric("TXN", pct(toxin), toxin / 100.0f,
                toxin > 0 ? (type == ToxinType.WITHER ? 0xFFBA68C8 : 0xFFA3E4D7) : DiagnosticAssets.COLOR_OK, null,
                List.of(Component.literal("Toxin triggers its effect once it reaches 100%."))));

        float calcium = Mth.clamp(phys.getCalcium(), 0.0f, 100.0f);
        list.add(new Metric("CAL", pct(calcium), calcium / 100.0f, 0xFF56C4E8, null,
                List.of(Component.literal("Calcium speeds up fracture healing."))));

        float fatigue = Mth.clamp(phys.getFatigueScore() * 100.0f, 0.0f, 100.0f);
        list.add(new Metric("FTG", pct(fatigue), fatigue / 100.0f,
                fatigue >= 80 ? DiagnosticAssets.COLOR_CRITICAL : (fatigue >= 50 ? DiagnosticAssets.COLOR_WARN : DiagnosticAssets.COLOR_OK), null,
                List.of(Component.literal("Above 80% your vision blurs while moving."))));

        float nut = (Mth.clamp(phys.getCalcium(), 0, 100) + Mth.clamp(phys.getCarbohydrates(), 0, 100)
                + Mth.clamp(phys.getVitamins(), 0, 100)) / 3.0f;
        list.add(new Metric("NUT", pct(nut), nut / 100.0f, 0xFF64D966, null, List.of(
                Component.literal(String.format("Ca %d / Carbs %d / Vit %d",
                        phys.getCalcium(), phys.getCarbohydrates(), phys.getVitamins())))));
        return list;
    }

    private List<Metric> buildBlood(PlayerPhysiologyData phys) {
        BloodType type = phys.getBloodType();
        int volume = phys.getBloodVolume();
        float fraction = Mth.clamp(volume / 5000.0f, 0.0f, 1.0f);
        int color = volume >= 4000 ? 0xFFFF8A65 : (volume >= 2500 ? DiagnosticAssets.COLOR_WARN : DiagnosticAssets.COLOR_CRITICAL);

        List<Metric> list = new ArrayList<>();
        list.add(new Metric("TYPE", type != null ? type.getDisplayName() : "?", 1.0f,
                DiagnosticAssets.COLOR_CRITICAL, null,
                List.of(Component.literal("Blood group; mismatched transfusions cause shock."))));
        list.add(new Metric("VOL", pct(fraction * 100.0f), fraction, color, null, List.of(
                Component.literal(volume + " / 5,000 ml. Low volume raises heart rate."))));
        byte bleed = phys.getBleedingTier();
        list.add(new Metric("BLEED", bleed > 0 ? "T" + bleed : "NONE", bleed / 3.0f,
                bleed > 0 ? DiagnosticAssets.COLOR_CRITICAL : DiagnosticAssets.COLOR_OK, null,
                List.of(Component.literal("Active bleeding drains blood; use a bandage."))));
        String shock = volume < 2000 ? "CRIT" : (volume < 3000 ? "MOD" : (volume < 4000 ? "MILD" : "NONE"));
        list.add(new Metric("SHOCK", shock, 1.0f - fraction, color, null,
                List.of(Component.literal("Shock narrows vision and weakens your strikes."))));
        return list;
    }

    private List<Metric> buildSystem(Player player, PlayerPhysiologyData phys) {
        int bpm = calculateHeartRateBpm(player, phys);
        String label = bpm >= 140 ? "TACHY" : (bpm >= 110 ? "ELEV" : (bpm >= 80 ? "MOD" : "NORM"));
        return List.of(
                new Metric("HP", String.valueOf(Math.round(player.getHealth())),
                        player.getHealth() / Math.max(1.0f, player.getMaxHealth()),
                        DiagnosticAssets.COLOR_OK, null, List.of(Component.literal("Health points remaining."))),
                new Metric("HR", bpm + " BPM", (bpm - 55) / 140.0f,
                        bpm >= 140 ? DiagnosticAssets.COLOR_CRITICAL : DiagnosticAssets.COLOR_OK, null,
                        List.of(Component.literal("High heart rate drains stamina faster."))),
                new Metric("STATE", label, 0.5f, DiagnosticAssets.COLOR_OK, null,
                        List.of(Component.literal("Overall cardiovascular strain level."))),
                placeholder("RES")
        );
    }

    private static Metric placeholder(String label) {
        return new Metric(label, "—", 0.0f, 0xFF475569, null, List.of());
    }

    private static String pct(float value) {
        return String.format("%.0f%%", value);
    }

    private static String abbrev(LimbType limb) {
        return switch (limb) {
            case HEAD -> "HEAD";
            case TORSO -> "TORSO";
            case MAIN_ARM -> "MARM";
            case OFF_ARM -> "OARM";
            case LEFT_LEG -> "LLEG";
            case RIGHT_LEG -> "RLEG";
        };
    }

    private static int severityColor(byte severity) {
        return switch (severity) {
            case 2 -> DiagnosticAssets.COLOR_CRITICAL;
            case 1 -> DiagnosticAssets.COLOR_WARN;
            default -> DiagnosticAssets.COLOR_OK;
        };
    }

    private List<Component> limbTooltip(Player player, LimbType limb) {
        PlayerInjuryData injury = InjuryModule.getOrCreateInjuryData(player.getUUID());
        byte sev = injury.getSeverity(limb);
        TraumaSeverity severity = TraumaSeverity.fromLevel(sev);
        List<Component> tip = new ArrayList<>();
        tip.add(Component.literal("§6" + limb.getDisplayName()));
        tip.add(Component.literal("§7Status: " + severity.getDisplayName() + " (T" + sev + ")"));
        if (sev > 0) {
            ItemStack splint = InjuryEventHandler.findFirstSplint(player);
            tip.add(Component.literal(splint.isEmpty() ? "§8Requires a splint" : "§eClick to treat"));
        } else {
            tip.add(Component.literal("§aNo active fracture"));
        }
        return tip;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < Tab.values().length; i++) {
                int x = leftPos + TAB_X[i];
                if (mouseX >= x && mouseX < x + 20 && mouseY >= topPos + 7 && mouseY < topPos + 19) {
                    activeTab = Tab.values()[i];
                    return true;
                }
            }
            if (this.minecraft != null && this.minecraft.player != null) {
                Player player = this.minecraft.player;
                int originX = leftPos + DiagnosticAssets.SPRITE_X;
                int originY = topPos + DiagnosticAssets.SPRITE_Y;
                for (LimbType limb : LimbType.values()) {
                    DiagnosticAssets.Rect rect = DiagnosticAssets.limbRect(limb);
                    if (mouseX >= originX + rect.x() && mouseX < originX + rect.x() + rect.width()
                            && mouseY >= originY + rect.y() && mouseY < originY + rect.y() + rect.height()) {
                        selectedLimb = limb;
                        byte treatment = findTreatmentType(player);
                        if (treatment >= 0 && this.minecraft.getConnection() != null) {
                            this.minecraft.getConnection().send(new ServerboundCustomPayloadPacket(
                                    new TreatLimbPayload((byte) limb.getIndex(), treatment)));
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static byte findTreatmentType(Player player) {
        ItemStack[] candidates = {
                player.containerMenu.getCarried(), player.getMainHandItem(), player.getOffhandItem()
        };
        for (ItemStack stack : candidates) {
            if (SplintItem.isValidSplint(stack)) return TreatLimbPayload.TYPE_SPLINT;
            if (stack.getItem() instanceof ClothBandageItem) return TreatLimbPayload.TYPE_BANDAGE;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (SplintItem.isValidSplint(stack)) return TreatLimbPayload.TYPE_SPLINT;
            if (stack.getItem() instanceof ClothBandageItem) return TreatLimbPayload.TYPE_BANDAGE;
        }
        return -1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeyMappings.OPEN_DIAGNOSTICS.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        Tab[] values = Tab.values();
        if (keyCode == 258 || keyCode == 262) {
            activeTab = values[(activeTab.ordinal() + 1) % values.length];
            return true;
        }
        if (keyCode == 263) {
            activeTab = values[(activeTab.ordinal() - 1 + values.length) % values.length];
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

    public static int calculateHeartRateBpm(Player player, PlayerPhysiologyData physData) {
        if (player == null || physData == null) return 68;
        double bpm = 68.0;
        bpm += (physData.getExertionScore() / 100.0) * 45.0;
        float maxHp = player.getMaxHealth();
        float curHp = player.getHealth();
        if (maxHp > 0.0f) {
            bpm += (1.0f - Math.max(0.0f, Math.min(1.0f, curHp / maxHp))) * 40.0;
        }
        bpm += (Math.min(100.0f, physData.getToxinBuildup()) / 100.0) * 25.0;
        bpm += physData.getFatigueScore() * 15.0;
        if (physData.isPanicking()) bpm += 45.0;
        if (physData.isAdrenalineActive()) bpm += 60.0;
        if (physData.getIndigestionBuildup() >= 50.0f) bpm += 10.0;
        if (PhysiologyModule.isBloodSystemEnabled() && physData.getBloodVolume() < 4500) {
            bpm += ((4500.0 - physData.getBloodVolume()) / 4500.0) * 50.0;
        }
        return (int) Math.round(Math.max(55.0, Math.min(205.0, bpm)));
    }
}
