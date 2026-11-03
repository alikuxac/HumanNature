package com.alikuxac.humannature.client;

import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.weight.WeightCalculator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

        if (contents > 0.001f) {
            tooltip.add(Component.translatable(
                    "tooltip.humannature.weight_container",
                    formatWeight(total),
                    formatWeight(contents)
            ).withStyle(ChatFormatting.GRAY));
        } else if (stack.getCount() > 1) {
            tooltip.add(Component.translatable(
                    "tooltip.humannature.weight_stack",
                    formatWeight(total),
                    formatWeight(unit)
            ).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable(
                    "tooltip.humannature.weight_single",
                    formatWeight(unit)
            ).withStyle(ChatFormatting.GRAY));
        }
    }

    public static String formatWeight(float weight) {
        if (weight <= 0.0f) return "0.0";
        if (weight < 0.1f) {
            return String.format(Locale.ROOT, "%.2f", weight);
        }
        return String.format(Locale.ROOT, "%.1f", weight);
    }
}

