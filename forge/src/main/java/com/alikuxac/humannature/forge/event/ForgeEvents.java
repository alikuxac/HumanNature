package com.alikuxac.humannature.forge.event;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.compat.CuriosCompat;
import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = HumanNatureCommon.MOD_ID)
public class ForgeEvents {

    private static final Map<UUID, Float> PREV_WALK_DIST = new HashMap<>();

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        InjuryEventHandler.onLivingFall(event.getEntity(), event.getDistance(), event.getDamageMultiplier());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        UUID playerId = player.getUUID();

        if (CommonConfig.ENABLE_INJURY.get()) {
            float currentWalkDist = player.walkDist;
            float prevWalkDist = PREV_WALK_DIST.getOrDefault(playerId, currentWalkDist);
            float delta = currentWalkDist - prevWalkDist;

            boolean isCrouching = player.isCrouching();
            boolean isSleeping = player.isSleeping();
            boolean isStandingStill = Math.abs(delta) < 0.001F;
            boolean hasSplint = CommonConfig.ENABLE_SPLINT.get() && CuriosCompat.hasActiveSplint(player);
            
            boolean canHeal = hasSplint || isCrouching || isSleeping || isStandingStill || isSittingOnCarpet(player);

            if (canHeal && InjuryEventHandler.isFractureActive(player)) {
                int reductionAmount = (isCrouching || isSleeping) ? 2 : 1;
                InjuryEventHandler.reduceFractureTimer(player, reductionAmount);

                if (!InjuryEventHandler.isFractureActive(player)) {
                    InjuryEventHandler.clearFractureTimer(player);
                }
            }
        }

        float currentWalkDist = player.walkDist;
        float prevWalkDist = PREV_WALK_DIST.getOrDefault(playerId, currentWalkDist);
        float delta = currentWalkDist - prevWalkDist;

        if (delta >= SplintItem.STEPS_PER_DURABILITY) {
            List<ItemStack> splintStacks = CuriosCompat.getSplintStacks(player);
            if (!splintStacks.isEmpty()) {
                int stepsToConsume = (int) (delta / SplintItem.STEPS_PER_DURABILITY);
                boolean anyBroken = false;
                
                for (ItemStack splintStack : splintStacks) {
                    for (int i = 0; i < stepsToConsume; i++) {
                        SplintItem.consumeDurability(player, splintStack);
                        if (splintStack.getDamageValue() >= splintStack.getMaxDamage()) {
                            anyBroken = true;
                            break;
                        }
                    }
                }
                
                // Re-apply injury modifiers if all splints broke
                if (anyBroken && !CuriosCompat.hasActiveSplint(player)) {
                    InjuryEventHandler.applyBoneFracture(player);
                }
            }
        }
        PREV_WALK_DIST.put(playerId, currentWalkDist);
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        InjuryEventHandler.handleItemConsumption(player, event.getItem());
    }
    
    // Helper method to check if player is sitting on carpet/stairs
    private static boolean isSittingOnCarpet(Player player) {
        return false; // Placeholder - can be enhanced later
    }
}
