package com.alikuxac.humannature.client;

import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;

public class AimTremorHandler {

    public record AimOffset(float pitch, float yaw) {}

    public static AimOffset calculateAimOffset(Player player, float partialTick) {
        if (!PhysiologyModule.isPanicking(player.getUUID())) {
            return new AimOffset(0.0f, 0.0f);
        }
        if (!player.isUsingItem()) {
            return new AimOffset(0.0f, 0.0f);
        }
        Item item = player.getUseItem().getItem();
        if (!(item instanceof BowItem) && !(item instanceof CrossbowItem)) {
            return new AimOffset(0.0f, 0.0f);
        }

        float time = player.tickCount + partialTick;
        float pitchOffset = (float) (Math.sin(time * 0.45) * 0.35 + Math.cos(time * 0.85) * 0.20);
        float yawOffset = (float) (Math.cos(time * 0.40) * 0.40 + Math.sin(time * 1.05) * 0.25);
        return new AimOffset(pitchOffset, yawOffset);
    }
}
