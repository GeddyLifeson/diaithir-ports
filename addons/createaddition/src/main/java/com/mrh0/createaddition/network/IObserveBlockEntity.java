package com.mrh0.createaddition.network;

import net.minecraft.server.level.ServerPlayer;

public interface IObserveBlockEntity {
	public void onObserved(ServerPlayer player, ObservePacketPayload pkt);
}
