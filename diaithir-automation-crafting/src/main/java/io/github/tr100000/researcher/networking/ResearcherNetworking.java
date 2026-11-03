package io.github.tr100000.researcher.networking;

import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.PlayerResearchTracker;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public class ResearcherNetworking {
    protected ResearcherNetworking() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ResearchUpdateS2CPacket.ID, ResearchUpdateS2CPacket.STREAM_CODEC, ResearcherClientNetworking::handleUpdate);
        registrar.playToServer(StartResearchC2SPacket.ID, StartResearchC2SPacket.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    PlayerResearchTracker researchTracker = ((net.minecraft.server.level.ServerPlayer)context.player()).researcher$getPlayerTracker();
                    researchTracker.handleUpdate(payload);
                }));
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadId(String path) {
        return new CustomPacketPayload.Type<>(ModUtils.id(path));
    }
}
