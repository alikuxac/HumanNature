package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.init.ModItems;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhysiologyEventHandler {

    public static final TagKey<Item> CALCIUM_FOODS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "calcium_foods"));

    public record FoodSnapshot(int foodLevel, float saturationLevel) {}
    private static final Map<UUID, FoodSnapshot> FOOD_SNAPSHOTS = new ConcurrentHashMap<>();

    public static void onItemUseStart(Player player, ItemStack stack) {
        if (player.isCreative() || player.isSpectator()) return;
        if (stack.has(DataComponents.FOOD)) {
            FOOD_SNAPSHOTS.put(player.getUUID(), new FoodSnapshot(player.getFoodData().getFoodLevel(), player.getFoodData().getSaturationLevel()));
        }
    }

    public static void onItemUseStop(Player player) {
        FOOD_SNAPSHOTS.remove(player.getUUID());
    }

    public static final ResourceLocation EXERTION_ATTACK_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "exertion_attack_speed");
    public static final ResourceLocation EXERTION_JUMP_STRENGTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "exertion_jump_strength");
    public static final ResourceLocation ADRENALINE_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "adrenaline_speed_surge");
    public static final ResourceLocation OVEREATING_SLOW_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "overeating_slowness");

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

    private static final AttributeModifier ADRENALINE_SPEED_MODIFIER = new AttributeModifier(
            ADRENALINE_SPEED_MODIFIER_ID,
            0.35,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    private static final AttributeModifier OVEREATING_SLOW_MODIFIER = new AttributeModifier(
            OVEREATING_SLOW_MODIFIER_ID,
            -0.15,
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

        if (PhysiologyModule.isDigestionEnabled()) {
            handleDigestionTick(player, data);
        }

        if (PhysiologyModule.isOvereatingEnabled()) {
            handleOvereatingTick(player, data);
        }

        if (PhysiologyModule.isIndigestionEnabled()) {
            handleIndigestionTick(player, data);
        }

        if (PhysiologyModule.isClaustrophobiaEnabled()) {
            handleClaustrophobiaTick(player, data);
        }

        if (PhysiologyModule.isAdrenalineEnabled()) {
            handleAdrenalineTick(player, data);
        }

        if (data.hasCalciumBuff()) {
            data.decrementCalciumAbsorptionTicks();
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
        if (player.isCreative() || player.isSpectator()) return;
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());

        if (PhysiologyModule.isToxinBuildupEnabled() && data.getToxinBuildup() > 0.0f) {
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

        if (PhysiologyModule.isCalciumHealingEnabled()) {
            if (stack.is(CALCIUM_FOODS) || stack.is(ModItems.BONE_BROTH.get()) || stack.is(Items.MILK_BUCKET)) {
                data.setCalciumAbsorptionTicks(3600); // 3 minutes
                if (stack.is(ModItems.BONE_BROTH.get())) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.8F);
                } else {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4F, 1.4F);
                }
            }
        }

        if (PhysiologyModule.isIndigestionEnabled()) {
            if (stack.is(Items.CHICKEN) || stack.is(Items.ROTTEN_FLESH)) {
                data.addIndigestion(stack.is(Items.ROTTEN_FLESH) ? 45.0f : 40.0f);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.6F, 0.5F);
            } else if (stack.is(Items.BEEF) || stack.is(Items.PORKCHOP) || stack.is(Items.MUTTON) || stack.is(Items.RABBIT) || stack.is(Items.POISONOUS_POTATO)) {
                data.addIndigestion(25.0f);
            } else if (stack.is(Items.MILK_BUCKET)) {
                data.resetIndigestion();
            } else if (stack.is(Items.HONEY_BOTTLE)) {
                data.decayIndigestion(30.0f);
            }
        }

        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            int nutrition = food.nutrition();
            float saturation = food.saturation();
            FoodSnapshot snapshot = FOOD_SNAPSHOTS.remove(player.getUUID());
            int initialFood = snapshot != null ? snapshot.foodLevel() : Math.max(0, player.getFoodData().getFoodLevel() - nutrition);
            float initialSat = snapshot != null ? snapshot.saturationLevel() : Math.max(0.0f, player.getFoodData().getSaturationLevel() - saturation);
            boolean wasAlreadyFull = initialFood >= 20;

            if (PhysiologyModule.isDigestionEnabled() && nutrition > 0) {
                int immediateNutrition = Math.max(1, (nutrition + 1) / 2);
                float immediateSaturation = saturation * 0.30f;
                int bufferedNutrition = nutrition - immediateNutrition;
                float bufferedSaturation = Math.max(0.0f, saturation - immediateSaturation);

                int newFood = Math.min(20, initialFood + immediateNutrition);
                float newSat = Math.min((float) newFood, initialSat + immediateSaturation);
                player.getFoodData().setFoodLevel(newFood);
                player.getFoodData().setSaturation(newSat);

                if (bufferedNutrition > 0 || bufferedSaturation > 0.0f) {
                    data.addDigestionBuffer(bufferedNutrition, bufferedSaturation);
                }
            }

            if (PhysiologyModule.isOvereatingEnabled()) {
                if (wasAlreadyFull) {
                    data.incrementRecentEatsWhenFull();
                    data.setLastFullEatGameTime(player.level().getGameTime());

                    if (data.getRecentEatsWhenFull() == 1) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.9F, 1.4F);
                        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                        if (speedAttr != null && !speedAttr.hasModifier(OVEREATING_SLOW_MODIFIER_ID)) {
                            speedAttr.addTransientModifier(OVEREATING_SLOW_MODIFIER);
                        }
                    } else if (data.getRecentEatsWhenFull() == 2) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.1F, 1.0F);
                        player.displayClientMessage(Component.translatable("message.humannature.stomach_full"), true);
                    } else if (data.getRecentEatsWhenFull() >= 3) {
                        player.getFoodData().setFoodLevel(Math.max(10, player.getFoodData().getFoodLevel() - 5));
                        player.getFoodData().setSaturation(0.0f);
                        data.resetDigestion();
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0, false, true, true));

                        player.drop(new ItemStack(Items.ROTTEN_FLESH), true);

                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0F, 0.6F);
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.8F, 0.8F);

                        data.resetRecentEatsWhenFull();
                        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                        if (speedAttr != null && speedAttr.hasModifier(OVEREATING_SLOW_MODIFIER_ID)) {
                            speedAttr.removeModifier(OVEREATING_SLOW_MODIFIER_ID);
                        }
                    }
                } else {
                    data.resetRecentEatsWhenFull();
                    AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speedAttr != null && speedAttr.hasModifier(OVEREATING_SLOW_MODIFIER_ID)) {
                        speedAttr.removeModifier(OVEREATING_SLOW_MODIFIER_ID);
                    }
                }
            }
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
        FOOD_SNAPSHOTS.remove(player.getUUID());
        PhysiologyModule.reset(player.getUUID());
        removeExertionModifiers(player);
    }

    public static void onPlayerRespawn(Player player) {
        FOOD_SNAPSHOTS.remove(player.getUUID());
        PhysiologyModule.reset(player.getUUID());
        removeExertionModifiers(player);
    }

    public static void onPlayerLoggedOut(Player player) {
        FOOD_SNAPSHOTS.remove(player.getUUID());
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

    private static void handleDigestionTick(Player player, PlayerPhysiologyData data) {
        if (data.getPendingNutrition() > 0.0f || data.getPendingSaturation() > 0.0f) {
            data.incrementDigestionTickCounter();
            int interval = ServerConfig.DIGESTION_INTERVAL_TICKS != null ? ServerConfig.DIGESTION_INTERVAL_TICKS.get() : 30;
            if (data.getDigestionTickCounter() >= interval) {
                data.resetDigestionTickCounter();

                if (data.getPendingNutrition() > 0.0f && player.getFoodData().needsFood()) {
                    int curFood = player.getFoodData().getFoodLevel();
                    player.getFoodData().setFoodLevel(Math.min(20, curFood + 1));
                    data.consumeDigestionStep(1.0f, 0.0f);
                }

                if (data.getPendingSaturation() > 0.0f) {
                    float curSat = player.getFoodData().getSaturationLevel();
                    float maxSat = (float) player.getFoodData().getFoodLevel();
                    if (curSat < maxSat) {
                        float satStep = Math.min(data.getPendingSaturation(), Math.min(1.5f, maxSat - curSat));
                        player.getFoodData().setSaturation(curSat + satStep);
                        data.consumeDigestionStep(0.0f, satStep);
                    }
                }
            }
        }
    }

    private static void handleOvereatingTick(Player player, PlayerPhysiologyData data) {
        if (data.getRecentEatsWhenFull() > 0 && player.level().getGameTime() - data.getLastFullEatGameTime() > 300) {
            data.resetRecentEatsWhenFull();
            AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null && speedAttr.hasModifier(OVEREATING_SLOW_MODIFIER_ID)) {
                speedAttr.removeModifier(OVEREATING_SLOW_MODIFIER_ID);
            }
        }
    }

    private static void handleIndigestionTick(Player player, PlayerPhysiologyData data) {
        if (data.getIndigestionBuildup() > 0.0f) {
            data.incrementIndigestionTickCounter();
            int counter = data.getIndigestionTickCounter();

            if (counter % 40 == 0) {
                data.decayIndigestion(1.0f);
            }

            if (data.getIndigestionBuildup() >= 50.0f) {
                player.getFoodData().addExhaustion(0.025f);
                if (counter % 80 == 0) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.4F, 0.5F);
                }
            }

            if (counter >= 2400) {
                data.resetIndigestionTickCounter();
            }
        } else {
            data.resetIndigestionTickCounter();
        }
    }

    private static void handleClaustrophobiaTick(Player player, PlayerPhysiologyData data) {
        boolean isDeepUnderground = player.getY() < 0;
        int light = player.level().getMaxLocalRawBrightness(player.blockPosition());
        boolean isCalmed = isCalmedByEnvironment(player);

        if (isDeepUnderground && light == 0 && !isCalmed) {
            data.incrementClaustrophobiaTicks();
            int threshold = ServerConfig.PANIC_THRESHOLD_TICKS != null ? ServerConfig.PANIC_THRESHOLD_TICKS.get() : 100;
            if (data.getClaustrophobiaTicks() >= threshold) {
                data.setPanicking(true);
                if (data.getPanicSoundCooldown() > 0) {
                    data.decrementPanicSoundCooldown();
                } else {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.75F, 1.2F);
                    data.setPanicSoundCooldown(25);
                }
            }
        } else {
            if (data.isPanicking()) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 0.7F, 1.0F);
            }
            data.resetPanic();
        }
    }

    private static boolean isLightEmittingItem(ItemStack stack) {
        return stack.is(Items.TORCH) || stack.is(Items.SOUL_TORCH)
                || stack.is(Items.LANTERN) || stack.is(Items.SOUL_LANTERN)
                || stack.is(Items.CAMPFIRE) || stack.is(Items.SOUL_CAMPFIRE);
    }

    public static boolean isCalmedByEnvironment(Player player) {
        if (player.level().getMaxLocalRawBrightness(player.blockPosition()) > 0) {
            return true;
        }
        if (isLightEmittingItem(player.getMainHandItem()) || isLightEmittingItem(player.getOffhandItem())) {
            return true;
        }
        BlockPos playerPos = player.blockPosition();
        Level level = player.level();
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-8, -3, -8), playerPos.offset(8, 3, 8))) {
            BlockState state = level.getBlockState(pos);
            if ((state.is(BlockTags.CAMPFIRES) && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT))
                    || (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT))) {
                return true;
            }
            if (state.is(Blocks.JUKEBOX) && state.hasProperty(BlockStateProperties.HAS_RECORD) && state.getValue(BlockStateProperties.HAS_RECORD)) {
                return true;
            }
        }
        AABB petSearchBox = player.getBoundingBox().inflate(10.0);
        List<TamableAnimal> pets = level.getEntitiesOfClass(TamableAnimal.class, petSearchBox, pet -> pet.isTame() && pet.isOwnedBy(player));
        return !pets.isEmpty();
    }

    private static void handleAdrenalineTick(Player player, PlayerPhysiologyData data) {
        data.decrementAdrenalineCooldownTicks();

        if (data.isAdrenalineActive()) {
            data.decrementAdrenalineTicks();
            AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null && !speedAttr.hasModifier(ADRENALINE_SPEED_MODIFIER_ID)) {
                speedAttr.addTransientModifier(ADRENALINE_SPEED_MODIFIER);
            }

            if (data.getAdrenalineTicks() <= 0) {
                data.setAdrenalineActive(false);
                if (speedAttr != null && speedAttr.hasModifier(ADRENALINE_SPEED_MODIFIER_ID)) {
                    speedAttr.removeModifier(ADRENALINE_SPEED_MODIFIER_ID);
                }

                InjuryEventHandler.updatePlayerAttributesAndPose(player);

                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 25, 0, false, false, true));

                data.setExertionScore(100.0f);
                data.setMuscleFailure(true);
                player.setSprinting(false);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 0.7F);

                data.setAdrenalineCooldownTicks(1200);
            }
        }
    }

    public static void triggerAdrenalineSurge(Player player) {
        if (!PhysiologyModule.isAdrenalineEnabled()) return;
        if (player.isCreative() || player.isSpectator()) return;
        PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        if (data.getAdrenalineCooldownTicks() <= 0 && !data.isAdrenalineActive()) {
            int duration = ServerConfig.ADRENALINE_SURGE_DURATION_TICKS != null ? ServerConfig.ADRENALINE_SURGE_DURATION_TICKS.get() : 80;
            data.triggerAdrenaline(duration);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.8F, 1.6F);
        }
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
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            if (speedAttr.hasModifier(ADRENALINE_SPEED_MODIFIER_ID)) {
                speedAttr.removeModifier(ADRENALINE_SPEED_MODIFIER_ID);
            }
            if (speedAttr.hasModifier(OVEREATING_SLOW_MODIFIER_ID)) {
                speedAttr.removeModifier(OVEREATING_SLOW_MODIFIER_ID);
            }
        }
    }
}
