package com.alikuxac.humannature.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayloadCodecTest {

    @Test
    void syncNutritionPayloadRoundTrip() {
        ByteBuf buf = Unpooled.buffer();
        SyncNutritionPayload payload = new SyncNutritionPayload((byte) 80, (byte) 55, (byte) 20);

        SyncNutritionPayload.STREAM_CODEC.encode(buf, payload);

        assertEquals(payload, SyncNutritionPayload.STREAM_CODEC.decode(buf));
    }

    @Test
    void treatLimbPayloadRoundTrip() {
        ByteBuf buf = Unpooled.buffer();
        TreatLimbPayload payload = new TreatLimbPayload((byte) 4, TreatLimbPayload.TYPE_BANDAGE);

        TreatLimbPayload.STREAM_CODEC.encode(buf, payload);

        assertEquals(payload, TreatLimbPayload.STREAM_CODEC.decode(buf));
    }
}
