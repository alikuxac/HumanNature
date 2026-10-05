package com.alikuxac.humannature.neoforge.event;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.compat.CuriosCompat;
import com.alikuxac.humannature.config.CommonConfig;
import com.alikuxac.humannature.item.SplintItem;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = HumanNatureCommon.MOD_ID)
public class NeoForgeEvents {

    private static final Map<UUID, Float> PREV_WALK_DIST = new HashMap<>();

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        InjuryEventHandler.onLivingFall(event.getEntity(), event.getDistance(), event.getDamageMultiplier());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!CommonConfig.ENABLE_SPLINT.get()) return;
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        UUID playerId = player.getUUID();
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
}

