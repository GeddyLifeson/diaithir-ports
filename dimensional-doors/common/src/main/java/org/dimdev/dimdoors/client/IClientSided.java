package org.dimdev.dimdoors.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface IClientSided {
    void onClientPlayerJoin(Runnable listener);

    <T extends CustomPacketPayload> void sendPacket(T packet);
}
