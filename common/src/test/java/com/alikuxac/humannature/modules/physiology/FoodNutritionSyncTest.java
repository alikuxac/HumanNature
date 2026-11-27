package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.network.SyncFoodNutritionPayload;
import net.minecraft.SharedConstants;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FoodNutritionSyncTest {
    private final Supplier<Boolean> previousPhysiology = ServerConfig.ENABLE_PHYSIOLOGY;
    private final Supplier<Boolean> previousNutrition = ServerConfig.ENABLE_NUTRIENT_RESERVES;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void reset() {
        ServerConfig.ENABLE_PHYSIOLOGY = previousPhysiology;
        ServerConfig.ENABLE_NUTRIENT_RESERVES = previousNutrition;
        FoodNutritionReloadListenerTest.reload(Map.of());
    }

    @Test
    void sendsLoadedServerDataToEverySelectedPlayer() {
        ServerConfig.ENABLE_PHYSIOLOGY = () -> true;
        ServerConfig.ENABLE_NUTRIENT_RESERVES = () -> true;
        FoodNutritionReloadListenerTest.reload(Map.of("apple", "{\"calcium\":0,\"carbohydrates\":3,\"vitamins\":9}"));
        var first = player();
        var second = player();
        FoodNutritionSync.sync(List.of(first, second));
        for (var player : List.of(first, second)) {
            var sent = ArgumentCaptor.forClass(ClientboundCustomPayloadPacket.class);
            verify(player.connection).send(sent.capture());
            var payload = assertInstanceOf(SyncFoodNutritionPayload.class, sent.getValue().payload());
            assertTrue(payload.first());
            assertTrue(payload.last());
            assertEquals(Map.of(ResourceLocation.parse("minecraft:apple"), new NutritionValues(0, 3, 9)), payload.values());
            verifyNoMoreInteractions(player.connection);
        }
    }

    @Test
    void disabledNutritionSendsEmptySnapshotToClearViewer() {
        ServerConfig.ENABLE_PHYSIOLOGY = () -> true;
        ServerConfig.ENABLE_NUTRIENT_RESERVES = () -> false;
        FoodNutritionReloadListenerTest.reload(Map.of("apple", "{\"calcium\":0,\"carbohydrates\":3,\"vitamins\":9}"));
        var player = player();
        FoodNutritionSync.sync(List.of(player));
        var sent = ArgumentCaptor.forClass(ClientboundCustomPayloadPacket.class);
        verify(player.connection).send(sent.capture());
        var payload = assertInstanceOf(SyncFoodNutritionPayload.class, sent.getValue().payload());
        assertTrue(payload.first());
        assertTrue(payload.last());
        assertTrue(payload.values().isEmpty());
    }

    private static ServerPlayer player() {
        var player = mock(ServerPlayer.class);
        player.connection = mock(ServerGamePacketListenerImpl.class);
        return player;
    }
}
