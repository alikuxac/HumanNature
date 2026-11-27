package com.alikuxac.humannature.modules.physiology;

import com.alikuxac.humannature.advancement.HumanNatureAdvancements;
import com.alikuxac.humannature.advancement.HumanNatureTrigger;
import com.alikuxac.humannature.api.event.HumanNatureEvents;
import com.alikuxac.humannature.api.event.NutritionConsumptionEvent;
import com.alikuxac.humannature.api.nutrition.NutritionValues;
import com.alikuxac.humannature.api.impl.NutritionAccessorImpl;
import com.alikuxac.humannature.config.ServerConfig;
import com.alikuxac.humannature.network.SyncNutritionPayload;
import net.minecraft.SharedConstants;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NutritionEventHandlerTest {
    private final Supplier<Boolean> previousPhysiology = ServerConfig.ENABLE_PHYSIOLOGY;
    private final Supplier<Boolean> previousNutrition = ServerConfig.ENABLE_NUTRIENT_RESERVES;
    private final Supplier<Integer> previousInterval = ServerConfig.NUTRIENT_DEPLETION_INTERVAL_TICKS;
    private final Supplier<Integer> previousDepletionAmount = ServerConfig.NUTRIENT_DEPLETION_AMOUNT;
    private final Supplier<HumanNatureTrigger> previousTrigger = HumanNatureAdvancements.GAMEPLAY_EVENT;
    private Consumer<NutritionConsumptionEvent> listener;
    private ServerPlayer player;
    private PlayerPhysiologyData data;
    private HumanNatureTrigger advancement;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setUp() {
        ServerConfig.ENABLE_PHYSIOLOGY = () -> true;
        ServerConfig.ENABLE_NUTRIENT_RESERVES = () -> true;
        player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(player.level()).thenReturn(mock(ServerLevel.class));
        when(player.isAlive()).thenReturn(true);
        player.connection = mock(ServerGamePacketListenerImpl.class);
        data = PhysiologyModule.getOrCreatePhysiologyData(player.getUUID());
        advancement = mock(HumanNatureTrigger.class);
        HumanNatureAdvancements.GAMEPLAY_EVENT = () -> advancement;
    }

    @AfterEach
    void restore() {
        FoodNutritionReloadListenerTest.reload(Map.of());
        if (listener != null) HumanNatureEvents.unregisterNutritionConsumptionListener(listener);
        PhysiologyModule.clearData(player.getUUID());
        ServerConfig.ENABLE_PHYSIOLOGY = previousPhysiology;
        ServerConfig.ENABLE_NUTRIENT_RESERVES = previousNutrition;
        ServerConfig.NUTRIENT_DEPLETION_INTERVAL_TICKS = previousInterval;
        ServerConfig.NUTRIENT_DEPLETION_AMOUNT = previousDepletionAmount;
        HumanNatureAdvancements.GAMEPLAY_EVENT = previousTrigger;
    }

    @Test
    void addonCanReplaceDatapackOnce() {
        FoodNutritionReloadListenerTest.reload(Map.of("apple",
                "{\"calcium\":2,\"carbohydrates\":3,\"vitamins\":8}"));
        ItemStack apple = new ItemStack(Items.APPLE);
        NutritionEventHandler.handleItemConsumption(player, apple);
        assertReserves(2, 3, 8);
        assertSingleSync(2, 3, 8);
        clearInvocations(player.connection);
        listen(event -> {
            assertEquals(new NutritionValues(2, 3, 8), event.getDefaultValues());
            assertFalse(event.isOverridden());
            event.setValues(new NutritionValues(0, 0, 5));
        });
        NutritionEventHandler.handleItemConsumption(player, apple);
        assertReserves(2, 3, 13);
        assertSingleSync(2, 3, 13);
    }

    @Test
    void zeroDatapackValuesGiveNoNutrients() {
        FoodNutritionReloadListenerTest.reload(Map.of("apple",
                "{\"calcium\":0,\"carbohydrates\":0,\"vitamins\":0}"));
        ItemStack apple = new ItemStack(Items.APPLE);
        NutritionEventHandler.handleItemConsumption(player, apple);
        assertReserves(0, 0, 0);
        assertSingleSync(0, 0, 0);
    }

    @Test
    void foodWithoutDatapackGivesNoNutrients() {
        data.setCalcium(12);
        data.setCarbohydrates(23);
        data.setVitamins(34);
        NutritionEventHandler.handleItemConsumption(player, new ItemStack(Items.APPLE));
        assertReserves(12, 23, 34);
        assertSingleSync(12, 23, 34);
        verifyNoInteractions(advancement);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void defaultAndMissingIntervalsDepleteOnceEveryThirtySeconds(boolean missingInterval) {
        ServerConfig.NUTRIENT_DEPLETION_INTERVAL_TICKS = missingInterval ? null : previousInterval;
        ServerConfig.NUTRIENT_DEPLETION_AMOUNT = () -> 1;
        data.setCalcium(50);
        data.setCarbohydrates(60);
        data.setVitamins(70);

        for (int tick = 0; tick < 599; tick++) {
            NutritionEventHandler.handlePlayerTick(player, data);
        }
        assertReserves(50, 60, 70);
        verifyNoInteractions(player.connection);

        NutritionEventHandler.handlePlayerTick(player, data);
        assertReserves(49, 59, 69);
        assertSingleSync(49, 59, 69);

        clearInvocations(player.connection);
        for (int tick = 0; tick < 599; tick++) {
            NutritionEventHandler.handlePlayerTick(player, data);
        }
        assertReserves(49, 59, 69);
        verifyNoInteractions(player.connection);

        NutritionEventHandler.handlePlayerTick(player, data);
        assertReserves(48, 58, 68);
        assertSingleSync(48, 58, 68);
    }

    @Test
    void unrelatedListenerLeavesDatapackValuesIntact() {
        listen(event -> { });
        NutritionEventHandler.handleItemConsumption(player, configuredFood());
        assertReserves(13, 17, 19);
        assertSingleSync(13, 17, 19);
    }

    @Test
    void replacesAllDatapackGainsBeforeAnyApplicationAndSyncsOnce() {
        var calls = new AtomicInteger();
        listen(event -> {
            calls.incrementAndGet();
            assertSame(player, event.getPlayer());
            assertEquals(new NutritionValues(13, 17, 19), event.getDefaultValues());
            assertReserves(10, 20, 30);
            event.setValues(new NutritionValues(2, 0, 7));
        });
        data.setCalcium(10);
        data.setCarbohydrates(20);
        data.setVitamins(30);
        NutritionEventHandler.handleItemConsumption(player, configuredFood());
        assertEquals(1, calls.get());
        assertReserves(12, 20, 37);
        assertSingleSync(12, 20, 37);
    }

    @Test
    void explicitZeroSuppressesAllDefaultGains() {
        listen(event -> event.setValues(new NutritionValues(0, 0, 0)));
        NutritionEventHandler.handleItemConsumption(player, configuredFood());
        assertReserves(0, 0, 0);
        assertSingleSync(0, 0, 0);
        verifyNoInteractions(advancement);
    }

    @Test
    void canSupplyNutritionForAFoodWithoutDatapack() {
        listen(event -> {
            assertEquals(new NutritionValues(0, 0, 0), event.getDefaultValues());
            event.setValues(new NutritionValues(6, 8, 10));
        });
        NutritionEventHandler.handleItemConsumption(player, new ItemStack(Items.APPLE));
        assertReserves(6, 8, 10);
        assertSingleSync(6, 8, 10);
    }

    @Test
    void directAccessorMutatorsRemainDataOnlyForBothOverloads() {
        var accessor = new NutritionAccessorImpl();
        accessor.setCalcium(player, 150);
        accessor.modifyCalcium(player.getUUID(), -200);
        accessor.setCarbohydrates(player.getUUID(), 90);
        accessor.modifyCarbohydrates(player, 30);
        accessor.setVitamins(player, -10);
        accessor.modifyVitamins(player.getUUID(), 25);
        assertReserves(0, 100, 25);
        verifyNoInteractions(player.connection, advancement);
    }

    @Test
    void clampsOverrideBeforeAdditionWithoutOverflowOrNegativeReserves() {
        data.setCalcium(95);
        data.setCarbohydrates(96);
        data.setVitamins(97);
        listen(event -> event.setValues(new NutritionValues(Integer.MAX_VALUE, Integer.MIN_VALUE, 50)));
        NutritionEventHandler.handleItemConsumption(player, configuredFood());
        assertReserves(100, 96, 100);
        assertSingleSync(100, 96, 100);
        verifyNoInteractions(advancement);
    }

    @Test
    void advancementUsesFinalValuesAndOnlyTheTransitionToAllFull() {
        data.setCalcium(99);
        data.setCarbohydrates(99);
        data.setVitamins(99);
        listen(event -> event.setValues(new NutritionValues(1, 1, 0)));
        NutritionEventHandler.handleItemConsumption(player, configuredFood());
        assertReserves(100, 100, 99);
        verifyNoInteractions(advancement);
        HumanNatureEvents.unregisterNutritionConsumptionListener(listener);
        listen(event -> event.setValues(new NutritionValues(0, 0, 1)));
        NutritionEventHandler.handleItemConsumption(player, configuredFood());
        NutritionEventHandler.handleItemConsumption(player, configuredFood());
        assertReserves(100, 100, 100);
        verify(advancement, times(1)).trigger(player, "max_nutrition");
        verifyNoMoreInteractions(advancement);
    }

    @ParameterizedTest
    @ValueSource(strings = {"physiology-disabled", "nutrition-disabled", "creative", "spectator", "client", "null-stack", "empty-stack", "null-player"})
    void ineligibleConsumptionSkipsHookMutationSyncAndAdvancement(String reason) {
        var calls = new AtomicInteger();
        listen(event -> {
            calls.incrementAndGet();
            event.setValues(new NutritionValues(100, 100, 100));
        });
        var stack = configuredFood();
        switch (reason) {
            case "physiology-disabled" -> ServerConfig.ENABLE_PHYSIOLOGY = () -> false;
            case "nutrition-disabled" -> ServerConfig.ENABLE_NUTRIENT_RESERVES = () -> false;
            case "creative" -> when(player.isCreative()).thenReturn(true);
            case "spectator" -> when(player.isSpectator()).thenReturn(true);
            case "client" -> {
                var level = mock(Level.class);
                when(level.isClientSide()).thenReturn(true);
                when(player.level()).thenReturn(level);
            }
            case "null-stack" -> stack = null;
            case "empty-stack" -> stack = ItemStack.EMPTY;
        }
        NutritionEventHandler.handleItemConsumption(reason.equals("null-player") ? null : player, stack);
        assertEquals(0, calls.get());
        assertReserves(0, 0, 0);
        verifyNoInteractions(player.connection, advancement);
    }

    private void listen(Consumer<NutritionConsumptionEvent> consumer) {
        listener = consumer;
        HumanNatureEvents.registerNutritionConsumptionListener(listener);
    }

    private ItemStack configuredFood() {
        FoodNutritionReloadListenerTest.reload(Map.of("apple",
                "{\"calcium\":13,\"carbohydrates\":17,\"vitamins\":19}"));
        return new ItemStack(Items.APPLE);
    }

    private void assertReserves(int calcium, int carbs, int vitamins) {
        assertEquals(calcium, data.getCalcium());
        assertEquals(carbs, data.getCarbohydrates());
        assertEquals(vitamins, data.getVitamins());
    }

    private void assertSingleSync(int calcium, int carbs, int vitamins) {
        var packet = ArgumentCaptor.forClass(ClientboundCustomPayloadPacket.class);
        verify(player.connection, times(1)).send(packet.capture());
        assertEquals(new SyncNutritionPayload((byte) calcium, (byte) carbs, (byte) vitamins), packet.getValue().payload());
        verifyNoMoreInteractions(player.connection);
    }
}
