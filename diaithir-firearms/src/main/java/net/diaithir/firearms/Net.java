package net.diaithir.firearms;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client to server: "reload". Everything else (fire, damage, ammo) is decided on the server. */
public final class Net {
    public record Reload() implements CustomPacketPayload {
        public static final Type<Reload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, "reload"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Reload> CODEC = StreamCodec.unit(new Reload());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    static void register(RegisterPayloadHandlersEvent e) {
        e.registrar("1").optional().playToServer(Reload.TYPE, Reload.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) Engine.reload(sp);
        }));
    }

    private Net() {}
}
