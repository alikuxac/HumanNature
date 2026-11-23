package com.alikuxac.humannature.advancement;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public final class HumanNatureAdvancements {
    public static final String SPLINT_APPLIED = "splint_applied";
    public static final String FRACTURE_HEALED = "fracture_healed";
    public static final String OVERBURDENED = "overburdened";
    public static final String MAX_LOAD_BEARING = "max_load_bearing";
    public static final String HYPOTHERMIA = "hypothermia";
    public static final String HEAT_EXHAUSTION = "heat_exhaustion";
    public static final String BLOOD_TRANSFUSED = "blood_transfused";
    public static final String MAX_NUTRITION = "max_nutrition";

    public static Supplier<HumanNatureTrigger> GAMEPLAY_EVENT = () -> null;

    private HumanNatureAdvancements() {
    }

    public static void trigger(Player player, String eventId) {
        if (!(player instanceof ServerPlayer serverPlayer) || !player.isAlive()
                || player.isCreative() || player.isSpectator()) return;
        HumanNatureTrigger trigger = GAMEPLAY_EVENT.get();
        if (trigger != null) {
            trigger.trigger(serverPlayer, eventId);
        }
    }
}
