package net.diaithir.wiretech.circuit;

import net.diaithir.wiretech.DiaithirWiretech;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Screen traffic: the client sends a program, the server sends what the block sees and says. */
public final class CircuitPayloads {
    private CircuitPayloads() {}

    public record Set(BlockPos pos, String program) implements CustomPacketPayload {
        public static final Type<Set> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirWiretech.ID, "circuit_set"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Set> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Set::pos, ByteBufCodecs.stringUtf8(300), Set::program, Set::new);

        @Override
        public Type<Set> type() {
            return TYPE;
        }
    }

    public record View(BlockPos pos, String program, String red, String green, String out, String msg, boolean resetProgram) implements CustomPacketPayload {
        public static final Type<View> TYPE = new Type<>(Identifier.fromNamespaceAndPath(DiaithirWiretech.ID, "circuit_view"));
        public static final StreamCodec<RegistryFriendlyByteBuf, View> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, View::pos, ByteBufCodecs.stringUtf8(300), View::program, ByteBufCodecs.stringUtf8(1500), View::red,
                ByteBufCodecs.stringUtf8(1500), View::green, ByteBufCodecs.stringUtf8(1500), View::out, ByteBufCodecs.stringUtf8(300), View::msg,
                ByteBufCodecs.BOOL, View::resetProgram, View::new);

        @Override
        public Type<View> type() {
            return TYPE;
        }
    }
}
