package com.alikuxac.humannature.forge.event;

import com.alikuxac.humannature.api.event.EncumbranceChangeEvent;
import com.alikuxac.humannature.api.event.FractureEvent;
import com.alikuxac.humannature.api.event.HumanNatureEvents;
import com.alikuxac.humannature.api.event.HypothermiaEvent;
import com.alikuxac.humannature.api.event.LimbInjuryEvent;
import com.alikuxac.humannature.api.injury.InjuryTier;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.api.weight.EncumbranceTier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public final class ForgeHumanNatureEventBridge {
    private static boolean registered = false;

    private ForgeHumanNatureEventBridge() {}

    public static void register() {
        if (registered) return;
        registered = true;

        HumanNatureEvents.registerInjuryListener(commonEvent -> {
            ForgeLimbInjuryEvent forgeEvent = new ForgeLimbInjuryEvent(
                    commonEvent.getPlayer(), commonEvent.getLimb(), commonEvent.getSeverity()
            );
            MinecraftForge.EVENT_BUS.post(forgeEvent);
            if (forgeEvent.isCanceled()) {
                commonEvent.setCanceled(true);
            }
            commonEvent.setSeverity(forgeEvent.getSeverity());
        });

        HumanNatureEvents.registerFractureListener(commonEvent -> {
            ForgeFractureEvent forgeEvent = new ForgeFractureEvent(
                    commonEvent.getPlayer(), commonEvent.getTier(), commonEvent.isHealed()
            );
            MinecraftForge.EVENT_BUS.post(forgeEvent);
        });

        HumanNatureEvents.registerEncumbranceListener(commonEvent -> {
            ForgeEncumbranceChangeEvent forgeEvent = new ForgeEncumbranceChangeEvent(
                    commonEvent.getPlayer(), commonEvent.getOldTier(), commonEvent.getNewTier(), commonEvent.getCarriedWeight()
            );
            MinecraftForge.EVENT_BUS.post(forgeEvent);
        });

        HumanNatureEvents.registerHypothermiaListener(commonEvent -> {
            ForgeHypothermiaEvent forgeEvent = new ForgeHypothermiaEvent(
                    commonEvent.getPlayer(), commonEvent.getCoreTemperature()
            );
            MinecraftForge.EVENT_BUS.post(forgeEvent);
            if (forgeEvent.isCanceled()) {
                commonEvent.setCanceled(true);
            }
        });
    }

    @Cancelable
    public static class ForgeLimbInjuryEvent extends Event {
        private final Player player;
        private final LimbType limb;
        private int severity;

        public ForgeLimbInjuryEvent(Player player, LimbType limb, int severity) {
            this.player = player;
            this.limb = limb;
            this.severity = severity;
        }

        public Player getPlayer() { return player; }
        public LimbType getLimb() { return limb; }
        public int getSeverity() { return severity; }
        public void setSeverity(int severity) { this.severity = severity; }
    }

    public static class ForgeFractureEvent extends Event {
        private final Player player;
        private final InjuryTier tier;
        private final boolean healed;

        public ForgeFractureEvent(Player player, InjuryTier tier, boolean healed) {
            this.player = player;
            this.tier = tier;
            this.healed = healed;
        }

        public Player getPlayer() { return player; }
        public InjuryTier getTier() { return tier; }
        public boolean isHealed() { return healed; }
    }

    public static class ForgeEncumbranceChangeEvent extends Event {
        private final Player player;
        private final EncumbranceTier oldTier;
        private final EncumbranceTier newTier;
        private final float carriedWeight;

        public ForgeEncumbranceChangeEvent(Player player, EncumbranceTier oldTier, EncumbranceTier newTier, float carriedWeight) {
            this.player = player;
            this.oldTier = oldTier;
            this.newTier = newTier;
            this.carriedWeight = carriedWeight;
        }

        public Player getPlayer() { return player; }
        public EncumbranceTier getOldTier() { return oldTier; }
        public EncumbranceTier getNewTier() { return newTier; }
        public float getCarriedWeight() { return carriedWeight; }
    }

    @Cancelable
    public static class ForgeHypothermiaEvent extends Event {
        private final Player player;
        private final float coreTemperature;

        public ForgeHypothermiaEvent(Player player, float coreTemperature) {
            this.player = player;
            this.coreTemperature = coreTemperature;
        }

        public Player getPlayer() { return player; }
        public float getCoreTemperature() { return coreTemperature; }
    }
}
