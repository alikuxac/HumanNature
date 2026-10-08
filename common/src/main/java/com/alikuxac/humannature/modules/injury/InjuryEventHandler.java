package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.config.CommonConfig;
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
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Ravager;
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

import java.util.Random;

public class InjuryEventHandler {
    private static final Random RANDOM = new Random();

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
            -0.30,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    private static AttributeModifier SPLINT_SPEED_MODIFIER = new AttributeModifier(
            SPLINT_SPEED_MODIFIER_ID,
            -0.10,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    public static void updateSplintModifier() {
        double reduction = CommonConfig.SPLINT_SPEED_REDUCTION.get();
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
        if (!CommonConfig.ENABLE_INJURY.get()) return;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);

        if (speedAttr == null) return;

        // Clean up existing attribute modifiers to prevent duplicates
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

        if (!data.hasAnyInjury()) {
            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.CONFUSION);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (player.getPose() == Pose.SWIMMING && !player.isSwimming() && !player.isVisuallyCrawling()) {
                player.setPose(Pose.STANDING);
            }
            return;
        }

        player.setSprinting(false);

        // --- 1. HEAD PENALTIES ---
        byte head = data.getSeverity(LimbType.HEAD);
        if (head == 1) {
            // Tinnitus sound / light disorient
            if (player.level().getGameTime() % 100 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false, false));
            }
        } else if (head >= 2) {
            // Flash darkness & disorient
            if (player.level().getGameTime() % 80 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false, true));
            }
        }

        // --- 2. TORSO PENALTIES ---
        byte torso = data.getSeverity(LimbType.TORSO);
        if (torso == 1) {
            // Exhaustion accelerates on moving
            if (player.walkDist - player.walkDistO > 0.001F) {
                player.causeFoodExhaustion(0.02F);
            }
        } else if (torso >= 2) {
            // Max Health capped to 14 HP (7 hearts) (-6 HP from base 20 HP)
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

        // --- 3. ARM PENALTIES ---
        byte mainArm = data.getSeverity(LimbType.MAIN_ARM);
        byte offArm = data.getSeverity(LimbType.OFF_ARM);
        if (mainArm > 0 || offArm > 0) {
            if (attackSpeedAttr != null && !attackSpeedAttr.hasModifier(FRACTURE_ATTACK_SPEED_MODIFIER_ID)) {
                attackSpeedAttr.addTransientModifier(FRACTURE_ATTACK_SPEED_MODIFIER);
            }
        }

        // --- 4. LEG PENALTIES ---
        byte leftLeg = data.getSeverity(LimbType.LEFT_LEG);
        byte rightLeg = data.getSeverity(LimbType.RIGHT_LEG);
        boolean isBothLegsBroken = (leftLeg >= 2 && rightLeg >= 2) || (leftLeg >= 1 && rightLeg >= 2) || (leftLeg >= 2 && rightLeg >= 1);
        boolean isAnyLegBroken = leftLeg > 0 || rightLeg > 0;

        if (isBothLegsBroken) {
            if (!speedAttr.hasModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID)) {
                speedAttr.addTransientModifier(FRACTURE_SPEED_MODIFIER_LEG2);
            }
            if (jumpAttr != null && !jumpAttr.hasModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID)) {
                jumpAttr.addTransientModifier(FRACTURE_JUMP_MODIFIER_LEG2);
            }
            player.setPose(Pose.SWIMMING);
        } else if (isAnyLegBroken) {
            if (!speedAttr.hasModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID)) {
                speedAttr.addTransientModifier(FRACTURE_SPEED_MODIFIER_LEG1);
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
        if (player.getPose() == Pose.SWIMMING && !player.isSwimming() && !player.isVisuallyCrawling()) {
            player.setPose(Pose.STANDING);
        }
    }

    public static void onPlayerDeath(Player player) {
        boolean keepInventory = player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

        if (!keepInventory) {
            // Keep inventory FALSE: Complete reset on death: wipe fracture timers, 6-limb trauma data, and attribute debuffs
            clearFractureTimer(player);

            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.CONFUSION);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }

    public static void onPlayerRespawn(Player player) {
        boolean keepInventory = player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

        if (!keepInventory) {
            // Keep inventory FALSE: Ensure clean state upon respawning
            clearFractureTimer(player);

            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.CONFUSION);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        } else {
            // Keep inventory TRUE: Anti-suicide-spam penalty for 30 seconds (600 ticks)
            // Clear severe debuffs first to set clean penalty
            clearFractureTimer(player);

            // 1. Fatigue effects: Weakness I (30s) + Hunger I (30s)
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));

            // 2. Leg Tier 1 fracture penalty (30s timer)
            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
            boolean pickLeft = RANDOM.nextBoolean();
            LimbType targetLeg = pickLeft ? LimbType.LEFT_LEG : LimbType.RIGHT_LEG;
            data.setSeverity(targetLeg, 1);
            InjuryModule.setInjuryData(player.getUUID(), data);

            // Set fracture timer to exactly 30s (600 ticks)
            InjuryModule.reduceFractureTimer(player.getUUID(), InjuryModule.getRemainingFractureTime(player.getUUID()) - 600);

            updatePlayerAttributesAndPose(player);
        }
    }

    public static void onPlayerWakeUp(Player player) {
        if (!CommonConfig.ENABLE_INJURY.get()) return;
        if (player.level().isClientSide()) return;

        // Sleep recovery requires sufficient nutrition (Food level >= 16, i.e. 8 drumsticks)
        int foodLevel = player.getFoodData().getFoodLevel();
        if (foodLevel >= 16) {
            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
            if (data.hasAnyInjury()) {
                boolean downgraded = data.downgradeWorstInjury();
                if (downgraded) {
                    InjuryModule.setInjuryData(player.getUUID(), data);
                    updatePlayerAttributesAndPose(player);

                    // Consume some hunger as metabolic cost for bone recovery (4 hunger / 2 drumsticks)
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
        if (!CommonConfig.ENABLE_INJURY.get()) return;
        if (player.isCreative() || player.isSpectator()) return;

        // Base safe fall threshold: 5.0 blocks
        float safeThreshold = 5.0F;

        // Check Feather Falling enchant on boots (+2.0 blocks safe threshold per level)
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int featherFallingLevel = getEnchantmentLevel(boots, Enchantments.FEATHER_FALLING);
        safeThreshold += (featherFallingLevel * 2.0F);

        // Check Leggings material (+1.5 blocks for Diamond or Netherite leggings)
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        if (isDiamondOrNetheriteArmor(leggings)) {
            safeThreshold += 1.5F;
        }

        float excessDistance = distance - safeThreshold;
        if (excessDistance > 0) {
            PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
            if (excessDistance >= 5.0F) {
                // Tier 2: Severe fracture on both legs
                data.setSeverity(LimbType.LEFT_LEG, 2);
                data.setSeverity(LimbType.RIGHT_LEG, 2);
                InjuryModule.setInjuryData(player.getUUID(), data);
                updatePlayerAttributesAndPose(player);
                playBoneCrackSound(player, 0.6F);
            } else {
                // Tier 1: Single random leg fractured or sprained
                boolean pickLeft = RANDOM.nextBoolean();
                LimbType targetLeg = pickLeft ? LimbType.LEFT_LEG : LimbType.RIGHT_LEG;
                int currentSev = data.getSeverity(targetLeg);
                data.setSeverity(targetLeg, Math.min(2, currentSev + 1));
                InjuryModule.setInjuryData(player.getUUID(), data);
                updatePlayerAttributesAndPose(player);
                playBoneCrackSound(player, 0.8F);
            }
        }

        // Near-Death Shock Check (<= 1.0 HP / 0.5 heart)
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
        if (!CommonConfig.ENABLE_INJURY.get()) return;
        if (player.isCreative() || player.isSpectator()) return;

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        boolean stateChanged = false;

        // 1. HEAD Trauma: Falling blocks (anvil/gravel/sand) or Elytra fly into wall
        boolean isAnvilOrFallingBlock = damageSource.is(DamageTypes.FALLING_ANVIL) ||
                damageSource.is(DamageTypes.FALLING_BLOCK);
        boolean isFlyIntoWall = damageSource.is(DamageTypes.FLY_INTO_WALL);

        if (isAnvilOrFallingBlock || isFlyIntoWall) {
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            int blastProtLevel = getEnchantmentLevel(helmet, Enchantments.BLAST_PROTECTION);
            
            // Blast Protection on Helmet mitigates 50% - 80% concussion chance
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

        // 2. TORSO Trauma: Heavy blunt hits from Ravager or Iron Golem
        boolean isHeavyBluntEntity = damageSource.getEntity() instanceof Ravager ||
                damageSource.getEntity() instanceof IronGolem;
        if (isHeavyBluntEntity || amount >= 12.0F) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            boolean isDiamondOrNetheriteChest = isDiamondOrNetheriteArmor(chestplate);

            int currentSev = data.getSeverity(LimbType.TORSO);
            if (amount >= 14.0F) {
                // Severe torso trauma
                data.setSeverity(LimbType.TORSO, 2);
                stateChanged = true;
                playBoneCrackSound(player, 0.6F);
            } else if (!isDiamondOrNetheriteChest && currentSev == 0) {
                // Tier 1 trauma nullified by Diamond or Netherite chestplate
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
        if (!CommonConfig.ENABLE_SPLINT.get() || splintStack.isEmpty() || !(splintStack.getItem() instanceof SplintItem splintItem)) {
            return false;
        }

        // Enforce cooldown so UI treatment cannot be spammed without channeling delay
        if (player.getCooldowns().isOnCooldown(splintItem)) {
            return false;
        }

        PlayerInjuryData data = InjuryModule.getOrCreateInjuryData(player.getUUID());
        byte currentSev = data.getSeverity(limb);
        if (currentSev <= 0) {
            return false;
        }

        // Reduce severity based on splint tier
        int reduction = (splintItem.getTier() == SplintTier.DIAMOND ||
                splintItem.getTier() == SplintTier.NETHERITE) ? 2 : 1;

        int newSev = Math.max(0, currentSev - reduction);
        data.setSeverity(limb, newSev);
        InjuryModule.setInjuryData(player.getUUID(), data);
        updatePlayerAttributesAndPose(player);

        // Apply 40-tick cooldown (2s) matching the item's use duration
        player.getCooldowns().addCooldown(splintItem, SplintItem.CHANNEL_DURATION);

        // Damage the splint by 1
        splintStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

        // Sound effect
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
        // 1. Check cursor item
        ItemStack cursor = player.containerMenu.getCarried();
        if (SplintItem.isValidSplint(cursor)) return cursor;

        // 2. Check main hand / offhand
        ItemStack main = player.getMainHandItem();
        if (SplintItem.isValidSplint(main)) return main;
        ItemStack off = player.getOffhandItem();
        if (SplintItem.isValidSplint(off)) return off;

        // 3. Check inventory slots
        for (ItemStack item : player.getInventory().items) {
            if (SplintItem.isValidSplint(item)) {
                return item;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean shouldCancelNaturalRegen(Player player) {
        return CommonConfig.ENABLE_INJURY.get() && isFractureActive(player);
    }
}
