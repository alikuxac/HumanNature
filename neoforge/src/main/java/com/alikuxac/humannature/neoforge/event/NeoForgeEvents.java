package com.alikuxac.humannature.neoforge.event;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.physiology.PhysiologyEventHandler;
import com.alikuxac.humannature.modules.temperature.TemperatureEventHandler;
import com.alikuxac.humannature.modules.weight.WeightEventHandler;
import com.alikuxac.humannature.neoforge.init.NeoForgeModPotions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = HumanNatureCommon.MOD_ID)
public class NeoForgeEvents {

    @SubscribeEvent
    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.AWKWARD, Items.ARMADILLO_SCUTE, NeoForgeModPotions.BURDEN_BEARER);
        event.getBuilder().addMix(Potions.AWKWARD, Items.IRON_INGOT, NeoForgeModPotions.BURDEN_BEARER);
        event.getBuilder().addMix(NeoForgeModPotions.BURDEN_BEARER, Items.REDSTONE, NeoForgeModPotions.LONG_BURDEN_BEARER);
        event.getBuilder().addMix(NeoForgeModPotions.BURDEN_BEARER, Items.GLOWSTONE_DUST, NeoForgeModPotions.STRONG_BURDEN_BEARER);
    }

    private static final Map<UUID, Float> PREV_WALK_DIST = new HashMap<>();

    @SubscribeEvent
    public static void onLivingUseTotem(LivingUseTotemEvent event) {
        if (event.getEntity() instanceof Player player) {
            InjuryEventHandler.onLivingUseTotem(player, event.getSource(), event.getTotem(), event.getHandHolding());
        }
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player) {
            if (InjuryEventHandler.shouldDenyMobEffect(player, event.getEffectInstance())) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                return;
            }
            if (PhysiologyEventHandler.handleMobEffectApplicable(player, event.getEffectInstance())) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.level().isClientSide()) return;
            PhysiologyEventHandler.recordJumpAction(player);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        InjuryEventHandler.onLivingFall(event.getEntity(), event.getDistance(), event.getDamageMultiplier());
        if (event.getEntity() instanceof Player player) {
            float mult = WeightEventHandler.getFallDamageMultiplier(player);
            if (mult > 1.0f) {
                event.setDamageMultiplier(event.getDamageMultiplier() * mult);
            }
            float nutritionMult = InjuryEventHandler.getNutritionFallDamageMultiplier(player);
            if (nutritionMult < 1.0f) {
                event.setDamageMultiplier(event.getDamageMultiplier() * nutritionMult);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        InjuryEventHandler.onLivingDamage(event.getEntity(), event.getSource(), event.getNewDamage());
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (InjuryEventHandler.shouldCancelNaturalRegen(player, event.getAmount())
                    || PhysiologyEventHandler.shouldCancelNaturalRegen(player, event.getAmount())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID playerId = player.getUUID();
            WAS_SLEEPING.remove(playerId);
            CONSECUTIVE_SLEEP_TICKS.remove(playerId);
            PREV_POS.remove(playerId);
            PREV_WALK_DIST.remove(playerId);

            if (ServerConfig.ENABLE_INJURY.get()) {
                InjuryEventHandler.onPlayerDeath(player);
            }
            if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
                PhysiologyEventHandler.onPlayerDeath(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();
        WAS_SLEEPING.remove(playerId);
        CONSECUTIVE_SLEEP_TICKS.remove(playerId);
        PREV_POS.remove(playerId);
        PREV_WALK_DIST.remove(playerId);

        if (ServerConfig.ENABLE_INJURY.get()) {
            InjuryEventHandler.onPlayerRespawn(player);
        }
        if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
            PhysiologyEventHandler.onPlayerRespawn(player);
        }
        if (ServerConfig.ENABLE_TEMPERATURE.get()) {
            TemperatureEventHandler.onPlayerRespawn(player);
        }
        if (ServerConfig.ENABLE_WEIGHT.get()) {
            WeightEventHandler.onPlayerRespawn(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player player = event.getEntity();
            UUID playerId = player.getUUID();
            WAS_SLEEPING.remove(playerId);
            CONSECUTIVE_SLEEP_TICKS.remove(playerId);
            PREV_POS.remove(playerId);
            PREV_WALK_DIST.remove(playerId);

            if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
                PhysiologyEventHandler.onPlayerRespawn(player);
            }
            if (ServerConfig.ENABLE_TEMPERATURE.get()) {
                TemperatureEventHandler.onPlayerRespawn(player);
            }
            if (ServerConfig.ENABLE_WEIGHT.get()) {
                WeightEventHandler.onPlayerRespawn(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();
        WAS_SLEEPING.remove(playerId);
        CONSECUTIVE_SLEEP_TICKS.remove(playerId);
        PREV_POS.remove(playerId);
        PREV_WALK_DIST.remove(playerId);
        PhysiologyEventHandler.onPlayerLoggedOut(player);
        TemperatureEventHandler.onPlayerLoggedOut(player);
        WeightEventHandler.onPlayerLoggedOut(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        WeightEventHandler.onPlayerLoggedIn(event.getEntity());
        if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
            PhysiologyEventHandler.onPlayerLoggedIn(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        WeightEventHandler.onItemPickup(event.getPlayer());
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        WeightEventHandler.onItemToss(event.getPlayer());
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof Player player) {
            WeightEventHandler.onContainerClose(player);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            WeightEventHandler.onEquipmentChange(player);
        }
    }

    private static final Map<UUID, Boolean> WAS_SLEEPING = new HashMap<>();
    private static final Map<UUID, Integer> CONSECUTIVE_SLEEP_TICKS = new HashMap<>();
    private static final Map<UUID, Vec3> PREV_POS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        UUID playerId = player.getUUID();

        Vec3 currentPos = player.position();
        Vec3 prevPos = PREV_POS.getOrDefault(playerId, currentPos);
        double dx = currentPos.x - prevPos.x;
        double dz = currentPos.z - prevPos.z;
        double horizontalDistSq = dx * dx + dz * dz;

        float currentWalkDist = player.walkDist;
        float prevWalkDist = PREV_WALK_DIST.getOrDefault(playerId, currentWalkDist);
        float delta = currentWalkDist - prevWalkDist;

        boolean isMoving = horizontalDistSq > 0.0004
                || Math.abs(delta) > 0.001F
                || player.getDeltaMovement().horizontalDistanceSqr() > 0.0004;
        boolean isStandingStill = !isMoving;

        boolean isCrouching = player.isCrouching();
        boolean isSleeping = player.isSleeping();

        int sleepTicks = CONSECUTIVE_SLEEP_TICKS.getOrDefault(playerId, 0);
        if (isSleeping) {
            sleepTicks++;
            CONSECUTIVE_SLEEP_TICKS.put(playerId, sleepTicks);
        }

        boolean wasSleeping = WAS_SLEEPING.getOrDefault(playerId, false);
        if (wasSleeping && !isSleeping) {
            if (ServerConfig.ENABLE_INJURY.get()) {
                InjuryEventHandler.onPlayerWakeUp(player);
            }
            if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
                PhysiologyEventHandler.onPlayerWakeUp(player, sleepTicks);
            }
            CONSECUTIVE_SLEEP_TICKS.remove(playerId);
        }
        WAS_SLEEPING.put(playerId, isSleeping);

        if (ServerConfig.ENABLE_PHYSIOLOGY.get()) {
            PhysiologyEventHandler.onPlayerTick(player, isMoving, isSleeping);
        }

        if (ServerConfig.ENABLE_TEMPERATURE.get()) {
            TemperatureEventHandler.onPlayerTick(player);
        }

        if (ServerConfig.ENABLE_WEIGHT.get()) {
            WeightEventHandler.onPlayerTick(player);
        }

        if (ServerConfig.ENABLE_INJURY.get()) {
            InjuryEventHandler.onPlayerTick(player);
            InjuryEventHandler.recordFlightTick(player);

            boolean isResting = isSleeping || (isCrouching && isStandingStill);
            boolean canHeal = isSleeping || isStandingStill;

            if (InjuryEventHandler.isFractureActive(player)) {
                InjuryEventHandler.updatePlayerAttributesAndPose(player);

                if (canHeal) {
                    int reductionAmount = isResting ? 2 : 1;
                    InjuryEventHandler.reduceFractureTimer(player, reductionAmount, true);
                }
            }
        }

        PREV_WALK_DIST.put(playerId, currentWalkDist);
        PREV_POS.put(playerId, currentPos);
    }

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        PhysiologyEventHandler.onItemUseStart(player, event.getItem());
    }

    @SubscribeEvent
    public static void onItemUseStop(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        PhysiologyEventHandler.onItemUseStop(player);
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        InjuryEventHandler.handleItemConsumption(player, event.getItem());
        PhysiologyEventHandler.handleItemConsumption(player, event.getItem());
        TemperatureEventHandler.handleItemConsumption(player, event.getItem());
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        PhysiologyEventHandler.recordMiningAction(player);
        float originalSpeed = event.getNewSpeed();
        float modifiedSpeed = InjuryEventHandler.modifyBreakSpeed(player, originalSpeed);
        modifiedSpeed = PhysiologyEventHandler.modifyBreakSpeed(player, modifiedSpeed);
        event.setNewSpeed(modifiedSpeed);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        float blockHardness = event.getState().getDestroySpeed(event.getLevel(), event.getPos());
        InjuryEventHandler.checkMiningExhaustion(player, blockHardness);
        InjuryEventHandler.checkSpasmDrop(player);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        PhysiologyEventHandler.recordAttackAction(player);
        InjuryEventHandler.checkSpasmDrop(player);
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getEntity() instanceof Player player) {
            InteractionHand hand = player.getUsedItemHand();
            boolean isOffHand = (hand == InteractionHand.OFF_HAND);

            if (isOffHand && InjuryEventHandler.isOffhandLocked(player)) {
                event.setBlocked(false);
                event.setCanceled(true);
                return;
            } else if (!isOffHand && InjuryEventHandler.isMainArmBroken(player)) {
                event.setBlocked(false);
                event.setCanceled(true);
                return;
            }
            InjuryEventHandler.onShieldBlock(player, event.getDamageSource(), event.getBlockedDamage(), hand);
        }
    }

    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getUsedItemHand() == InteractionHand.OFF_HAND && InjuryEventHandler.isOffhandLocked(player)) {
                event.setCanceled(true);
                return;
            }
        }
        if (InjuryEventHandler.handleWeaponDrawTick(event.getEntity(), event.getItem(), event.getDuration())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (event.getHand() == InteractionHand.OFF_HAND) {
            if (InjuryEventHandler.isOffhandLocked(player)) {
                event.setCanceled(true);
                return;
            }
        }
        ItemStack stack = event.getItemStack();
        if (stack.is(Items.LEATHER) || stack.is(Items.STRING)) {
            if (PhysiologyEventHandler.applyEmergencyTourniquet(player, stack)) {
                event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));
                event.setCanceled(true);
                return;
            }
        }
        InjuryEventHandler.onPlayerRightClickItem(player, event.getHand(), event.getItemStack());
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Player targetPlayer) {
            ItemStack stack = event.getItemStack();
            if (stack.is(Items.LEATHER) || stack.is(Items.STRING)) {
                if (PhysiologyEventHandler.applyEmergencyTourniquetToTarget(event.getEntity(), targetPlayer, stack)) {
                    event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.OFF_HAND) {
            Player player = event.getEntity();
            if (InjuryEventHandler.isOffhandLocked(player)) {
                event.setCanceled(true);
            }
        }
    }
}
