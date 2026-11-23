package com.alikuxac.humannature.network;

import com.alikuxac.humannature.HumanNatureCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncBloodPayload(short bloodVolume, byte bloodTypeId, byte bleedingTier) implements CustomPacketPayload {
    public static final Type<SyncBloodPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "sync_blood"));

    public static final StreamCodec<ByteBuf, SyncBloodPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeShort(payload.bloodVolume());
                buf.writeByte(payload.bloodTypeId());
                buf.writeByte(payload.bleedingTier());
            },
            buf -> new SyncBloodPayload(buf.readShort(), buf.readByte(), buf.readByte())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

