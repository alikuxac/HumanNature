package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.item.SplintTier;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InjuryEventHandler {
    private static final Random RANDOM = new Random();
    private static final Set<UUID> PENDING_TRAUMA_TOTEM = ConcurrentHashMap.newKeySet();
    public record FlightState(double speed, float pitch, long tick) {}
    private static final Map<UUID, Long> LAST_FIREWORK_USE_TICK = new ConcurrentHashMap<>();
    private static final Map<UUID, FlightState> RECENT_FLIGHT_STATE = new ConcurrentHashMap<>();

    public static final ResourceLocation FRACTURE_SPEED_MODIFIER_LEG1_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_speed_leg1");
    public static final ResourceLocation FRACTURE_SPEED_MODIFIER_LEG2_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_speed_leg2");

    public static final ResourceLocation FRACTURE_JUMP_MODIFIER_LEG1_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_jump_leg1");
    public static final ResourceLocation FRACTURE_JUMP_MODIFIER_LEG2_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_jump_leg2");

    public static final ResourceLocation FRACTURE_MAX_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_max_health");
    public static final ResourceLocation FRACTURE_ATTACK_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_attack_speed");

    public static final ResourceLocation SPLINT_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "splint_speed_reduction");

    private static final AttributeModifier FRACTURE_SPEED_MODIFIER_LEG1 = new AttributeModifier(
            FRACTURE_SPEED_MODIFIER_LEG1_ID,
            -0.45,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static final AttributeModifier FRACTURE_SPEED_MODIFIER_LEG2 = new AttributeModifier(
            FRACTURE_SPEED_MODIFIER_LEG2_ID,
            -0.85,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static final AttributeModifier FRACTURE_JUMP_MODIFIER_LEG2 = new AttributeModifier(
            FRACTURE_JUMP_MODIFIER_LEG2_ID,
            -1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static final AttributeModifier FRACTURE_MAX_HEALTH_MODIFIER = new AttributeModifier(
            FRACTURE_MAX_HEALTH_MODIFIER_ID,
            -6.0,
            AttributeModifier.Operation.ADD_VALUE
    );

    private static final AttributeModifier FRACTURE_ATTACK_SPEED_MODIFIER = new AttributeModifier(
            FRACTURE_ATTACK_SPEED_MODIFIER_ID,
            -0.50,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static AttributeModifier SPLINT_SPEED_MODIFIER = new AttributeModifier(
            SPLINT_SPEED_MODIFIER_ID,
            -0.10,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    public static void updateSplintModifier() {
        double reduction = ServerConfig.SPLINT_SPEED_REDUCTION.get();
        SPLINT_SPEED_MODIFIER = new AttributeModifier(
                SPLINT_SPEED_MODIFIER_ID,
                -reduction,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    public static int getFractureCount(Player player) {
        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        return data.getTotalInjuries();
    }

    public static InjuryTier getFractureTier(Player player) {
        return InjuryModule.getFractureTier(player.getUUID());
    }

    public static void applyBoneFracture(Player player) {
        applyBoneFractureTier(player, InjuryTier.TIER_1_SINGLE_LEG);
    }

    public static void applyBoneFractureTier(Player player, InjuryTier targetTier) {
        player.setSprinting(false);
        InjuryModule.setFractureTier(player.getUUID(), targetTier);
        updatePlayerAttributesAndPose(player);

        float pitch = (targetTier == InjuryTier.TIER_1_SINGLE_LEG) ? 0.8F : 0.6F;
        playBoneCrackSound(player, pitch);
    }

    private static void playBoneCrackSound(Player player, float pitch) {
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SKELETON_HURT,
                SoundSource.PLAYERS,
                1.0F,
                pitch
        );
    }

    public static void updatePlayerAttributesAndPose(Player player) {
        if (!ServerConfig.ENABLE_INJURY.get()) return;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);

        if (speedAttr == null) return;

        if (!data.hasAnyInjury()) {
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID);
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID);
            speedAttr.removeModifier(SPLINT_SPEED_MODIFIER_ID);
            if (jumpAttr != null) {
                jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG1_ID);
                jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID);
            }
            if (maxHealthAttr != null) {
                maxHealthAttr.removeModifier(FRACTURE_MAX_HEALTH_MODIFIER_ID);
            }
            if (attackSpeedAttr != null) {
                attackSpeedAttr.removeModifier(FRACTURE_ATTACK_SPEED_MODIFIER_ID);
            }

            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.CONFUSION);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            return;
        }

        byte head = data.getSeverity(LimbType.HEAD);
        if (head == 1) {
            if (player.level().getGameTime() % 100 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false, false));
            }
        } else if (head >= 2) {
            if (player.level().getGameTime() % 80 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false, true));
            }
        }

        byte torso = data.getSeverity(LimbType.TORSO);
        if (torso == 1) {
            if (player.walkDist - player.walkDistO > 0.001F) {
                player.causeFoodExhaustion(0.02F);
            }
        } else if (torso >= 2) {
            if (maxHealthAttr != null && !maxHealthAttr.hasModifier(FRACTURE_MAX_HEALTH_MODIFIER_ID)) {
                maxHealthAttr.addTransientModifier(FRACTURE_MAX_HEALTH_MODIFIER);
                if (player.getHealth() > maxHealthAttr.getValue()) {
                    player.setHealth((float) Math.max(1.0, maxHealthAttr.getValue()));
                }
            }
            if (player.walkDist - player.walkDistO > 0.001F) {
                player.causeFoodExhaustion(0.04F);
            }
        }

        byte mainArm = data.getSeverity(LimbType.MAIN_ARM);
        if (mainArm > 0) {
            if (attackSpeedAttr != null && !attackSpeedAttr.hasModifier(FRACTURE_ATTACK_SPEED_MODIFIER_ID)) {
                attackSpeedAttr.addTransientModifier(FRACTURE_ATTACK_SPEED_MODIFIER);
            }
        } else if (attackSpeedAttr != null) {
            attackSpeedAttr.removeModifier(FRACTURE_ATTACK_SPEED_MODIFIER_ID);
        }

        byte leftLeg = data.getSeverity(LimbType.LEFT_LEG);
        byte rightLeg = data.getSeverity(LimbType.RIGHT_LEG);
        boolean isBothLegsBroken = (leftLeg >= 2 && rightLeg >= 2) || (leftLeg >= 1 && rightLeg >= 2) || (leftLeg >= 2 && rightLeg >= 1);
        boolean isAnyLegBroken = leftLeg > 0 || rightLeg > 0;

        if (isAnyLegBroken) {
            player.setSprinting(false);
        }

        if (isBothLegsBroken) {
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID);
            if (!speedAttr.hasModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID)) {
                speedAttr.addTransientModifier(FRACTURE_SPEED_MODIFIER_LEG2);
            }
            if (jumpAttr != null && !jumpAttr.hasModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID)) {
                jumpAttr.addTransientModifier(FRACTURE_JUMP_MODIFIER_LEG2);
            }
        } else if (isAnyLegBroken) {
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID);
            if (jumpAttr != null) {
                jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID);
            }
            if (!speedAttr.hasModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID)) {
                speedAttr.addTransientModifier(FRACTURE_SPEED_MODIFIER_LEG1);
            }
        } else {
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID);
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID);
            if (jumpAttr != null) {
                jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID);
            }
        }
    }

    public static void reduceFractureTimer(Player player, int amount) {
        InjuryModule.reduceFractureTimer(player.getUUID(), amount);
        if (!InjuryModule.isFractureActive(player.getUUID())) {
            clearFractureTimer(player);
        }
    }

    public static int getRemainingFractureTime(Player player) {
        return InjuryModule.getRemainingFractureTime(player.getUUID());
    }

    public static boolean isFractureActive(Player player) {
        return InjuryModule.isFractureActive(player.getUUID());
    }

    public static void clearFractureTimer(Player player) {
        PENDING_TRAUMA_TOTEM.remove(player.getUUID());
        LAST_FIREWORK_USE_TICK.remove(player.getUUID());
        RECENT_FLIGHT_STATE.remove(player.getUUID());
        InjuryModule.clearFractureTimer(player.getUUID());

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);

        if (speedAttr != null) {
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID);
            speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID);
            speedAttr.removeModifier(SPLINT_SPEED_MODIFIER_ID);
        }

        if (jumpAttr != null) {
            jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG1_ID);
            jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID);
        }

        if (maxHealthAttr != null) {
            maxHealthAttr.removeModifier(FRACTURE_MAX_HEALTH_MODIFIER_ID);
        }

        if (attackSpeedAttr != null) {
            attackSpeedAttr.removeModifier(FRACTURE_ATTACK_SPEED_MODIFIER_ID);
        }

        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    public static void onPlayerDeath(Player player) {
        boolean keepInventory = player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

        if (!keepInventory) {
            clearFractureTimer(player);

            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.CONFUSION);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }

    public static void onPlayerRespawn(Player player) {
        boolean keepInventory = player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

        if (!keepInventory) {
            clearFractureTimer(player);

            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.CONFUSION);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        } else {
            clearFractureTimer(player);

            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));

            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
            boolean pickLeft = RANDOM.nextBoolean();
            LimbType targetLeg = pickLeft ? LimbType.LEFT_LEG : LimbType.RIGHT_LEG;
            data.setSeverity(targetLeg, 1);
            InjuryModule.setInjuryData(player.getUUID(), data);

            InjuryModule.reduceFractureTimer(player.getUUID(), InjuryModule.getRemainingFractureTime(player.getUUID()) - 600);

            updatePlayerAttributesAndPose(player);
        }
    }

    public static void onPlayerWakeUp(Player player) {
        if (!ServerConfig.ENABLE_INJURY.get()) return;
        if (player.level().isClientSide()) return;

        int foodLevel = player.getFoodData().getFoodLevel();
        if (foodLevel >= 16) {
            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
            if (data.hasAnyInjury()) {
                boolean downgraded = data.downgradeWorstInjury();
                if (downgraded) {
                    InjuryModule.setInjuryData(player.getUUID(), data);
                    updatePlayerAttributesAndPose(player);

                    player.getFoodData().setFoodLevel(Math.max(0, foodLevel - 4));

                    player.level().playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.PLAYER_LEVELUP,
                            SoundSource.PLAYERS,
                            0.8F,
                            1.2F
                    );
                }
            }
        }
    }

    public static void handleItemConsumption(Player player, ItemStack stack) {
        if (!isFractureActive(player)) return;

        double reductionPercent = 0.0;

        if (stack.is(Items.MILK_BUCKET)) {
            reductionPercent = 0.50;
        } else if (stack.is(Items.RABBIT_STEW) ||
                 stack.is(Items.MUSHROOM_STEW) ||
                 stack.is(Items.BEETROOT_SOUP) ||
                 stack.is(Items.SUSPICIOUS_STEW)) {
            reductionPercent = 0.30;
        } else if (stack.is(Items.GOLDEN_APPLE) ||
                 stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            reductionPercent = 0.75;
        } else if (stack.getItem() instanceof PotionItem) {
            reductionPercent = 0.40;
        }

        if (reductionPercent > 0.0) {
            int currentTime = getRemainingFractureTime(player);
            int reducedTime = (int) (currentTime * reductionPercent);
            reduceFractureTimer(player, reducedTime);

            player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BONE_MEAL_USE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            );
        }
    }

    public static void onLivingFall(LivingEntity entity, float distance, float damageMultiplier) {
        if (!(entity instanceof Player player)) return;
        if (!ServerConfig.ENABLE_INJURY.get()) return;
        if (player.isCreative() || player.isSpectator()) return;

        float safeThreshold = 5.0F;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int featherFallingLevel = getEnchantmentLevel(boots, Enchantments.FEATHER_FALLING);
        safeThreshold += (featherFallingLevel * 2.0F);

        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (isDiamondOrNetheriteArmor(leggings)) {
            safeThreshold += 1.5F;
        }

        float excessDistance = distance - safeThreshold;
        if (excessDistance > 0) {
            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
            if (excessDistance >= 5.0F) {
                data.setSeverity(LimbType.LEFT_LEG, 2);
                data.setSeverity(LimbType.RIGHT_LEG, 2);
                InjuryModule.setInjuryData(player.getUUID(), data);
                updatePlayerAttributesAndPose(player);
                playBoneCrackSound(player, 0.6F);
            } else {
                boolean pickLeft = RANDOM.nextBoolean();
                LimbType targetLeg = pickLeft ? LimbType.LEFT_LEG : LimbType.RIGHT_LEG;
                int currentSev = data.getSeverity(targetLeg);
                data.setSeverity(targetLeg, Math.min(2, currentSev + 1));
                InjuryModule.setInjuryData(player.getUUID(), data);
                updatePlayerAttributesAndPose(player);
                playBoneCrackSound(player, 0.8F);
            }
        }

        if (player.getHealth() <= 1.0F && isFractureActive(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, true));
            player.drop(player.getItemBySlot(EquipmentSlot.MAINHAND).copy(), true);
            player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.WARDEN_HEARTBEAT,
                    SoundSource.PLAYERS,
                    1.2F,
                    0.8F
            );
        }
    }

    public static void onLivingDamage(LivingEntity entity, DamageSource damageSource, float amount) {
        if (!(entity instanceof Player player)) return;
        if (!ServerConfig.ENABLE_INJURY.get()) return;
        if (player.isCreative() || player.isSpectator()) return;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        boolean stateChanged = false;

        boolean isAnvilOrFallingBlock = damageSource.is(DamageTypes.FALLING_ANVIL) ||
                damageSource.is(DamageTypes.FALLING_BLOCK);
        boolean isFlyIntoWall = damageSource.is(DamageTypes.FLY_INTO_WALL);

        if (isAnvilOrFallingBlock || isFlyIntoWall) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            int blastProtLevel = getEnchantmentLevel(helmet, Enchantments.BLAST_PROTECTION);

            double concussionChance = 1.0;
            if (blastProtLevel > 0) {
                concussionChance = Math.max(0.2, 1.0 - (0.2 * blastProtLevel));
            }

            if (RANDOM.nextDouble() < concussionChance) {
                int sev = (amount > 8.0F || isAnvilOrFallingBlock) ? 2 : 1;
                data.setSeverity(LimbType.HEAD, Math.max(data.getSeverity(LimbType.HEAD), (byte) sev));
                stateChanged = true;
                playBoneCrackSound(player, 0.7F);
            }
        }

        if (isFlyIntoWall && amount >= 6.0F) {
            LimbType crashArm = RANDOM.nextBoolean() ? LimbType.MAIN_ARM : LimbType.OFF_ARM;
            int armSev = amount >= 12.0F ? 2 : 1;
            data.setSeverity(crashArm, Math.max(data.getSeverity(crashArm), (byte) armSev));
            stateChanged = true;
        }

        FlightState flightState = getRecentFlightState(player);
        boolean isRecentFlight = flightState != null;
        boolean isKineticDiveFall = damageSource.is(DamageTypes.FALL) && isRecentFlight;
        boolean isKineticCrash = isFlyIntoWall || isKineticDiveFall;

        if (isKineticCrash && ServerConfig.ENABLE_KINETIC_CRASH.get()) {
            double impactSpeed = isRecentFlight ? flightState.speed() : (amount / 10.0 + 0.3);
            float impactPitch = isRecentFlight ? flightState.pitch() : 0.0F;

            boolean isExtremeImpact = amount >= 12.0F ||
                    (impactSpeed >= 1.4 && impactPitch >= 35.0F) ||
                    impactSpeed >= 1.8;
            boolean isModerateImpact = amount >= 4.0F || impactSpeed >= 0.7;

            if (isExtremeImpact) {
                data.setSeverity(LimbType.LEFT_LEG, 2);
                data.setSeverity(LimbType.RIGHT_LEG, 2);
                stateChanged = true;
                playBoneCrackSound(player, 0.6F);

                dropHeldFireworks(player);
            } else if (isModerateImpact) {
                byte leftLeg = data.getSeverity(LimbType.LEFT_LEG);
                byte rightLeg = data.getSeverity(LimbType.RIGHT_LEG);
                if (leftLeg == 0 && rightLeg == 0) {
                    LimbType targetLeg = RANDOM.nextBoolean() ? LimbType.LEFT_LEG : LimbType.RIGHT_LEG;
                    data.setSeverity(targetLeg, 1);
                    stateChanged = true;
                    playBoneCrackSound(player, 0.8F);
                }
            }
        }

        if (amount >= 12.0F) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            boolean isDiamondOrNetheriteChest = isDiamondOrNetheriteArmor(chestplate);

            int currentSev = data.getSeverity(LimbType.TORSO);
            if (amount >= 14.0F) {
                data.setSeverity(LimbType.TORSO, 2);
                stateChanged = true;
                playBoneCrackSound(player, 0.6F);
            } else if (!isDiamondOrNetheriteChest && currentSev == 0) {
                data.setSeverity(LimbType.TORSO, 1);
                stateChanged = true;
                playBoneCrackSound(player, 0.8F);
            }
        }

        if (stateChanged) {
            InjuryModule.setInjuryData(player.getUUID(), data);
            updatePlayerAttributesAndPose(player);
        }
    }

    public static void recordFlightTick(Player player) {
        if (player.level().isClientSide()) return;
        long currentTick = player.level().getGameTime();
        if (player.isFallFlying()) {
            double speed = player.getDeltaMovement().length();
            float pitch = player.getXRot();
            RECENT_FLIGHT_STATE.put(player.getUUID(), new FlightState(speed, pitch, currentTick));
        }
    }

    public static FlightState getRecentFlightState(Player player) {
        FlightState state = RECENT_FLIGHT_STATE.get(player.getUUID());
        if (state != null) {
            long diff = player.level().getGameTime() - state.tick();
            if (diff >= 0 && diff <= 10) {
                return state;
            }
        }
        return null;
    }

    public static void onPlayerRightClickItem(Player player, InteractionHand hand, ItemStack stack) {
        if (player.level().isClientSide()) return;
        if (!ServerConfig.ENABLE_INJURY.get() || !ServerConfig.ENABLE_GFORCE_BLACKOUT.get()) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (player.isFallFlying() && stack.is(Items.FIREWORK_ROCKET)) {
            long currentTick = player.level().getGameTime();
            Long lastTick = LAST_FIREWORK_USE_TICK.get(player.getUUID());
            if (lastTick != null && (currentTick - lastTick) <= 40) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20, 0, false, false, true));
                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ELYTRA_FLYING,
                        SoundSource.PLAYERS,
                        1.6F,
                        1.8F
                );
                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.WIND_CHARGE_BURST,
                        SoundSource.PLAYERS,
                        1.2F,
                        0.6F
                );
            }
            LAST_FIREWORK_USE_TICK.put(player.getUUID(), currentTick);
        }
    }

    private static void dropHeldFireworks(Player player) {
        boolean dropped = false;
        ItemStack mainHand = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (mainHand.is(Items.FIREWORK_ROCKET)) {
            player.drop(mainHand.copy(), true);
            player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            dropped = true;
        }
        ItemStack offHand = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (offHand.is(Items.FIREWORK_ROCKET)) {
            player.drop(offHand.copy(), true);
            player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            dropped = true;
        }
        if (dropped) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ITEM_BREAK,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.9F
            );
        }
    }

    public static void onShieldBlock(Player player, DamageSource damageSource, float blockedDamage) {
        onShieldBlock(player, damageSource, blockedDamage, player.getUsedItemHand());
    }

    public static void onShieldBlock(Player player, DamageSource damageSource, float blockedDamage, InteractionHand hand) {
        if (!ServerConfig.ENABLE_INJURY.get()) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (blockedDamage >= 10.0F) {
            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
            LimbType arm = (hand == InteractionHand.MAIN_HAND) ? LimbType.MAIN_ARM : LimbType.OFF_ARM;
            int currentSev = data.getSeverity(arm);
            int newSev = blockedDamage >= 14.0F ? 2 : Math.min(2, currentSev + 1);
            if (newSev > currentSev) {
                data.setSeverity(arm, newSev);
                InjuryModule.setInjuryData(player.getUUID(), data);
                updatePlayerAttributesAndPose(player);
                playBoneCrackSound(player, newSev >= 2 ? 0.6F : 0.8F);
            }
        }
    }

    public static void onShieldDisabled(Player player) {
        onShieldDisabled(player, player.getUsedItemHand());
    }

    public static void onShieldDisabled(Player player, InteractionHand hand) {
        if (!ServerConfig.ENABLE_INJURY.get()) return;
        if (player.isCreative() || player.isSpectator()) return;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        LimbType arm = (hand == InteractionHand.MAIN_HAND) ? LimbType.MAIN_ARM : LimbType.OFF_ARM;
        int currentSev = data.getSeverity(arm);
        int newSev = Math.min(2, currentSev + 1);
        if (newSev > currentSev) {
            data.setSeverity(arm, newSev);
            InjuryModule.setInjuryData(player.getUUID(), data);
            updatePlayerAttributesAndPose(player);
            playBoneCrackSound(player, newSev >= 2 ? 0.6F : 0.8F);
        }
    }

    public static float modifyBreakSpeed(Player player, float originalSpeed) {
        if (!ServerConfig.ENABLE_INJURY.get()) return originalSpeed;
        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        if (data.getSeverity(LimbType.MAIN_ARM) > 0) {
            return originalSpeed * 0.5F;
        }
        return originalSpeed;
    }

    public static void checkMiningExhaustion(Player player, float blockHardness) {
        if (!ServerConfig.ENABLE_INJURY.get()) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (blockHardness >= 10.0F) {
            if (RANDOM.nextDouble() < 0.05) {
                PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
                int currentSev = data.getSeverity(LimbType.MAIN_ARM);
                if (currentSev < 2) {
                    data.setSeverity(LimbType.MAIN_ARM, currentSev + 1);
                    InjuryModule.setInjuryData(player.getUUID(), data);
                    updatePlayerAttributesAndPose(player);
                    playBoneCrackSound(player, 0.8F);
                }
            }
        }
    }

    public static boolean checkSpasmDrop(Player player) {
        if (!ServerConfig.ENABLE_INJURY.get()) return false;
        if (player.isCreative() || player.isSpectator()) return false;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        if (data.getSeverity(LimbType.MAIN_ARM) > 0) {
            if (RANDOM.nextDouble() < 0.15) {
                ItemStack held = player.getMainHandItem();
                if (!held.isEmpty()) {
                    player.drop(held.copy(), true);
                    player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    player.level().playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.PLAYER_ATTACK_WEAK,
                            SoundSource.PLAYERS,
                            1.0F,
                            0.7F
                    );
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isOffhandLocked(Player player) {
        if (!ServerConfig.ENABLE_INJURY.get()) return false;
        if (player.isCreative() || player.isSpectator()) return false;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        return data.getSeverity(LimbType.OFF_ARM) > 0;
    }

    public static boolean isMainArmBroken(Player player) {
        if (!ServerConfig.ENABLE_INJURY.get()) return false;
        if (player.isCreative() || player.isSpectator()) return false;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        return data.getSeverity(LimbType.MAIN_ARM) > 0;
    }

    public static boolean handleWeaponDrawTick(LivingEntity entity, ItemStack stack, int useDuration) {
        if (!(entity instanceof Player player)) return false;
        if (!ServerConfig.ENABLE_INJURY.get()) return false;
        if (player.isCreative() || player.isSpectator()) return false;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        InteractionHand hand = player.getUsedItemHand();
        LimbType arm = (hand == InteractionHand.OFF_HAND) ? LimbType.OFF_ARM : LimbType.MAIN_ARM;
        byte armSev = data.getSeverity(arm);
        if (armSev <= 0) return false;

        boolean isBowOrCrossbow = stack.is(Items.BOW) || stack.is(Items.CROSSBOW);
        if (isBowOrCrossbow) {
            int drawTicks = stack.getUseDuration(entity) - useDuration;
            if (drawTicks >= 12) {
                player.stopUsingItem();
                playBoneCrackSound(player, 0.9F);
                return true;
            }
        }
        return false;
    }

    private static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantmentKey) {
        if (stack.isEmpty()) return 0;
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) return 0;
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (holder.is(enchantmentKey)) {
                return enchantments.getLevel(holder);
            }
        }
        return 0;
    }

    private static boolean isDiamondOrNetheriteArmor(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armorItem)) {
            return false;
        }
        Holder<ArmorMaterial> material = armorItem.getMaterial();
        return material.is(ArmorMaterials.DIAMOND.unwrapKey().orElse(null)) ||
                material.is(ArmorMaterials.NETHERITE.unwrapKey().orElse(null));
    }

    public static boolean handleQuickTreat(Player player, LimbType limb, ItemStack splintStack) {
        if (!ServerConfig.ENABLE_SPLINT.get() || splintStack.isEmpty() || !(splintStack.getItem() instanceof SplintItem splintItem)) {
            return false;
        }

        if (player.getCooldowns().isOnCooldown(splintItem)) {
            return false;
        }

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        byte currentSev = data.getSeverity(limb);
        if (currentSev <= 0) {
            return false;
        }

        int reduction = (splintItem.getTier() == SplintTier.DIAMOND ||
                splintItem.getTier() == SplintTier.NETHERITE) ? 2 : 1;

        int newSev = Math.max(0, currentSev - reduction);
        data.setSeverity(limb, newSev);
        InjuryModule.setInjuryData(player.getUUID(), data);
        updatePlayerAttributesAndPose(player);

        player.getCooldowns().addCooldown(splintItem, SplintItem.CHANNEL_DURATION);

        splintStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        return true;
    }

    public static ItemStack findFirstSplint(Player player) {
        ItemStack cursor = player.containerMenu.getCarried();
        if (SplintItem.isValidSplint(cursor)) return cursor;

        ItemStack main = player.getMainHandItem();
        if (SplintItem.isValidSplint(main)) return main;
        ItemStack off = player.getOffhandItem();
        if (SplintItem.isValidSplint(off)) return off;

        for (ItemStack item : player.getInventory().items) {
            if (SplintItem.isValidSplint(item)) {
                return item;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean shouldCancelNaturalRegen(Player player) {
        return ServerConfig.ENABLE_INJURY.get() && isFractureActive(player);
    }

    public static void onLivingUseTotem(Player player, DamageSource damageSource, ItemStack totem, InteractionHand hand) {
        if (!ServerConfig.ENABLE_INJURY.get() || !ServerConfig.ENABLE_TOTEM_BYPASS.get()) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!damageSource.is(DamageTypes.FALL)) return;

        PENDING_TRAUMA_TOTEM.add(player.getUUID());

        applyBoneFractureTier(player, InjuryTier.TIER_2_DOUBLE_LEG);

        executeKineticInventoryScatter(player, totem, hand);
    }

    public static boolean shouldDenyMobEffect(Player player, MobEffectInstance effectInstance) {
        if (!ServerConfig.ENABLE_INJURY.get() || !ServerConfig.ENABLE_TOTEM_BYPASS.get()) return false;
        if (PENDING_TRAUMA_TOTEM.contains(player.getUUID())) {
            return effectInstance.getEffect().is(MobEffects.REGENERATION) || effectInstance.getEffect().is(MobEffects.ABSORPTION);
        }
        return false;
    }

    public static void onPlayerTick(Player player) {
        if (!ServerConfig.ENABLE_INJURY.get()) return;

        if (PENDING_TRAUMA_TOTEM.remove(player.getUUID())) {
            player.removeEffect(MobEffects.REGENERATION);
            player.removeEffect(MobEffects.ABSORPTION);
            player.setHealth(1.0F);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, true));
        }
    }

    private static void executeKineticInventoryScatter(Player player, ItemStack activeTotem, InteractionHand totemHand) {
        if (totemHand == InteractionHand.OFF_HAND) {
            if (activeTotem.getCount() > 1) {
                ItemStack extra = activeTotem.copy();
                extra.shrink(1);
                scatterItem(player, extra);
            }
            player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

            ItemStack main = player.getMainHandItem();
            if (!main.isEmpty()) {
                scatterItem(player, main.copy());
                player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        } else if (totemHand == InteractionHand.MAIN_HAND) {
            if (activeTotem.getCount() > 1) {
                ItemStack extra = activeTotem.copy();
                extra.shrink(1);
                scatterItem(player, extra);
            }
            player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

            ItemStack off = player.getOffhandItem();
            if (!off.isEmpty()) {
                scatterItem(player, off.copy());
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
        } else {
            ItemStack main = player.getMainHandItem();
            if (!main.isEmpty()) {
                scatterItem(player, main.copy());
                player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            ItemStack off = player.getOffhandItem();
            if (!off.isEmpty()) {
                scatterItem(player, off.copy());
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                scatterItem(player, stack.copy());
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }

        List<Integer> occupiedSlots = new ArrayList<>();
        for (int i = 9; i < 36; i++) {
            if (!player.getInventory().items.get(i).isEmpty()) {
                occupiedSlots.add(i);
            }
        }

        if (!occupiedSlots.isEmpty()) {
            Collections.shuffle(occupiedSlots, RANDOM);
            int dropCount = (int) Math.ceil(occupiedSlots.size() * 0.30);
            for (int k = 0; k < dropCount; k++) {
                int slot = occupiedSlots.get(k);
                ItemStack stack = player.getInventory().items.get(slot);
                if (!stack.isEmpty()) {
                    scatterItem(player, stack.copy());
                    player.getInventory().items.set(slot, ItemStack.EMPTY);
                }
            }
        }

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_BREAK,
                SoundSource.PLAYERS,
                1.0F,
                0.9F
        );
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BUNDLE_DROP_CONTENTS,
                SoundSource.PLAYERS,
                1.2F,
                0.8F
        );
    }

    private static void scatterItem(Player player, ItemStack stack) {
        if (stack.isEmpty() || player.level().isClientSide()) return;

        double angle = RANDOM.nextDouble() * 2.0 * Math.PI;
        double dist = 0.5 + RANDOM.nextDouble() * 3.5;
        double dx = Math.cos(angle) * dist;
        double dz = Math.sin(angle) * dist;

        double spawnX = player.getX() + dx * 0.2;
        double spawnY = player.getY() + 0.3;
        double spawnZ = player.getZ() + dz * 0.2;

        ItemEntity itemEntity = new ItemEntity(player.level(), spawnX, spawnY, spawnZ, stack);
        itemEntity.setDeltaMovement(dx * 0.12, 0.25 + RANDOM.nextDouble() * 0.15, dz * 0.12);
        itemEntity.setPickUpDelay(40);
        player.level().addFreshEntity(itemEntity);
    }
}
