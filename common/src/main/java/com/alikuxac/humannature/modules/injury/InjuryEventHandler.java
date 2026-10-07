package com.alikuxac.humannature.modules.injury;

import com.alikuxac.humannature.config.CommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;

public class InjuryEventHandler {
    public static final ResourceLocation FRACTURE_SPEED_MODIFIER_LEG1_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_speed_leg1");
    public static final ResourceLocation FRACTURE_SPEED_MODIFIER_LEG2_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_speed_leg2");

    public static final ResourceLocation FRACTURE_JUMP_MODIFIER_LEG1_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_jump_leg1");
    public static final ResourceLocation FRACTURE_JUMP_MODIFIER_LEG2_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_jump_leg2");

    public static final ResourceLocation FRACTURE_MAX_HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("humannature", "bone_fracture_max_health");

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
        InjuryTier tier = getFractureTier(player);
        return tier.getTierLevel();
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

        InjuryTier tier = getFractureTier(player);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);

        if (speedAttr == null) return;

        // Clean up existing attribute modifiers to prevent duplicates
        speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID);
        speedAttr.removeModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID);
        speedAttr.removeModifier(SPLINT_SPEED_MODIFIER_ID);
        if (jumpAttr != null) {
            jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG1_ID);
            jumpAttr.removeModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID);
        }

        if (tier == InjuryTier.NONE) {
            if (maxHealthAttr != null) {
                maxHealthAttr.removeModifier(FRACTURE_MAX_HEALTH_MODIFIER_ID);
            }
            return;
        }

        player.setSprinting(false);

        // Max Health suppression (-6 HP) when injured
        if (maxHealthAttr != null && !maxHealthAttr.hasModifier(FRACTURE_MAX_HEALTH_MODIFIER_ID)) {
            maxHealthAttr.addTransientModifier(FRACTURE_MAX_HEALTH_MODIFIER);
            if (player.getHealth() > maxHealthAttr.getValue()) {
                player.setHealth((float) Math.max(1.0, maxHealthAttr.getValue()));
            }
        }

        if (tier == InjuryTier.TIER_1_SINGLE_LEG) {
            if (!speedAttr.hasModifier(FRACTURE_SPEED_MODIFIER_LEG1_ID)) {
                speedAttr.addTransientModifier(FRACTURE_SPEED_MODIFIER_LEG1);
            }
        } else if (tier == InjuryTier.TIER_2_DOUBLE_LEG) {
            if (!speedAttr.hasModifier(FRACTURE_SPEED_MODIFIER_LEG2_ID)) {
                speedAttr.addTransientModifier(FRACTURE_SPEED_MODIFIER_LEG2);
            }
            if (jumpAttr != null && !jumpAttr.hasModifier(FRACTURE_JUMP_MODIFIER_LEG2_ID)) {
                jumpAttr.addTransientModifier(FRACTURE_JUMP_MODIFIER_LEG2);
            }
            player.setPose(Pose.SWIMMING);
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

        InjuryTier currentTier = getFractureTier(player);

        if (distance >= 10.0F) {
            applyBoneFractureTier(player, InjuryTier.TIER_2_DOUBLE_LEG);
        } else if (distance >= 5.0F) {
            applyBoneFractureTier(player, InjuryTier.TIER_1_SINGLE_LEG);
        } else if (distance >= 4.0F && currentTier == InjuryTier.TIER_1_SINGLE_LEG) {
            // Second fall while Tier 1 escalates to Tier 2
            applyBoneFractureTier(player, InjuryTier.TIER_2_DOUBLE_LEG);
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

    public static boolean shouldCancelNaturalRegen(Player player) {
        return CommonConfig.ENABLE_INJURY.get() && isFractureActive(player);
    }
}
