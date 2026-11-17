package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.weight.EncumbranceTier;
import com.alikuxac.humannature.modules.weight.PlayerWeightData;
import com.alikuxac.humannature.modules.weight.WeightCalculator;
import com.alikuxac.humannature.modules.weight.WeightModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public class WeightTooltipHandler {

    public static void onTooltip(ItemStack stack, List<Component> tooltip) {
        if (stack == null || stack.isEmpty()) return;
        if (ServerConfig.ENABLE_WEIGHT != null && !ServerConfig.ENABLE_WEIGHT.get()) return;
        if (ClientConfig.ENABLE_WEIGHT_TOOLTIP != null && !ClientConfig.ENABLE_WEIGHT_TOOLTIP.get()) return;

        float unit = WeightCalculator.getItemWeight(stack);
        float total = WeightCalculator.calculateStackWeight(stack);
        float contents = WeightCalculator.getContentsWeight(stack);

        MutableComponent baseComponent;
        if (contents > 0.001f) {
            baseComponent = Component.translatable(
                    "tooltip.humannature.weight_container",
                    formatWeight(total),
                    formatWeight(contents)
            );
        } else if (stack.getCount() > 1) {
            baseComponent = Component.translatable(
                    "tooltip.humannature.weight_stack",
                    formatWeight(total),
                    formatWeight(unit)
            );
        } else {
            baseComponent = Component.translatable(
                    "tooltip.humannature.weight_single",
                    formatWeight(unit)
            );
        }
        baseComponent.withStyle(ChatFormatting.GRAY);

        if (Screen.hasShiftDown()) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc != null ? mc.player : null;
            if (player != null) {
                PlayerWeightData weightData = WeightModule.getOrCreateWeightData(player.getUUID());
                float curWeight = weightData.getCachedWeight();
                if (curWeight <= 0.0f) {
                    curWeight = WeightCalculator.calculateWeight(player);
                    weightData.setCachedWeight(curWeight);
                }
                float maxWeight = WeightCalculator.calculateMaxCapacity(player);
                float weightRatio = maxWeight > 0.0f ? (curWeight / maxWeight) : 0.0f;
                int pct = Math.round(weightRatio * 100.0f);
                EncumbranceTier tier = EncumbranceTier.fromWeightRatio(weightRatio);

                ChatFormatting tierColor = switch (tier) {
                    case NORMAL -> ChatFormatting.GREEN;
                    case LIGHT -> ChatFormatting.YELLOW;
                    case MEDIUM -> ChatFormatting.GOLD;
                    case OVERBURDENED -> ChatFormatting.RED;
                };

                Component tierComponent = Component.literal(tier.getDisplayName()).withStyle(tierColor);
                Component totalSuffix = Component.translatable("tooltip.humannature.weight_total_suffix", pct, tierComponent).withStyle(ChatFormatting.GRAY);
                baseComponent.append(Component.literal(" ")).append(totalSuffix);
            }
        }

        tooltip.add(baseComponent);
    }

    public static String formatWeight(float weight) {
        if (weight <= 0.0f) return "0.0";
        if (weight < 0.1f) {
            return String.format(Locale.ROOT, "%.2f", weight);
        }
        return String.format(Locale.ROOT, "%.1f", weight);
    }
}

