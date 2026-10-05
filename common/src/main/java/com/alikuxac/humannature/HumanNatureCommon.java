package com.alikuxac.humannature;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import com.alikuxac.humannature.compat.CuriosCompat;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class HumanNatureCommon {
    public static final String MOD_ID = BuildConstants.MOD_ID;
    public static final String SPLINT_SLOT = "splint";

    public static void init() {
        // Common initialization
    }

    /**
     * Registers the custom Curios slot for splints.
     * Call this during mod construction (in forge/neoforge mod class constructor).
     */
    public static void registerCuriosSlots(IEventBus modBus) {
        if (!CuriosCompat.isCuriosLoaded()) {
            return;
        }

        // Register the splint slot type using the correct Curios 9.5+ API
        // In Curios 9.5+, slot types are registered with a ResourceLocation and a SlotTypeMessage
        try {
            Class<?> resourceLocationClass = Class.forName("net.minecraft.resources.ResourceLocation");
            Constructor<?> rlConstructor = resourceLocationClass.getConstructor(String.class, String.class);
            Object identifier = rlConstructor.newInstance(MOD_ID, SPLINT_SLOT);

            // Use reflection to call the correct registerSlotType method
            Class<?> slotTypeMessageClass = Class.forName("top.theillusivec4.curios.api.type.capability.SlotTypeMessage");
            Method builderMethod = slotTypeMessageClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);

            Method sizeMethod = builder.getClass().getMethod("size", int.class);
            sizeMethod.invoke(builder, 1);

            // Add icon for the Curios GUI - use the splint item texture
            try {
                Method iconMethod = builder.getClass().getMethod("icon", resourceLocationClass);
                Object iconId = rlConstructor.newInstance(MOD_ID, "item/splint");
                iconMethod.invoke(builder, iconId);
            } catch (Exception ignored) {
                // Icon is optional
            }

            Method buildMethod = builder.getClass().getMethod("build");
            Object slotTypeMessage = buildMethod.invoke(builder);

            Method registerMethod = CuriosApi.class.getMethod("registerSlotType", resourceLocationClass, slotTypeMessageClass);
            registerMethod.invoke(null, identifier, slotTypeMessage);
        } catch (Exception e) {
            // Log error but do not crash
            e.printStackTrace();
        }
    }
}
