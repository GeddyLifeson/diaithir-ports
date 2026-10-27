package net.createmod.catnip.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface NetworkHelper {
    void sendToServer(CustomPacketPayload packet);
}
