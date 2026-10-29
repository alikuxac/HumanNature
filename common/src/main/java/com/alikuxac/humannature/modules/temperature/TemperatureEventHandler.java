package com.alikuxac.humannature.modules.temperature;

import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TemperatureEventHandler {

    public static void onPlayerTick(Player player) {
        if (!TemperatureModule.isModuleEnabled()) return;
        if (player.isCreative() || player.isSpectator()) return;

        PlayerTemperatureData data = TemperatureModule.getOrCreateTemperatureData(player.getUUID());

        data.decrementHeatScanCooldown();
        data.decrementSteamSoundCooldown();
        data.decrementStumbleCooldown();

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
        if (!TemperatureModule.isHypothermiaEnabled()) return;

        if (data.getWetnessBuffer() >= 1.0f && !data.isNearHeatSource()) {
            int currentFrozen = player.getTicksFrozen();
            int maxFreezeTicks = player.getTicksRequiredToFreeze() + 80;
            if (currentFrozen < maxFreezeTicks) {
                player.setTicksFrozen(Math.min(maxFreezeTicks, currentFrozen + 3));
            }

            if (player.getTicksFrozen() >= 70 && data.getStumbleCooldown() <= 0) {
                if (player.onGround() && (player.walkDist - player.walkDistO > 0.005F)) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, false, true));
                    data.setStumbleCooldown(80 + player.getRandom().nextInt(41));
                }
            }
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
        if (data != null) {
            data.reset();
        }
        player.setTicksFrozen(0);
    }

    public static void onPlayerLoggedOut(Player player) {
        TemperatureModule.removeTemperatureData(player.getUUID());
    }
}
