package net.diaithir.worldsys.packversioncheck;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client-to-server payload carrying the client pack version. */
public record PackVersionPayload(String version) implements CustomPacketPayload {
    public static final Type<PackVersionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("diaithirworldsys", "pack_version"));
    public static final StreamCodec<ByteBuf, PackVersionPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(PackVersionPayload::new, PackVersionPayload::version);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
