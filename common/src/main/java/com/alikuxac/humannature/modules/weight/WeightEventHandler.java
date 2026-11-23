package com.alikuxac.humannature.modules.weight;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.config.ClientConfig;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.advancement.HumanNatureAdvancements;
import com.alikuxac.humannature.init.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class WeightEventHandler {

    public static final ResourceLocation ENCUMBRANCE_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "encumbrance_speed_penalty");

    public static void recalculateWeight(Player player) {
        if (player == null || !ServerConfig.ENABLE_WEIGHT.get()) {
            if (player != null) {
                clearMovementSpeedModifier(player);
            }
            return;
        }

        float weight = WeightCalculator.calculateWeight(player);
        float maxCapacity = WeightCalculator.calculateMaxCapacity(player);
        PlayerWeightData data = WeightModule.getOrCreateWeightData(player.getUUID());
        data.setCachedWeight(weight);
        data.setCachedMaxCapacity(maxCapacity);

        EncumbranceTier prevTier = data.getPreviousTier();
        EncumbranceTier currentTier = data.getEncumbranceTier();

        if (currentTier == EncumbranceTier.OVERBURDENED && prevTier != EncumbranceTier.OVERBURDENED) {
            HumanNatureAdvancements.trigger(player, HumanNatureAdvancements.OVERBURDENED);
            boolean showWarning = ClientConfig.SHOW_ACTIONBAR_WEIGHT_WARNINGS == null || ClientConfig.SHOW_ACTIONBAR_WEIGHT_WARNINGS.get();
            if (showWarning) {
                int pct = Math.round(data.getWeightRatio() * 100.0f);
                player.displayClientMessage(Component.translatable("message.humannature.encumbered", pct).withStyle(ChatFormatting.RED), true);
            }
        }
        data.setPreviousTier(currentTier);

        updateMovementSpeedModifier(player, currentTier);
    }

    public static void updateMovementSpeedModifier(Player player, EncumbranceTier tier) {
        if (player == null) return;
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        speedAttr.removeModifier(ENCUMBRANCE_SPEED_MODIFIER_ID);
        if (player.isCreative() || player.isSpectator()) return;

        if (tier != null && tier != EncumbranceTier.NORMAL && ServerConfig.ENABLE_WEIGHT.get()) {
            double penalty = tier.getSpeedPenalty();
            if (penalty > 0.0) {
                speedAttr.addTransientModifier(new AttributeModifier(
                        ENCUMBRANCE_SPEED_MODIFIER_ID,
                        -penalty,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }
    }

    public static void clearMovementSpeedModifier(Player player) {
        if (player == null) return;
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(ENCUMBRANCE_SPEED_MODIFIER_ID);
        }
    }

    public static void onPlayerTick(Player player) {
        if (player == null || !ServerConfig.ENABLE_WEIGHT.get() || player.isCreative() || player.isSpectator()) {
            return;
        }

        PlayerWeightData data = WeightModule.getOrCreateWeightData(player.getUUID());

        if (player.tickCount % 20 == 0) {
            recalculateWeight(player);
        }

        if (player.isInWater() || player.isUnderWater()) {
            EncumbranceTier tier = data.getEncumbranceTier();
            if (tier != EncumbranceTier.NORMAL) {
                double sinkPull = tier.getSinkPull();
                if (sinkPull > 0.0) {
                    Vec3 delta = player.getDeltaMovement();
                    player.setDeltaMovement(delta.x, delta.y - sinkPull, delta.z);
                }
            }
        }
    }

    public static float getFallDamageMultiplier(Player player) {
        if (player == null || !ServerConfig.ENABLE_WEIGHT.get() || player.isCreative() || player.isSpectator()) {
            return 1.0f;
        }
        PlayerWeightData data = WeightModule.getOrCreateWeightData(player.getUUID());
        return (float) data.getEncumbranceTier().getFallDamageMultiplier();
    }

    public static void onItemPickup(Player player) {
        recalculateWeight(player);
    }

    public static void onItemToss(Player player) {
        recalculateWeight(player);
    }

    public static void onContainerClose(Player player) {
        recalculateWeight(player);
    }

    public static void onEquipmentChange(Player player) {
        recalculateWeight(player);
        checkLoadBearingEquipment(player);
    }

    public static void onPlayerLoggedIn(Player player) {
        recalculateWeight(player);
        checkLoadBearingEquipment(player);
    }

    private static void checkLoadBearingEquipment(Player player) {
        if (!(player instanceof ServerPlayer) || !ServerConfig.ENABLE_WEIGHT.get()
                || player.isCreative() || player.isSpectator()) return;
        var enchantment = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(ModEnchantments.LOAD_BEARING);
        if (enchantment.isEmpty()) return;
        int maximum = enchantment.get().value().getMaxLevel();
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            var stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof ArmorItem
                    && WeightCalculator.getEnchantmentLevel(stack, ModEnchantments.LOAD_BEARING) >= maximum) {
                HumanNatureAdvancements.trigger(player, HumanNatureAdvancements.MAX_LOAD_BEARING);
                return;
            }
        }
    }

    public static void onPlayerRespawn(Player player) {
        PlayerWeightData data = WeightModule.getWeightData(player.getUUID());
        if (data != null) {
            data.reset();
        }
        clearMovementSpeedModifier(player);
        recalculateWeight(player);
    }

    public static void onPlayerLoggedOut(Player player) {
        clearMovementSpeedModifier(player);
        WeightModule.removeWeightData(player.getUUID());
    }
}
