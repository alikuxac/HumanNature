package com.alikuxac.humannature.neoforge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.init.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NeoForgeModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, HumanNatureCommon.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT = SOUNDS.register("heartbeat",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "heartbeat")));

    public static void register(IEventBus eventBus) {
        ModSounds.HEARTBEAT = HEARTBEAT;
        SOUNDS.register(eventBus);
    }
}
