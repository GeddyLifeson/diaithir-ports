package cn.leolezury.eternalstarlight.neoforge.platform;

import cn.leolezury.eternalstarlight.common.platform.ESClientPlatform;
import cn.leolezury.eternalstarlight.neoforge.network.ESNeoNetworkHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class ESNeoClientPlatform implements ESClientPlatform {
	@Override
	public void sendToServer(CustomPacketPayload packet) {
		ESNeoNetworkHandler.sendToServer(packet);
	}
}
