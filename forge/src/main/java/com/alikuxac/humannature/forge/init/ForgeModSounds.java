package com.alikuxac.humannature.forge.init;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.init.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgeModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HumanNatureCommon.MOD_ID);

    public static final RegistryObject<SoundEvent> HEARTBEAT = SOUNDS.register("heartbeat",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HumanNatureCommon.MOD_ID, "heartbeat")));

    public static void register(IEventBus eventBus) {
        ModSounds.HEARTBEAT = HEARTBEAT;
        SOUNDS.register(eventBus);
    }
}
