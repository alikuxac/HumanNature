package com.alikuxac.humannature.modules.temperature;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.api.event.HumanNatureEvents;
import com.alikuxac.humannature.api.event.HypothermiaEvent;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.modules.weight.EncumbranceTier;
import com.alikuxac.humannature.modules.weight.WeightCalculator;
import com.alikuxac.humannature.modules.weight.WeightModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
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
        handleHeatStressTick(player, data);
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

        boolean isCold = isColdEnvironment(player.level(), player.blockPosition());
        boolean isWet = data.getWetnessBuffer() > 0.0f;
        boolean isFullySoaked = data.getWetnessBuffer() >= 1.0f;
        boolean isRainingOrSnowing = player.level().isRainingAt(player.blockPosition());

        if (!data.isNearHeatSource() && (isFullySoaked || (isCold && (isWet || isRainingOrSnowing)))) {
            int currentFrozen = player.getTicksFrozen();
            int maxFreezeTicks = player.getTicksRequiredToFreeze() + 80;
            if (currentFrozen < maxFreezeTicks) {
                float baseDelta = isFullySoaked ? 3.0f : (isCold && isWet ? 2.0f : 1.0f);
                float insulationFactor = calculateArmorInsulationFactor(player, isWet, isCold);
                int freezeDelta = Math.max(1, Math.round(baseDelta * insulationFactor));
                player.setTicksFrozen(Math.min(maxFreezeTicks, currentFrozen + freezeDelta));
            }
        }

        int currentFrozen = player.getTicksFrozen();
        int requiredFreeze = player.getTicksRequiredToFreeze();
        int penaltyThreshold = (int) Math.round(requiredFreeze * ServerConfig.HYPOTHERMIA_PENALTY_THRESHOLD.get());

        if (currentFrozen > 0) {
            float targetTemp = Math.max(30.0f, 37.0f - (currentFrozen / (float) requiredFreeze) * 7.0f);
            if (data.getCoreTemperature() > targetTemp) {
                data.modifyCoreTemperature(-0.01f);
            }
        }

        if (currentFrozen >= penaltyThreshold) {
            applyHypothermiaDebuffs(player, data);
            HumanNatureEvents.postHypothermia(new HypothermiaEvent(player, data.getCoreTemperature()));

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

    private static void handleHeatStressTick(Player player, PlayerTemperatureData data) {
        if (!TemperatureModule.isModuleEnabled() || !ServerConfig.ENABLE_HEAVY_LOAD_HEAT_STRESS.get()) {
            return;
        }

        EncumbranceTier tier = WeightModule.getOrCreateWeightData(player.getUUID()).getEncumbranceTier();
        boolean isOverburdened = tier == EncumbranceTier.OVERBURDENED;
        boolean hotBiome = isHotBiome(player.level(), player.blockPosition());

        if (isOverburdened && hotBiome) {
            double riseRate = ServerConfig.HEAT_STRESS_TEMP_RISE_RATE != null ?
                    ServerConfig.HEAT_STRESS_TEMP_RISE_RATE.get() : 0.0015;
            int metalPieces = countMetallicPieces(player);
            float metalHeatFactor = 1.0f + (metalPieces * 0.15f);
            data.modifyCoreTemperature((float) (riseRate * metalHeatFactor));

            if (ServerConfig.ENABLE_PHYSIOLOGY.get() && PhysiologyModule.isExertionEnabled()) {
                double exertionDrain = ServerConfig.HEAT_STRESS_EXERTION_DRAIN != null ?
                        ServerConfig.HEAT_STRESS_EXERTION_DRAIN.get() : 0.10;
                PlayerPhysiologyData physData = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
                physData.addExertion((float) exertionDrain);
                if (physData.getExertionScore() >= 100.0f) {
                    physData.setMuscleFailure(true);
                    if (player.isSprinting()) {
                        player.setSprinting(false);
                    }
                }
            }
        } else if (data.getCoreTemperature() > 37.0f && !player.isOnFire()) {
            float coolingRate = player.isInWater() ? 0.02f : (hotBiome ? 0.0005f : 0.005f);
            data.modifyCoreTemperature(-coolingRate);
            if (data.getCoreTemperature() < 37.0f) {
                data.setCoreTemperature(37.0f);
            }
        }

        double overheatThreshold = ServerConfig.HEAT_STRESS_OVERHEAT_THRESHOLD != null ?
                ServerConfig.HEAT_STRESS_OVERHEAT_THRESHOLD.get() : 39.5;
        double damageThreshold = ServerConfig.HEAT_STRESS_DAMAGE_THRESHOLD != null ?
                ServerConfig.HEAT_STRESS_DAMAGE_THRESHOLD.get() : 42.0;

        data.decrementHeatExhaustionSoundCooldown();
        data.decrementHeatDamageCooldown();

        if (data.getCoreTemperature() > overheatThreshold) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, true));

            if (data.getHeatExhaustionSoundCooldown() <= 0) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 0.7F, 0.8F + player.getRandom().nextFloat() * 0.3F);
                data.setHeatExhaustionSoundCooldown(60);
            }
        }

        if (data.getCoreTemperature() > damageThreshold) {
            if (data.getHeatDamageCooldown() <= 0) {
                int interval = ServerConfig.HEAT_STRESS_DAMAGE_INTERVAL_TICKS != null ?
                        ServerConfig.HEAT_STRESS_DAMAGE_INTERVAL_TICKS.get() : 40;
                data.setHeatDamageCooldown(interval);
                player.hurt(player.damageSources().dryOut(), 1.0f);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_HURT_ON_FIRE, SoundSource.PLAYERS, 0.5F, 1.2F);
            }
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

    public static int countLeatherPieces(Player player) {
        if (player == null) return 0;
        int count = 0;
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
                count++;
            }
        }
        return count;
    }

    public static int countMetallicPieces(Player player) {
        if (player == null) return 0;
        int count = 0;
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && (stack.is(WeightCalculator.COMMON_METALLIC_ITEMS) || stack.is(WeightCalculator.MOD_METALLIC_ITEMS))) {
                count++;
            }
        }
        return count;
    }

    public static boolean isColdEnvironment(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        Holder<Biome> biome = level.getBiome(pos);
        return biome.value().coldEnoughToSnow(pos) || biome.value().getBaseTemperature() < 0.15f;
    }

    public static boolean isHotBiome(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        if (level.dimension() == Level.NETHER) return true;
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_BADLANDS)) return true;
        ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
        if (key != null && (key.equals(Biomes.DESERT) || key.equals(Biomes.BADLANDS) || key.equals(Biomes.ERODED_BADLANDS) || key.equals(Biomes.WOODED_BADLANDS))) {
            return true;
        }
        return biome.value().getBaseTemperature() >= 1.5f;
    }

    public static float calculateArmorInsulationFactor(Player player, boolean isWet, boolean isCold) {
        if (!ServerConfig.ENABLE_ARMOR_THERMAL_INSULATION.get() || player == null) {
            return 1.0f;
        }

        float factor = 1.0f;
        int leatherPieces = countLeatherPieces(player);
        if (leatherPieces > 0) {
            double maxReduction = ServerConfig.LEATHER_INSULATION_FREEZE_REDUCTION != null ?
                    ServerConfig.LEATHER_INSULATION_FREEZE_REDUCTION.get() : 0.60;
            float leatherReduction = (leatherPieces / 4.0f) * (float) maxReduction;
            factor *= (1.0f - leatherReduction);
        }

        if (isWet || isCold) {
            int metallicPieces = countMetallicPieces(player);
            if (metallicPieces > 0) {
                double conductionMult = ServerConfig.METALLIC_ARMOR_COLD_CONDUCTION_MULTIPLIER != null ?
                        ServerConfig.METALLIC_ARMOR_COLD_CONDUCTION_MULTIPLIER.get() : 1.50;
                float metallicConduction = 1.0f + (metallicPieces / 4.0f) * (float) (conductionMult - 1.0);
                factor *= metallicConduction;
            }
        }

        return factor;
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
    }
}
