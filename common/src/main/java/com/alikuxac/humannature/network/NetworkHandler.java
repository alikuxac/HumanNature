package com.alikuxac.humannature.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.function.Consumer;

public class NetworkHandler {
    private static Consumer<CustomPacketPayload> sender = null;

    public static void setSender(Consumer<CustomPacketPayload> packetSender) {
        sender = packetSender;
    }

    public static void sendToServer(CustomPacketPayload payload) {
        if (sender != null && payload != null) {
            sender.accept(payload);
        }
    }
}

