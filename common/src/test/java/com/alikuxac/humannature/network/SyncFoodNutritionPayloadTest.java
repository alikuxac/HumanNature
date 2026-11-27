package com.alikuxac.humannature.network;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SyncFoodNutritionPayloadTest {
    @Test
    void snapshotIsChunkedAndRoundTripsWithoutLosingEntries() {
        Map<ResourceLocation, NutritionValues> values = new HashMap<>();
        for (int i = 0; i < 600; i++) {
            values.put(ResourceLocation.parse("example:food_" + i), new NutritionValues(0, 8, 20));
        }
        var packets = SyncFoodNutritionPayload.split(values);
        assertEquals(3, packets.size());
        assertTrue(packets.getFirst().first());
        assertFalse(packets.getFirst().last());
        assertFalse(packets.getLast().first());
        assertTrue(packets.getLast().last());
        Map<ResourceLocation, NutritionValues> received = new HashMap<>();
        for (var packet : packets) {
            assertTrue(packet.values().size() <= 256);
            var buffer = Unpooled.buffer();
            try {
                SyncFoodNutritionPayload.STREAM_CODEC.encode(buffer, packet);
                var decoded = SyncFoodNutritionPayload.STREAM_CODEC.decode(buffer);
                assertEquals(packet, decoded);
                received.putAll(decoded.values());
            } finally {
                buffer.release();
            }
        }
        assertEquals(values, received);
    }

    @Test
    void emptySnapshotStillSendsOneReplacementPacket() {
        var packets = SyncFoodNutritionPayload.split(Map.of());
        assertEquals(1, packets.size());
        assertTrue(packets.getFirst().first());
        assertTrue(packets.getFirst().last());
        assertTrue(packets.getFirst().values().isEmpty());
    }

    @Test
    void decoderRejectsOversizedBatchBeforeReadingEntries() {
        var buffer = Unpooled.buffer();
        try {
            buffer.writeBoolean(true).writeBoolean(true).writeInt(257);
            assertThrows(IllegalArgumentException.class, () -> SyncFoodNutritionPayload.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }
}
