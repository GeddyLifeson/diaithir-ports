package net.diaithir.combat;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client to server requests. The server decides everything; these only say "I swung" and "I rolled this way". */
public final class Net {
    public record Swing() implements CustomPacketPayload {
        public static final Type<Swing> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirCombat.ID, "swing"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Swing> CODEC = StreamCodec.unit(new Swing());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Roll(int dir) implements CustomPacketPayload {
        public static final Type<Roll> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirCombat.ID, "roll"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Roll> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Roll::dir, Roll::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    static void register(RegisterPayloadHandlersEvent e) {
        var r = e.registrar("1").optional();
        r.playToServer(Swing.TYPE, Swing.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) Engine.swing(sp);
        }));
        r.playToServer(Roll.TYPE, Roll.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) Engine.dodge(sp, Math.floorMod(p.dir(), 4), false);
        }));
    }

    private Net() {}
}
