package com.alikuxac.humannature.network;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public record SyncFoodNutritionPayload(boolean first, boolean last,
        Map<ResourceLocation, NutritionValues> values) implements CustomPacketPayload {
    private static final int MAX_BATCH_SIZE = 256;
    public static final Type<SyncFoodNutritionPayload> TYPE = new Type<>(ResourceLocation.parse("humannature:sync_food_nutrition"));
    public static final StreamCodec<ByteBuf, SyncFoodNutritionPayload> STREAM_CODEC = StreamCodec.ofMember(
            (payload, buffer) -> {
                buffer.writeBoolean(payload.first());
                buffer.writeBoolean(payload.last());
                buffer.writeInt(payload.values().size());
                payload.values().forEach((id, gains) -> {
                    ResourceLocation.STREAM_CODEC.encode(buffer, id);
                    buffer.writeByte(gains.calcium());
                    buffer.writeByte(gains.carbohydrates());
                    buffer.writeByte(gains.vitamins());
                });
            }, buffer -> {
                boolean first = buffer.readBoolean();
                boolean last = buffer.readBoolean();
                int size = buffer.readInt();
                if (size < 0 || size > MAX_BATCH_SIZE) throw new IllegalArgumentException("Invalid food nutrition batch size");
                Map<ResourceLocation, NutritionValues> values = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
                    int calcium = buffer.readUnsignedByte();
                    int carbohydrates = buffer.readUnsignedByte();
                    int vitamins = buffer.readUnsignedByte();
                    if (calcium > 100 || carbohydrates > 100 || vitamins > 100) {
                        throw new IllegalArgumentException("Invalid food nutrition gain");
                    }
                    if (values.put(id, new NutritionValues(calcium, carbohydrates, vitamins)) != null) {
                        throw new IllegalArgumentException("Duplicate food nutrition item");
                    }
                }
                return new SyncFoodNutritionPayload(first, last, values);
            });

    public SyncFoodNutritionPayload {
        if (values.size() > MAX_BATCH_SIZE) throw new IllegalArgumentException("Food nutrition batch too large");
        values = Map.copyOf(values);
    }

    public static List<SyncFoodNutritionPayload> split(Map<ResourceLocation, NutritionValues> values) {
        List<Map<ResourceLocation, NutritionValues>> batches = new ArrayList<>();
        Map<ResourceLocation, NutritionValues> batch = new HashMap<>();
        for (var entry : values.entrySet()) {
            batch.put(entry.getKey(), entry.getValue());
            if (batch.size() == MAX_BATCH_SIZE) {
                batches.add(batch);
                batch = new HashMap<>();
            }
        }
        if (!batch.isEmpty() || batches.isEmpty()) batches.add(batch);
        List<SyncFoodNutritionPayload> packets = new ArrayList<>(batches.size());
        for (int i = 0; i < batches.size(); i++) {
            packets.add(new SyncFoodNutritionPayload(i == 0, i == batches.size() - 1, batches.get(i)));
        }
        return List.copyOf(packets);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
