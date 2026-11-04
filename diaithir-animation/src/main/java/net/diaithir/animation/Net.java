package net.diaithir.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Server to client: play a one-shot clip on a player. Client to server: inspect request (the server checks the held item). */
public final class Net {
    public record Play(int entity, String clip) implements CustomPacketPayload {
        public static final Type<Play> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "play"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Play> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Play::entity, ByteBufCodecs.STRING_UTF8, Play::clip, Play::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Inspect() implements CustomPacketPayload {
        public static final Type<Inspect> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "inspect"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Inspect> CODEC = StreamCodec.unit(new Inspect());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    static void register(RegisterPayloadHandlersEvent e) {
        var r = e.registrar("1").optional();
        r.playToClient(Play.TYPE, Play.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.getEntity(p.entity()) instanceof net.minecraft.world.entity.Avatar a) AnimClient.oneShot(a, p.clip());
        }));
        r.playToServer(Inspect.TYPE, Inspect.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) Anim.gun(sp, "inspect");
        }));
    }

    private Net() {}
}
