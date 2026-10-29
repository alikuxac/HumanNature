package com.alikuxac.humannature.modules.temperature;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TemperatureEventHandler {

    public static final TagKey<Item> HOT_STEWS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "hot_stews"));
    public static final TagKey<Item> COMMON_FOODS_SOUPS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/soups"));
    public static final TagKey<Item> COMMON_SOUPS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "soups"));

    public static final ResourceLocation HYPOTHERMIA_ATTACK_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "hypothermia_attack_speed");
    public static final ResourceLocation HYPOTHERMIA_BLOCK_RANGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "hypothermia_block_range");

    private static final AttributeModifier HYPOTHERMIA_ATTACK_SPEED_MODIFIER = new AttributeModifier(
            HYPOTHERMIA_ATTACK_SPEED_MODIFIER_ID,
            -0.35,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static final AttributeModifier HYPOTHERMIA_BLOCK_RANGE_MODIFIER = new AttributeModifier(
            HYPOTHERMIA_BLOCK_RANGE_MODIFIER_ID,
            -1.0,
            AttributeModifier.Operation.ADD_VALUE
    );

    public static boolean isHotStew(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(HOT_STEWS) || stack.is(COMMON_FOODS_SOUPS) || stack.is(COMMON_SOUPS)) {
            return true;
        }
        return stack.is(Items.MUSHROOM_STEW)
                || stack.is(Items.RABBIT_STEW)
                || stack.is(Items.BEETROOT_SOUP)
                || stack.is(Items.SUSPICIOUS_STEW);
    }

    public static void handleItemConsumption(Player player, ItemStack stack) {
        if (!TemperatureModule.isModuleEnabled()) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (isHotStew(stack)) {
            PlayerTemperatureData data = TemperatureModule.getOrCreateTemperatureData(player.getUUID());
            data.setWetnessBuffer(data.getWetnessBuffer() * 0.5f);
            player.setTicksFrozen(0);
            int durationTicks = ServerConfig.INTERNAL_WARMING_DURATION_TICKS.get();
            data.setInternalWarmingTicks(durationTicks);
            removeHypothermiaDebuffs(player, data);

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.4F, 1.2F);
        }
    }

    public static void onPlayerTick(Player player) {
        if (!TemperatureModule.isModuleEnabled()) return;
        if (player.isCreative() || player.isSpectator()) return;

        PlayerTemperatureData data = TemperatureModule.getOrCreateTemperatureData(player.getUUID());

        data.decrementHeatScanCooldown();
        data.decrementSteamSoundCooldown();
        data.decrementStumbleCooldown();
        data.decrementInternalWarmingTicks();

        if (data.getHeatScanCooldown() <= 0) {
            boolean nearHeat = scanForNearbyHeatSources(player);
            data.setNearHeatSource(nearHeat);
            data.setHeatScanCooldown(20);
        } else if (player.isOnFire() || player.isInLava()) {
            data.setNearHeatSource(true);
        }

        if (data.isNearHeatSource()) {
            handleHeatRewarmingTick(player, data);
        } else {
            handleWetnessTick(player, data);
        }

        handleHypothermiaTick(player, data);
    }

    private static void handleHeatRewarmingTick(Player player, PlayerTemperatureData data) {
        if (!TemperatureModule.isHeatRewarmingEnabled()) return;

        double dryingTimeSec = Math.max(1.0, ServerConfig.HEAT_DRYING_TIME_SECONDS.get());
        float dryingPerTick = (float) (1.0 / (dryingTimeSec * 20.0));
        data.reduceWetness(dryingPerTick);

        int currentFrozen = player.getTicksFrozen();
        if (currentFrozen > 0) {
            int thawAmount = (int) Math.round(2.0 * ServerConfig.HEAT_THAW_MULTIPLIER.get());
            player.setTicksFrozen(Math.max(0, currentFrozen - thawAmount));
        }

        if ((data.getWetnessBuffer() > 0.0f || currentFrozen > 0) && data.getSteamSoundCooldown() <= 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.2F, 1.1F);
            data.setSteamSoundCooldown(35);
        }
    }

    private static void handleWetnessTick(Player player, PlayerTemperatureData data) {
        if (!TemperatureModule.isWetnessAccumulationEnabled()) return;

        boolean inWater = player.isInWater() || player.isUnderWater();
        boolean inRain = player.level().isRainingAt(player.blockPosition());

        if (inWater) {
            float delta = ServerConfig.WETNESS_WATER_ACCUMULATION_RATE.get().floatValue();
            float reduction = calculateFreezeImmuneReduction(player);
            delta *= (1.0f - reduction);
            data.addWetness(delta);
        } else if (inRain) {
            float delta = ServerConfig.WETNESS_RAIN_ACCUMULATION_RATE.get().floatValue();
            if (player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                delta *= ServerConfig.WETNESS_NO_HELMET_MULTIPLIER.get().floatValue();
            }
            float reduction = calculateFreezeImmuneReduction(player);
            delta *= (1.0f - reduction);
            data.addWetness(delta);
        } else {
            data.reduceWetness(0.0005F);
        }
    }

    private static void handleHypothermiaTick(Player player, PlayerTemperatureData data) {
        if (!TemperatureModule.isHypothermiaEnabled()) {
            removeHypothermiaDebuffs(player, data);
            return;
        }

        if (data.hasInternalWarming()) {
            if (player.getTicksFrozen() > 0) {
                player.setTicksFrozen(0);
            }
            removeHypothermiaDebuffs(player, data);
            return;
        }

        if (data.getWetnessBuffer() >= 1.0f && !data.isNearHeatSource()) {
            int currentFrozen = player.getTicksFrozen();
            int maxFreezeTicks = player.getTicksRequiredToFreeze() + 80;
            if (currentFrozen < maxFreezeTicks) {
                player.setTicksFrozen(Math.min(maxFreezeTicks, currentFrozen + 3));
            }
        }

        int currentFrozen = player.getTicksFrozen();
        int requiredFreeze = player.getTicksRequiredToFreeze();
        int penaltyThreshold = (int) Math.round(requiredFreeze * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get());

        if (currentFrozen >= penaltyThreshold) {
            applyHypothermiaDebuffs(player, data);

            if (data.getStumbleCooldown() <= 0) {
                if (player.onGround() && (player.walkDist - player.walkDistO > 0.005F)) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, false, true));
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.4F, 1.1F + player.getRandom().nextFloat() * 0.2F);
                    data.setStumbleCooldown(300 + player.getRandom().nextInt(101));
                }
            }
        } else {
            removeHypothermiaDebuffs(player, data);
        }
    }

    private static void applyHypothermiaDebuffs(Player player, PlayerTemperatureData data) {
        if (ServerConfig.HYPOTHERMIA_MINING_FATIGUE.get()) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 0, false, false, true));
        }

        AttributeInstance attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttr != null && !attackSpeedAttr.hasModifier(HYPOTHERMIA_ATTACK_SPEED_MODIFIER_ID)) {
            attackSpeedAttr.addTransientModifier(HYPOTHERMIA_ATTACK_SPEED_MODIFIER);
        }

        AttributeInstance blockRangeAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (blockRangeAttr != null && !blockRangeAttr.hasModifier(HYPOTHERMIA_BLOCK_RANGE_MODIFIER_ID)) {
            blockRangeAttr.addTransientModifier(HYPOTHERMIA_BLOCK_RANGE_MODIFIER);
        }

        data.setHypothermiaDebuffActive(true);
    }

    public static void removeHypothermiaDebuffs(Player player, PlayerTemperatureData data) {
        if (data != null && !data.isHypothermiaDebuffActive()) {
            return;
        }

        AttributeInstance attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttr != null && attackSpeedAttr.hasModifier(HYPOTHERMIA_ATTACK_SPEED_MODIFIER_ID)) {
            attackSpeedAttr.removeModifier(HYPOTHERMIA_ATTACK_SPEED_MODIFIER_ID);
        }

        AttributeInstance blockRangeAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (blockRangeAttr != null && blockRangeAttr.hasModifier(HYPOTHERMIA_BLOCK_RANGE_MODIFIER_ID)) {
            blockRangeAttr.removeModifier(HYPOTHERMIA_BLOCK_RANGE_MODIFIER_ID);
        }

        if (data != null) {
            data.setHypothermiaDebuffActive(false);
        }
    }

    public static boolean scanForNearbyHeatSources(Player player) {
        if (player.isOnFire() || player.isInLava()) return true;

        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        int radius = (int) Math.round(ServerConfig.HEAT_SOURCE_RADIUS.get());
        int radiusY = Math.min(radius, 2);

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-radius, -radiusY, -radius),
                playerPos.offset(radius, radiusY, radius))) {
            BlockState state = level.getBlockState(pos);
            if (isHeatSourceBlock(state)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHeatSourceBlock(BlockState state) {
        if (state.is(BlockTags.CAMPFIRES) && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
            return true;
        }
        if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT)) {
            return true;
        }
        if (state.is(Blocks.LAVA) || state.getFluidState().is(FluidTags.LAVA)) {
            return true;
        }
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.MAGMA_BLOCK)) {
            return true;
        }
        return false;
    }

    public static float calculateFreezeImmuneReduction(Player player) {
        int freezeImmunePieces = 0;
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
                freezeImmunePieces++;
            }
        }
        if (freezeImmunePieces == 0) return 0.0F;
        float maxReduction = ServerConfig.WETNESS_FREEZE_IMMUNE_REDUCTION.get().floatValue();
        return (freezeImmunePieces / 4.0F) * maxReduction;
    }

    public static void onPlayerRespawn(Player player) {
        PlayerTemperatureData data = TemperatureModule.getTemperatureData(player.getUUID());
        removeHypothermiaDebuffs(player, data);
        if (data != null) {
            data.reset();
        }
        player.setTicksFrozen(0);
    }

    public static void onPlayerLoggedOut(Player player) {
        PlayerTemperatureData data = TemperatureModule.getTemperatureData(player.getUUID());
        removeHypothermiaDebuffs(player, data);
        TemperatureModule.removeTemperatureData(player.getUUID());
    }
}
