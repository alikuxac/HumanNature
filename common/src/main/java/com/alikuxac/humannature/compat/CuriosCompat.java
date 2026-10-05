package com.alikuxac.humannature.compat;

import com.alikuxac.humannature.item.SplintItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CuriosCompat {
    private static final Supplier<Boolean> CURIOS_LOADED = Suppliers.memoize(() -> {
        try {
            Class.forName("top.theillusivec4.curios.api.CuriosApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    });

    public static boolean isCuriosLoaded() {
        return CURIOS_LOADED.get();
    }

    private static Class<?> curiosApiClass;
    private static MethodHandle getCuriosInventoryHandle;
    private static MethodHandle getStacksHandlerHandle;
    private static MethodHandle getStacksMethodHandle;

    static {
        if (CURIOS_LOADED.get()) {
            initReflection();
        }
    }

    private static void initReflection() {
        try {
            curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            
            var lookup = MethodHandles.privateLookupIn(curiosApiClass, MethodHandles.lookup());
            
            getCuriosInventoryHandle = lookup.findStatic(curiosApiClass, "getCuriosInventory",
                    MethodType.methodType(Object.class, net.minecraft.world.entity.player.Player.class));
            
            Class<?> curiosInventoryClass = Class.forName("top.theillusivec4.curios.api.CuriosApi$CuriosInventory");
            getStacksHandlerHandle = lookup.findVirtual(curiosInventoryClass, "getStacksHandler",
                    MethodType.methodType(Object.class, String.class));
            
            Class<?> stacksHandlerClass = Class.forName("top.theillusivec4.curios.api.CuriosApi$StacksHandler");
            getStacksMethodHandle = lookup.findVirtual(stacksHandlerClass, "getStacks",
                    MethodType.methodType(java.util.List.class));
            
        } catch (Exception e) {
            CURIOS_LOADED.get(); // Force re-evaluation next time
        }
    }

    public static boolean hasActiveSplint(Player player) {
        return getSplintStacks(player).stream().anyMatch(SplintItem::isValidSplint);
    }

    public static List<ItemStack> getSplintStacks(Player player) {
        List<ItemStack> splints = new ArrayList<>();
        
        // Check Curios feet slot
        if (isCuriosLoaded()) {
            try {
                Object curiosInventory = getCuriosInventoryHandle.invokeExact(player);
                if (curiosInventory != null) {
                    Object stacksHandler = getStacksHandlerHandle.invokeExact(curiosInventory, "feet");
                    if (stacksHandler != null) {
                        @SuppressWarnings("unchecked")
                        java.util.List<ItemStack> stacks = (java.util.List<ItemStack>) getStacksMethodHandle.invokeExact(stacksHandler);
                        if (stacks != null) {
                            for (ItemStack stack : stacks) {
                                if (SplintItem.isValidSplint(stack)) {
                                    splints.add(stack);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        // Check Curios splint slot (custom slot)
        if (isCuriosLoaded()) {
            try {
                Object curiosInventory = getCuriosInventoryHandle.invokeExact(player);
                if (curiosInventory != null) {
                    Object stacksHandler = getStacksHandlerHandle.invokeExact(curiosInventory, "splint");
                    if (stacksHandler != null) {
                        @SuppressWarnings("unchecked")
                        java.util.List<ItemStack> stacks = (java.util.List<ItemStack>) getStacksMethodHandle.invokeExact(stacksHandler);
                        if (stacks != null) {
                            for (ItemStack stack : stacks) {
                                if (SplintItem.isValidSplint(stack)) {
                                    splints.add(stack);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        
        // Check offhand - get actual stack from inventory
        ItemStack offhand = player.getInventory().offhand.get(0);
        if (SplintItem.isValidSplint(offhand)) {
            splints.add(offhand);
        }
        
        return splints;
    }

    public static ItemStack getSplintStack(Player player) {
        List<ItemStack> splints = getSplintStacks(player);
        return splints.isEmpty() ? ItemStack.EMPTY : splints.get(0);
    }
}
