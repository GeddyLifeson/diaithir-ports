package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.minigame.SessionManager;
import net.minecraft.util.Util;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;


/**
 * A batch of knife positions (12-bit fixed point per axis). {@code begins} places the knife at the first point;
 * {@code ends} lifts it after the last. Points only: the server replays them and computes everything else.
 */
public record FleshStrokePacket(int sessionId, int sequence, boolean begins, boolean ends, int clientMs, short[] xs, short[] ys) implements CustomPacketPayload {
    public static final Type<FleshStrokePacket> TYPE = new Type<>(ImmersiveTanning.id("flesh_stroke"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FleshStrokePacket> STREAM_CODEC = StreamCodec.ofMember(FleshStrokePacket::encode, FleshStrokePacket::decode);

    @Override
    public Type<FleshStrokePacket> type() {
        return TYPE;
    }

    public static final int MAX_POINTS = 64;

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
        buf.writeVarInt(sequence);
        buf.writeBoolean(begins);
        buf.writeBoolean(ends);
        buf.writeVarInt(Math.max(0, clientMs));
        int n = Math.min(xs.length, MAX_POINTS);
        buf.writeVarInt(n);
        for (int i = 0; i < n; i++) {
            buf.writeShort(xs[i]);
            buf.writeShort(ys[i]);
        }
    }

    public static FleshStrokePacket decode(RegistryFriendlyByteBuf buf) {
        int session = buf.readVarInt();
        int sequence = buf.readVarInt();
        boolean begins = buf.readBoolean();
        boolean ends = buf.readBoolean();
        int clientMs = buf.readVarInt();
        int n = Math.min(buf.readVarInt(), MAX_POINTS);
        short[] xs = new short[n];
        short[] ys = new short[n];
        for (int i = 0; i < n; i++) {
            xs[i] = buf.readShort();
            ys[i] = buf.readShort();
        }
        return new FleshStrokePacket(session, sequence, begins, ends, clientMs, xs, ys);
    }

    public void handle(IPayloadContext ctx) {
        long arrival = Util.getMillis();
        ServerPlayer player = (ServerPlayer) ctx.player();
        ctx.enqueueWork(() -> SessionManager.fleshStroke(player, this, arrival));
    }
}
