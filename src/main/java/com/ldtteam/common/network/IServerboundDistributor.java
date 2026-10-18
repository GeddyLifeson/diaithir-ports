package com.ldtteam.common.network;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * List of possible network targets when sending from client to server.
 */
public interface IServerboundDistributor extends CustomPacketPayload
{
    public default void sendToServer()
    {
        ClientPacketDistributor.sendToServer(this);
    }
}
