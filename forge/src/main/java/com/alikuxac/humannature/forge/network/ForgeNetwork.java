package com.alikuxac.humannature.forge.network;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.forge.client.ForgeClientPayloadHandler;
import com.alikuxac.humannature.network.SyncBloodPayload;
import com.alikuxac.humannature.network.SyncInjuryPayload;
import com.alikuxac.humannature.network.TreatLimbPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.ChannelBuildable;

public class ForgeNetwork {
    private static final int PROTOCOL_VERSION = 1;
    private static Channel<CustomPacketPayload> CHANNEL;

    public static void register() {
        var builder = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "main"))
                .networkProtocolVersion(PROTOCOL_VERSION)
                .optional()
                .payloadChannel();

        builder.play(protocol -> {
            protocol.clientbound()
                    .add(SyncBloodPayload.TYPE, SyncBloodPayload.STREAM_CODEC.cast(), (payload, context) -> {
                        context.enqueueWork(() -> {
                            if (context.isClientSide()) {
                                ForgeClientPayloadHandler.handleSyncBlood(payload);
                            }
                        });
                        context.setPacketHandled(true);
                    })
                    .add(SyncInjuryPayload.TYPE, SyncInjuryPayload.STREAM_CODEC.cast(), (payload, context) -> {
                        context.enqueueWork(() -> {});
                        context.setPacketHandled(true);
                    });

            protocol.serverbound()
                    .add(TreatLimbPayload.TYPE, TreatLimbPayload.STREAM_CODEC.cast(), (payload, context) -> {
                        context.enqueueWork(() -> {});
                        context.setPacketHandled(true);
                    });
        });

        if (builder instanceof ChannelBuildable<?> buildable) {
            CHANNEL = (Channel<CustomPacketPayload>) buildable.build();
        }
    }

    public static Channel<CustomPacketPayload> getChannel() {
        return CHANNEL;
    }
}

