package com.alikuxac.humannature.network;

import com.alikuxac.humannature.HumanNatureCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncNutritionPayload(byte calcium, byte carbohydrates, byte vitamins) implements CustomPacketPayload {
    public static final Type<SyncNutritionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "sync_nutrition"));

    public static final StreamCodec<ByteBuf, SyncNutritionPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeByte(payload.calcium());
                buf.writeByte(payload.carbohydrates());
                buf.writeByte(payload.vitamins());
            },
            buf -> new SyncNutritionPayload(buf.readByte(), buf.readByte(), buf.readByte())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
