package io.github.tr100000.researcher.networking;

import io.github.tr100000.trutils.api.utils.PacketCodecUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record StartResearchC2SPacket(Mode mode, Optional<Identifier> researchId) implements CustomPacketPayload {
    public static final Type<StartResearchC2SPacket> ID = ResearcherNetworking.payloadId("start_research");
    public static final StreamCodec<RegistryFriendlyByteBuf, StartResearchC2SPacket> STREAM_CODEC = StreamCodec.composite(
            PacketCodecUtils.ofEnum(Mode.class), StartResearchC2SPacket::mode,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), StartResearchC2SPacket::researchId,
            StartResearchC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public enum Mode {
        SET_CURRENT,
        PIN,
        UNPIN,
        GRANT,
        REVOKE,
    }
}
