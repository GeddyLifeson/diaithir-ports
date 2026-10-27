package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.NetworkHelper;
import net.createmod.catnip.platform.services.PlatformHelper;

/** Shim of Catnip's service holder (catnip is embedded in the port under com.zurrtum.create.catnip and has no platform layer). */
public class CatnipServices {
    public static final PlatformHelper PLATFORM = Env::current;
    public static final NetworkHelper NETWORK = packet -> net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(packet);
}
