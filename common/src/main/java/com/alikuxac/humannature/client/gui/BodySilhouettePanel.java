package com.alikuxac.humannature.client.gui;

import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.item.SplintTier;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.network.NetworkHandler;
import com.alikuxac.humannature.network.TreatLimbPayload;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import com.alikuxac.humannature.modules.injury.TraumaSeverity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
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

    private static final List<LimbHitbox> HITBOXES = List.of(
            new LimbHitbox(LimbType.HEAD, 18, 5, 15, 14),
            new LimbHitbox(LimbType.TORSO, 17, 20, 17, 22),
            new LimbHitbox(LimbType.OFF_ARM, 7, 20, 9, 22),
            new LimbHitbox(LimbType.MAIN_ARM, 35, 20, 9, 22),
            new LimbHitbox(LimbType.LEFT_LEG, 17, 43, 8, 25),
            new LimbHitbox(LimbType.RIGHT_LEG, 26, 43, 8, 25)
    );

    public static void render(GuiGraphics guiGraphics, int originX, int originY, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());

        RenderSystem.disableDepthTest();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 200.0f);

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
                color = (color & 0x00FFFFFF) | 0xFF000000;
            }

            int bx = originX + box.relX();
            int by = originY + box.relY();
            guiGraphics.fill(bx, by, bx + box.width(), by + box.height(), color);
            int outlineColor = isHovered ? 0xFFFFFFFF : (data.isSplinted(box.limb()) ? 0xFF55FFFF : 0xFF1E1E1E);
            guiGraphics.renderOutline(bx, by, box.width(), box.height(), outlineColor);
        }

        guiGraphics.pose().popPose();
        guiGraphics.flush();
        RenderSystem.enableDepthTest();

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
                    Component splintName = st != null ? Component.translatable(st.getTranslationKey()) : Component.translatable("humannature.splint_tier.generic");
                    tooltip.add(Component.translatable("gui.humannature.tooltip.limb.splinted", splintName, data.getSplintDurability(hoveredBox.limb())).withStyle(ChatFormatting.AQUA));
                    if (PhysiologyModule.hasCalciumBuff(player.getUUID())) {
                        tooltip.add(Component.translatable("gui.humannature.tooltip.limb.osteogenesis").withStyle(ChatFormatting.LIGHT_PURPLE));
                    }
                    ItemStack splint = InjuryEventHandler.findFirstSplint(player);
                    if (!splint.isEmpty()) {
                        tooltip.add(Component.translatable("gui.humannature.tooltip.limb.click_replace", splint.getHoverName()).withStyle(ChatFormatting.YELLOW));
                    }
                } else {
                    ItemStack splint = InjuryEventHandler.findFirstSplint(player);
                    if (!splint.isEmpty()) {
                        tooltip.add(Component.translatable("gui.humannature.tooltip.limb.click_treat", splint.getHoverName()).withStyle(ChatFormatting.YELLOW));
                    } else {
                        tooltip.add(Component.translatable("gui.humannature.tooltip.limb.need_splint").withStyle(ChatFormatting.GRAY));
                    }
                }
            } else {
                tooltip.add(Component.translatable("gui.humannature.tooltip.limb.no_fractures").withStyle(ChatFormatting.GREEN));
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

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());

        for (LimbHitbox box : HITBOXES) {
            if (box.contains((int) mouseX, (int) mouseY, originX, originY)) {
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
        return false;
    }
}
