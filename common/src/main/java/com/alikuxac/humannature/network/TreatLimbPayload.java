package com.alikuxac.humannature.network;

import com.alikuxac.humannature.HumanNatureCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TreatLimbPayload(byte limbIndex) implements CustomPacketPayload {
    public static final Type<TreatLimbPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "treat_limb"));

    public static final StreamCodec<ByteBuf, TreatLimbPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            TreatLimbPayload::limbIndex,
            TreatLimbPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
