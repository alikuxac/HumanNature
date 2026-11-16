package com.alikuxac.humannature.forge.client;

import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.network.SyncBloodPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ForgeClientPayloadHandler {

    public static void handleSyncBlood(SyncBloodPayload payload) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
            data.setBloodVolume(payload.bloodVolume());
            data.setBloodType(BloodType.fromId(payload.bloodTypeId()));
            data.setBleedingTier(payload.bleedingTier());
        }
    }
}

