package com.alikuxac.humannature.neoforge.network;

import com.alikuxac.humannature.HumanNatureCommon;
import com.alikuxac.humannature.api.injury.LimbType;
import com.alikuxac.humannature.modules.injury.InjuryEventHandler;
import com.alikuxac.humannature.network.NetworkHandler;
import com.alikuxac.humannature.network.TreatLimbPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = HumanNatureCommon.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NeoForgeNetwork {

    public static void initClient() {
        NetworkHandler.setSender(PacketDistributor::sendToServer);
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(HumanNatureCommon.MOD_ID).versioned("1.0.0");
        registrar.playToServer(
                TreatLimbPayload.TYPE,
                TreatLimbPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        Player player = context.player();
                        if (player != null) {
                            LimbType limb = LimbType.fromIndex(payload.limbIndex());
                            ItemStack splint = InjuryEventHandler.findFirstSplint(player);
                            if (!splint.isEmpty()) {
                                InjuryEventHandler.handleQuickTreat(player, limb, splint);
                            }
                        }
                    });
                }
        );
    }
}

