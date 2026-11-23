package com.alikuxac.humannature.neoforge.network;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.api.blood.BloodType;
import com.alikuxac.humannature.modules.injury.InjuryModule;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import com.alikuxac.humannature.modules.physiology.PhysiologyEventHandler;
import com.alikuxac.humannature.modules.physiology.PhysiologyModule;
import com.alikuxac.humannature.modules.physiology.PlayerPhysiologyData;
import com.alikuxac.humannature.network.NetworkHandler;
import com.alikuxac.humannature.network.SyncBloodPayload;
import com.alikuxac.humannature.network.SyncInjuryPayload;
import com.alikuxac.humannature.network.SyncNutritionPayload;
import com.alikuxac.humannature.network.TreatLimbPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NeoForgeNetwork {

    public static void initClient() {
        NetworkHandler.setSender(PacketDistributor::sendToServer);
    }

    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(HumanNatureCommon.MOD_ID);

        registrar.playToClient(
                SyncBloodPayload.TYPE,
                SyncBloodPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() != null) {
                            PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(context.player().getUUID());
                            data.setBloodVolume(payload.bloodVolume());
                            data.setBloodType(BloodType.fromId(payload.bloodTypeId()));
                            data.setBleedingTier(payload.bleedingTier());
                        }
                    });
                }
        );

        registrar.playToClient(
                SyncNutritionPayload.TYPE,
                SyncNutritionPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() != null) {
                            PlayerPhysiologyData data = PhysiologyModule.getOrCreatePhysiologyData(context.player().getUUID());
                            data.setCalcium(payload.calcium());
                            data.setCarbohydrates(payload.carbohydrates());
                            data.setVitamins(payload.vitamins());
                        }
                    });
                }
        );

        registrar.playToClient(
                SyncInjuryPayload.TYPE,
                SyncInjuryPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() != null) {
                        InjuryModule.setInjuryData(context.player().getUUID(), new PlayerInjuryData(payload.limbs()));
                    }
                })
        );

        registrar.playToServer(
                TreatLimbPayload.TYPE,
                TreatLimbPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        PhysiologyEventHandler.handleDiagnosticTreat(serverPlayer, payload.limbIndex(), payload.treatmentType());
                    }
                })
        );
    }
}
