package cn.leolezury.eternalstarlight.common.platform;

import cn.leolezury.eternalstarlight.common.client.resource.ESBookLoader;
import cn.leolezury.eternalstarlight.common.client.resource.ESClientResourceReloadListener;
import net.minecraft.util.Util;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.util.Iterator;
import java.util.ServiceLoader;

public interface ESClientPlatform {
	ESClientPlatform INSTANCE = Util.make(() -> {
		final ServiceLoader<ESClientPlatform> loader = ServiceLoader.load(ESClientPlatform.class);
		final Iterator<ESClientPlatform> iterator = loader.iterator();
		if (!iterator.hasNext()) {
			throw new RuntimeException("Client platform instance not found!");
		} else {
			final ESClientPlatform platform = iterator.next();
			if (iterator.hasNext()) {
				throw new RuntimeException("More than one client platform instance was found!");
			}
			return platform;
		}
	});

	// reload listeners

	default ESBookLoader createBookLoader() {
		return new ESBookLoader();
	}

	default ResourceManagerReloadListener createResourceReloadListener() {
		return new ESClientResourceReloadListener();
	}

	void sendToServer(CustomPacketPayload packet);
}
