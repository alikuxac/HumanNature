package com.alikuxac.humannature.api.event;

import com.alikuxac.humannature.api.nutrition.NutritionValues;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class NutritionConsumptionEventTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void usesDefaultsUntilExplicitlyOverriddenIncludingZero() {
        var defaults = new NutritionValues(25, 25, 20);
        var event = new NutritionConsumptionEvent(mock(Player.class), new ItemStack(Items.APPLE), defaults);
        assertFalse(event.isOverridden());
        assertEquals(defaults, event.getValues());

        event.setValues(new NutritionValues(0, 0, 0));
        assertTrue(event.isOverridden());
        assertEquals(new NutritionValues(0, 0, 0), event.getValues());
        assertEquals(defaults, event.getDefaultValues());
        assertThrows(NullPointerException.class, () -> event.setValues(null));
    }

    @Test
    void boundsGainsBeforeAddingToByteReserves() {
        assertEquals(new NutritionValues(0, 100, 17),
                new NutritionValues(Integer.MIN_VALUE, Integer.MAX_VALUE, 17));
    }

    @Test
    void protectsConsumedStackFromListenerMutations() {
        var stack = new ItemStack(Items.APPLE, 3);
        var event = new NutritionConsumptionEvent(mock(Player.class), stack, new NutritionValues(0, 0, 20));
        stack.setCount(1);
        assertEquals(3, event.getStack().getCount());
        event.getStack().setCount(50);
        assertEquals(3, event.getStack().getCount());
        assertEquals(1, stack.getCount());
    }

    @Test
    void unregisterRestoresFallbackForLaterConsumptions() {
        Consumer<NutritionConsumptionEvent> listener = event -> event.setValues(new NutritionValues(3, 7, 11));
        HumanNatureEvents.registerNutritionConsumptionListener(listener);
        try {
            var event = new NutritionConsumptionEvent(mock(Player.class), new ItemStack(Items.APPLE), new NutritionValues(0, 0, 20));
            HumanNatureEvents.postNutritionConsumption(event);
            assertEquals(new NutritionValues(3, 7, 11), event.getValues());
        } finally {
            HumanNatureEvents.unregisterNutritionConsumptionListener(listener);
        }
        var next = new NutritionConsumptionEvent(mock(Player.class), new ItemStack(Items.APPLE), new NutritionValues(0, 0, 20));
        HumanNatureEvents.postNutritionConsumption(next);
        assertFalse(next.isOverridden());
        assertEquals(new NutritionValues(0, 0, 20), next.getValues());
    }
}
