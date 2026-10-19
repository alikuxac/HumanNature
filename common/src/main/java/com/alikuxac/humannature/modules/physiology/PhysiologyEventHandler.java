package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PhysiologyEventHandler {

    public static final ResourceLocation EXERTION_ATTACK_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "exertion_attack_speed");
    public static final ResourceLocation EXERTION_JUMP_STRENGTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "exertion_jump_strength");

    private static final AttributeModifier EXERTION_ATTACK_SPEED_MODIFIER = new AttributeModifier(
            EXERTION_ATTACK_SPEED_MODIFIER_ID,
            -0.50,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static final AttributeModifier EXERTION_JUMP_STRENGTH_MODIFIER = new AttributeModifier(
            EXERTION_JUMP_STRENGTH_MODIFIER_ID,
            -1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    public static void onPlayerTick(Player player, boolean isMoving, boolean isSleeping) {
        if (!ServerConfig.ENABLE_PHYSIOLOGY.get()) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (player.isDeadOrDying()) return;

        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());

        if (PhysiologyModule.isExertionEnabled()) {
            handlePhysicalExertionTick(player, data, isMoving, isSleeping);
        }

        if (PhysiologyModule.isSwimmingCrampsEnabled()) {
            handleSwimmingCrampTick(player, data);
        }

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
        boolean sleptThroughNight = consecutiveSleepTicks >= 90 || player.level().isDay();
        if (player instanceof ServerPlayer serverPlayer) {
            int timeSinceRest = serverPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            if (timeSinceRest < 100) {
                sleptThroughNight = true;
            }
        }

        if (sleptThroughNight) {
            if (PhysiologyModule.isSleepDeprivationEnabled()) {
                PhysiologyModule.resetSleepDeprivation(player.getUUID());
            }
            PhysiologyModule.resetExertion(player.getUUID());
            PhysiologyModule.resetSwimmingCramps(player.getUUID());
            removeExertionModifiers(player);
        }
    }

    public static void onPlayerDeath(Player player) {
        PhysiologyModule.reset(player.getUUID());
        removeExertionModifiers(player);
    }

    public static void onPlayerRespawn(Player player) {
        PhysiologyModule.reset(player.getUUID());
        removeExertionModifiers(player);
    }

    private static void handlePhysicalExertionTick(Player player, PlayerPhysiologyData data, boolean isMoving, boolean isSleeping) {
        data.incrementTicksSinceLastMining();
        if (data.getTicksSinceLastMining() > 10) {
            data.resetContinuousMiningTicks();
        }

        data.incrementTicksSinceLastJump();

        if (player.isInWater() || player.isSwimming()) {
            BlockPos pos = player.blockPosition();
            FluidState fluid = player.level().getFluidState(pos);
            Vec3 flow = fluid.getFlow(player.level(), pos);
            if (flow.lengthSqr() > 0.005) {
                Vec3 motion = player.getDeltaMovement();
                if (flow.dot(motion) < -0.01) {
                    data.addExertion(0.75f);
                    if (data.getExertionScore() >= 100.0f) {
                        data.setMuscleFailure(true);
                    }
                }
            }
        }

        boolean isSprinting = player.isSprinting() && isMoving && !player.isPassenger() && !player.isFallFlying();
        if (isSprinting) {
            if (data.isMuscleFailure()) {
                player.setSprinting(false);
            } else {
                double drain = ServerConfig.EXERTION_SPRINT_DRAIN_PER_TICK != null ?
                        ServerConfig.EXERTION_SPRINT_DRAIN_PER_TICK.get() : 0.08;
                data.addExertion((float) drain);
                if (data.getExertionScore() >= 100.0f) {
                    data.setMuscleFailure(true);
                    player.setSprinting(false);
                }
            }
        }

        if (!isMoving && player.onGround() && data.getTicksSinceLastMining() > 10 && data.getTicksSinceLastJump() > 10 && !isSprinting) {
            data.incrementRestTicks();
            int reqRest = ServerConfig.EXERTION_REST_RECOVERY_TICKS != null ? ServerConfig.EXERTION_REST_RECOVERY_TICKS.get() : 70;
            data.decayExertion(100.0f / (float) reqRest);
            if (data.getRestTicks() >= reqRest) {
                data.resetExertion();
                removeExertionModifiers(player);
            }
        } else {
            data.resetRestTicks();
            if (!data.isMuscleFailure() && !isSprinting) {
                data.decayExertion(0.2f);
            }
        }

        AttributeInstance attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        AttributeInstance jumpStrengthAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (data.isMuscleFailure()) {
            if (player.isSprinting()) {
                player.setSprinting(false);
            }
            if (attackSpeedAttr != null && !attackSpeedAttr.hasModifier(EXERTION_ATTACK_SPEED_MODIFIER_ID)) {
                attackSpeedAttr.addTransientModifier(EXERTION_ATTACK_SPEED_MODIFIER);
            }
            if (jumpStrengthAttr != null && !jumpStrengthAttr.hasModifier(EXERTION_JUMP_STRENGTH_MODIFIER_ID)) {
                jumpStrengthAttr.addTransientModifier(EXERTION_JUMP_STRENGTH_MODIFIER);
            }
        } else {
            if (attackSpeedAttr != null && attackSpeedAttr.hasModifier(EXERTION_ATTACK_SPEED_MODIFIER_ID)) {
                attackSpeedAttr.removeModifier(EXERTION_ATTACK_SPEED_MODIFIER_ID);
            }
            if (jumpStrengthAttr != null && jumpStrengthAttr.hasModifier(EXERTION_JUMP_STRENGTH_MODIFIER_ID)) {
                jumpStrengthAttr.removeModifier(EXERTION_JUMP_STRENGTH_MODIFIER_ID);
            }
        }
    }

    private static void handleSwimmingCrampTick(Player player, PlayerPhysiologyData data) {
        if (player.isInWater() || player.isSwimming()) {
            data.incrementContinuousSwimmingTicks();
            data.resetCrampRestTicks();
            int threshold = ServerConfig.SWIMMING_CRAMP_THRESHOLD_TICKS != null ? ServerConfig.SWIMMING_CRAMP_THRESHOLD_TICKS.get() : 600;
            if (data.getContinuousSwimmingTicks() >= threshold && !data.isSwimmingCramped()) {
                data.setSwimmingCramped(true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_HURT_DROWN, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
            if (data.isSwimmingCramped()) {
                double penalty = ServerConfig.SWIMMING_CRAMP_SPEED_PENALTY != null ? ServerConfig.SWIMMING_CRAMP_SPEED_PENALTY.get() : 0.40;
                double factor = Math.max(0.1, 1.0 - penalty);
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x * factor, motion.y, motion.z * factor);
                if (player.level().getGameTime() % 40 == 0) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.5F, 0.7F);
                }
            }
        } else {
            boolean isAshore = player.onGround() && !player.isInWater();
            boolean isInBoat = player.getVehicle() != null;
            if (isAshore || isInBoat || !player.isInWater()) {
                data.incrementCrampRestTicks();
                int recTicks = ServerConfig.SWIMMING_CRAMP_RECOVERY_TICKS != null ? ServerConfig.SWIMMING_CRAMP_RECOVERY_TICKS.get() : 60;
                if (data.getCrampRestTicks() >= recTicks) {
                    data.resetSwimmingCramps();
                }
            }
        }
    }

    public static void recordMiningAction(Player player) {
        if (!PhysiologyModule.isExertionEnabled()) return;
        if (player.isCreative() || player.isSpectator()) return;
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        long now = player.level().getGameTime();
        if (data.getLastMiningGameTime() == now) {
            return;
        }
        data.setLastMiningGameTime(now);
        data.incrementContinuousMiningTicks();
        data.setTicksSinceLastMining(0);
        data.resetRestTicks();
        int limit = ServerConfig.EXERTION_CONTINUOUS_MINING_TICKS != null ? ServerConfig.EXERTION_CONTINUOUS_MINING_TICKS.get() : 200;
        data.addExertion(100.0f / (float) limit);
        if (data.getContinuousMiningTicks() >= limit) {
            data.setMuscleFailure(true);
        }
    }

    public static void recordAttackAction(Player player) {
        if (!PhysiologyModule.isExertionEnabled()) return;
        if (player.isCreative() || player.isSpectator()) return;
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        data.setTicksSinceLastMining(0);
        data.resetRestTicks();
        data.addExertion(4.0f);
    }

    public static void recordJumpAction(Player player) {
        if (!PhysiologyModule.isExertionEnabled()) return;
        if (player.isCreative() || player.isSpectator()) return;
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        if (data.isMuscleFailure() || (100.0f - data.getExertionScore()) < 10.0f) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, Math.min(0.0, motion.y), motion.z);
            player.hurtMarked = true;
            return;
        }
        long now = player.level().getGameTime();
        if (data.getLastJumpGameTime() == now) {
            return;
        }
        data.setLastJumpGameTime(now);
        data.setTicksSinceLastMining(0);
        data.resetRestTicks();
        if (data.getTicksSinceLastJump() < 40) {
            data.incrementConsecutiveJumps();
        } else {
            data.setConsecutiveJumps(1);
        }
        data.setTicksSinceLastJump(0);

        float jumpCost = 10.0f;
        if (data.getConsecutiveJumps() >= 3) {
            jumpCost += Math.min(6.0f, 1.0f * (data.getConsecutiveJumps() - 2));
        }
        data.addExertion(jumpCost);
        if (data.getExertionScore() >= 100.0f || data.getConsecutiveJumps() >= 8) {
            data.setMuscleFailure(true);
        }
    }

    public static float modifyBreakSpeed(Player player, float originalSpeed) {
        if (!PhysiologyModule.isExertionEnabled()) return originalSpeed;
        if (PhysiologyModule.isMuscleFailure(player.getUUID())) {
            double penalty = ServerConfig.EXERTION_MINING_SPEED_PENALTY != null ? ServerConfig.EXERTION_MINING_SPEED_PENALTY.get() : 0.50;
            return (float) (originalSpeed * (1.0 - penalty));
        }
        return originalSpeed;
    }

    public static void removeExertionModifiers(Player player) {
        AttributeInstance attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttr != null && attackSpeedAttr.hasModifier(EXERTION_ATTACK_SPEED_MODIFIER_ID)) {
            attackSpeedAttr.removeModifier(EXERTION_ATTACK_SPEED_MODIFIER_ID);
        }
        AttributeInstance jumpStrengthAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpStrengthAttr != null && jumpStrengthAttr.hasModifier(EXERTION_JUMP_STRENGTH_MODIFIER_ID)) {
            jumpStrengthAttr.removeModifier(EXERTION_JUMP_STRENGTH_MODIFIER_ID);
        }
    }
}
