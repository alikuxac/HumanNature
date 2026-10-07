package com.alikuxac.humannature.forge.event;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
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
    public static void onLivingHeal(net.neoforged.neoforge.event.entity.living.LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (InjuryEventHandler.shouldCancelNaturalRegen(player)) {
                // Cancel natural food/saturation passive regeneration
                event.setCanceled(true);
            }
        }
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

            boolean canHeal = isCrouching || isSleeping || isStandingStill;

            if (InjuryEventHandler.isFractureActive(player)) {
                InjuryEventHandler.updatePlayerAttributesAndPose(player);

                if (canHeal) {
                    int reductionAmount = (isCrouching || isSleeping) ? 2 : 1;
                    InjuryEventHandler.reduceFractureTimer(player, reductionAmount);
                }
            }

            PREV_WALK_DIST.put(playerId, currentWalkDist);
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        InjuryEventHandler.handleItemConsumption(player, event.getItem());
    }
}
