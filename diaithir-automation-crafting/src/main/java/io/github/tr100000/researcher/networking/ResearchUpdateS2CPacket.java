package io.github.tr100000.researcher.networking;

import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.ResearchProgress;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ResearchUpdateS2CPacket(
        boolean clearCurrent,
        Map<Identifier, ResearchProgress> progressMap,
        Map<Identifier, Research> loadedResearch,
        Optional<Identifier> currentResearching,
        List<Identifier> pinnedResearches
) implements CustomPacketPayload {
    public static final Type<ResearchUpdateS2CPacket> ID = ResearcherNetworking.payloadId("research_update");
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchUpdateS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ResearchUpdateS2CPacket::clearCurrent,
            ByteBufCodecs.map(Object2ObjectOpenHashMap::new, Identifier.STREAM_CODEC, ResearchProgress.STREAM_CODEC), ResearchUpdateS2CPacket::progressMap,
            ByteBufCodecs.map(Object2ObjectOpenHashMap::new, Identifier.STREAM_CODEC, Research.STREAM_CODEC), ResearchUpdateS2CPacket::loadedResearch,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), ResearchUpdateS2CPacket::currentResearching,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ResearchUpdateS2CPacket::pinnedResearches,
            ResearchUpdateS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
