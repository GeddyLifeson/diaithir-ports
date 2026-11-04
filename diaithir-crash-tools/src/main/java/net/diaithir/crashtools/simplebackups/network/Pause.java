package net.diaithir.crashtools.simplebackups.network;

import net.diaithir.crashtools.simplebackups.SimpleBackups;
import net.diaithir.crashtools.simplebackups.client.ClientEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record Pause(boolean pause) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(SimpleBackups.MODID, "pause");
    public static final CustomPacketPayload.Type<Pause> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, Pause> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Pause::pause, Pause::new
    );

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return Pause.TYPE;
    }

    public void handle(IPayloadContext context) {
        ClientEventHandler.setPaused(this.pause);
    }
}
