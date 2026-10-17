package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class PhysiologyEventHandler {

    public static void onPlayerTick(Player player, boolean isMoving, boolean isSleeping) {
        if (!ServerConfig.ENABLE_PHYSIOLOGY.get()) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (player.isDeadOrDying()) return;

        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());

        // Sleep Deprivation ticking
        if (PhysiologyModule.isSleepDeprivationEnabled() && !isSleeping) {
            data.incrementAwakeTicks();

            if (data.isBlinkEligible()) {
                if (data.getBlinkCooldown() > 0) {
                    data.decrementBlinkCooldown();
                } else if (isMoving && !player.hasEffect(MobEffects.DARKNESS)) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 0, false, false, true));
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.8F, 0.7F);

                    int minInterval = ServerConfig.SLEEP_FATIGUE_BLINK_MIN_INTERVAL != null ?
                            ServerConfig.SLEEP_FATIGUE_BLINK_MIN_INTERVAL.get() : 300;
                    int maxInterval = ServerConfig.SLEEP_FATIGUE_BLINK_MAX_INTERVAL != null ?
                            ServerConfig.SLEEP_FATIGUE_BLINK_MAX_INTERVAL.get() : 800;
                    if (maxInterval < minInterval) {
                        maxInterval = minInterval;
                    }

                    int intervalRange = Math.max(1, maxInterval - minInterval + 1);
                    int nextCooldown = minInterval + player.getRandom().nextInt(intervalRange);
                    data.setBlinkCooldown(nextCooldown);
                }
            }
        }

        // Toxin Buildup passive decay
        if (PhysiologyModule.isToxinBuildupEnabled()) {
            if (data.getToxinBuildup() > 0.0f) {
                data.incrementTicksSinceLastExposure();
                int decayDelay = ServerConfig.TOXIN_DECAY_DELAY_TICKS != null ?
                        ServerConfig.TOXIN_DECAY_DELAY_TICKS.get() : 80;

                if (data.getTicksSinceLastExposure() >= decayDelay) {
                    boolean isStandingStill = !isMoving;
                    boolean isResting = isSleeping || (player.isCrouching() && isStandingStill);
                    boolean nearHeat = isNearHeatSource(player);

                    double multiplier = (isResting || nearHeat) ?
                            (ServerConfig.TOXIN_RESTING_HEAT_DECAY_MULTIPLIER != null ?
                                    ServerConfig.TOXIN_RESTING_HEAT_DECAY_MULTIPLIER.get() : 3.0)
                            : 1.0;
                    double ratePerSec = ServerConfig.TOXIN_DECAY_RATE_PER_SECOND != null ?
                            ServerConfig.TOXIN_DECAY_RATE_PER_SECOND.get() : 2.0;
                    float decayPerTick = (float) ((ratePerSec / 20.0) * multiplier);
                    data.decayToxinBuildup(decayPerTick);
                }
            }
        }
    }

    public static boolean handleMobEffectApplicable(Player player, MobEffectInstance effectInstance) {
        if (!PhysiologyModule.isToxinBuildupEnabled()) return false;
        if (player.isCreative() || player.isSpectator()) return false;
        if (PhysiologyModule.isErupting(player.getUUID())) return false;

        Holder<MobEffect> effect = effectInstance.getEffect();
        boolean isPoison = effect.is(MobEffects.POISON);
        boolean isWither = effect.is(MobEffects.WITHER);

        if (!isPoison && !isWither) {
            return false;
        }

        ToxinType toxinType = isPoison ? ToxinType.POISON : ToxinType.WITHER;
        double baseHit = ServerConfig.TOXIN_BASE_BUILDUP_PER_HIT != null ?
                ServerConfig.TOXIN_BASE_BUILDUP_PER_HIT.get() : 25.0;
        float durationFactor = (effectInstance.getDuration() / 20.0f) * 1.5f;
        float amplifierFactor = effectInstance.getAmplifier() + 1;
        float baseValue = (float) Math.max(baseHit, Math.min(100.0, durationFactor * amplifierFactor));
        float multiplier = PhysiologyModule.calculateToxinMultiplier(player.getUUID());
        float addedBuildup = baseValue * multiplier;

        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        data.addToxinBuildup(addedBuildup, toxinType, effectInstance.getDuration(), effectInstance.getAmplifier());

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                isPoison ? SoundEvents.HONEY_DRINK : SoundEvents.WITHER_HURT,
                SoundSource.PLAYERS, 0.7F, 1.4F);

        if (data.getToxinBuildup() >= 100.0f) {
            triggerToxinEruption(player, data);
        }

        return true;
    }

    public static void triggerToxinEruption(Player player, PlayerPhysiologyData data) {
        UUID uuid = player.getUUID();
        ToxinType type = data.getLastToxinType();
        Holder<MobEffect> effectHolder = type == ToxinType.WITHER ? MobEffects.WITHER : MobEffects.POISON;
        int duration = Math.max(200, data.getPendingDuration());
        int amplifier = Math.max(0, data.getPendingAmplifier());

        PhysiologyModule.setErupting(uuid, true);
        try {
            player.addEffect(new MobEffectInstance(effectHolder, duration, amplifier, false, true, true));
        } finally {
            PhysiologyModule.setErupting(uuid, false);
        }

        data.resetToxin();

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                type == ToxinType.WITHER ? SoundEvents.WITHER_SHOOT : SoundEvents.SPLASH_POTION_BREAK,
                SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    public static boolean isNearHeatSource(Player player) {
        if (player.isOnFire() || player.isInLava()) return true;

        BlockPos playerPos = player.blockPosition();
        Level level = player.level();
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-2, -1, -2), playerPos.offset(2, 2, 2))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.CAMPFIRES) && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
                return true;
            }
            if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK)) {
                return true;
            }
        }
        return false;
    }

    public static void handleItemConsumption(Player player, ItemStack stack) {
        if (!PhysiologyModule.isToxinBuildupEnabled()) return;
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        if (data.getToxinBuildup() <= 0.0f) return;

        if (stack.is(Items.MILK_BUCKET)) {
            data.resetToxin();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (stack.is(Items.HONEY_BOTTLE)) {
            data.reduceToxinBuildup(0.50f);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
    }

    public static void onPlayerWakeUp(Player player, int consecutiveSleepTicks) {
        if (!PhysiologyModule.isSleepDeprivationEnabled()) return;

        boolean sleptThroughNight = consecutiveSleepTicks >= 90 || player.level().isDay();
        if (player instanceof ServerPlayer serverPlayer) {
            int timeSinceRest = serverPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            if (timeSinceRest < 100) {
                sleptThroughNight = true;
            }
        }

        if (sleptThroughNight) {
            PhysiologyModule.resetSleepDeprivation(player.getUUID());
        }
    }

    public static void onPlayerDeath(Player player) {
        PhysiologyModule.reset(player.getUUID());
    }

    public static void onPlayerRespawn(Player player) {
        PhysiologyModule.reset(player.getUUID());
    }
}
