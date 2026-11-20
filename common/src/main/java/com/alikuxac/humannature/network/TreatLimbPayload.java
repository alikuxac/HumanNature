package com.alikuxac.humannature.network;

import com.alikuxac.humannature.HumanNatureCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TreatLimbPayload(byte limbIndex, byte treatmentType) implements CustomPacketPayload {
    public static final byte TYPE_SPLINT = 0;
    public static final byte TYPE_BANDAGE = 1;

    public static final Type<TreatLimbPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "treat_limb"));

    public static final StreamCodec<ByteBuf, TreatLimbPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            TreatLimbPayload::limbIndex,
            ByteBufCodecs.BYTE,
            TreatLimbPayload::treatmentType,
            TreatLimbPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
