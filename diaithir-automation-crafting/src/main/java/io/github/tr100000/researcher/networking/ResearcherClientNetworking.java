package io.github.tr100000.researcher.networking;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ResearcherClientNetworking extends ResearcherNetworking {
    private ResearcherClientNetworking() {}

    /** Runs on the network thread; only ever loaded by the client dispatcher. */
    public static void handleUpdate(ResearchUpdateS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().getConnection().researcher$getClientTracker().handleUpdate(payload));
    }
}
