package com.alikuxac.humannature.neoforge.event;

import com.alikuxac.humannature.api.event.EncumbranceChangeEvent;
import com.alikuxac.humannature.api.event.FractureEvent;
import com.alikuxac.humannature.api.event.HumanNatureEvents;
import com.alikuxac.humannature.api.event.HypothermiaEvent;
import com.alikuxac.humannature.api.event.LimbInjuryEvent;
import com.alikuxac.humannature.api.injury.InjuryTier;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.api.weight.EncumbranceTier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class NeoForgeHumanNatureEventBridge {
    private static boolean registered = false;

    private NeoForgeHumanNatureEventBridge() {}

    public static void register() {
        if (registered) return;
        registered = true;

        HumanNatureEvents.registerInjuryListener(commonEvent -> {
            NeoForgeLimbInjuryEvent neoEvent = new NeoForgeLimbInjuryEvent(
                    commonEvent.getPlayer(), commonEvent.getLimb(), commonEvent.getSeverity()
            );
            NeoForge.EVENT_BUS.post(neoEvent);
            if (neoEvent.isCanceled()) {
                commonEvent.setCanceled(true);
            }
            commonEvent.setSeverity(neoEvent.getSeverity());
        });

        HumanNatureEvents.registerFractureListener(commonEvent -> {
            NeoForgeFractureEvent neoEvent = new NeoForgeFractureEvent(
                    commonEvent.getPlayer(), commonEvent.getTier(), commonEvent.isHealed()
            );
            NeoForge.EVENT_BUS.post(neoEvent);
        });

        HumanNatureEvents.registerEncumbranceListener(commonEvent -> {
            NeoForgeEncumbranceChangeEvent neoEvent = new NeoForgeEncumbranceChangeEvent(
                    commonEvent.getPlayer(), commonEvent.getOldTier(), commonEvent.getNewTier(), commonEvent.getCarriedWeight()
            );
            NeoForge.EVENT_BUS.post(neoEvent);
        });

        HumanNatureEvents.registerHypothermiaListener(commonEvent -> {
            NeoForgeHypothermiaEvent neoEvent = new NeoForgeHypothermiaEvent(
                    commonEvent.getPlayer(), commonEvent.getCoreTemperature()
            );
            NeoForge.EVENT_BUS.post(neoEvent);
            if (neoEvent.isCanceled()) {
                commonEvent.setCanceled(true);
            }
        });
    }

    public static class NeoForgeLimbInjuryEvent extends Event implements ICancellableEvent {
        private final Player player;
        private final LimbType limb;
        private int severity;

        public NeoForgeLimbInjuryEvent(Player player, LimbType limb, int severity) {
            this.player = player;
            this.limb = limb;
            this.severity = severity;
        }

        public Player getPlayer() { return player; }
        public LimbType getLimb() { return limb; }
        public int getSeverity() { return severity; }
        public void setSeverity(int severity) { this.severity = severity; }
    }

    public static class NeoForgeFractureEvent extends Event {
        private final Player player;
        private final InjuryTier tier;
        private final boolean healed;

        public NeoForgeFractureEvent(Player player, InjuryTier tier, boolean healed) {
            this.player = player;
            this.tier = tier;
            this.healed = healed;
        }

        public Player getPlayer() { return player; }
        public InjuryTier getTier() { return tier; }
        public boolean isHealed() { return healed; }
    }

    public static class NeoForgeEncumbranceChangeEvent extends Event {
        private final Player player;
        private final EncumbranceTier oldTier;
        private final EncumbranceTier newTier;
        private final float carriedWeight;

        public NeoForgeEncumbranceChangeEvent(Player player, EncumbranceTier oldTier, EncumbranceTier newTier, float carriedWeight) {
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

    public static class NeoForgeHypothermiaEvent extends Event implements ICancellableEvent {
        private final Player player;
        private final float coreTemperature;

        public NeoForgeHypothermiaEvent(Player player, float coreTemperature) {
            this.player = player;
            this.coreTemperature = coreTemperature;
        }

        public Player getPlayer() { return player; }
        public float getCoreTemperature() { return coreTemperature; }
    }
}
