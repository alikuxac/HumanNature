package com.alikuxac.humannature.network;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.modules.injury.PlayerInjuryData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncInjuryPayload(byte[] limbs) implements CustomPacketPayload {
    public static final Type<SyncInjuryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "sync_injury"));

    public static final StreamCodec<ByteBuf, SyncInjuryPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                byte[] data = payload.limbs();
                if (data == null || data.length != PlayerInjuryData.LIMB_COUNT) {
                    data = new byte[PlayerInjuryData.LIMB_COUNT];
                }
                buf.writeBytes(data, 0, PlayerInjuryData.LIMB_COUNT);
            },
            buf -> {
                byte[] data = new byte[PlayerInjuryData.LIMB_COUNT];
                buf.readBytes(data, 0, PlayerInjuryData.LIMB_COUNT);
                return new SyncInjuryPayload(data);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
