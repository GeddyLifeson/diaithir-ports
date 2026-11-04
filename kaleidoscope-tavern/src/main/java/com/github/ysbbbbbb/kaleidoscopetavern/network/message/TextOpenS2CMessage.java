package com.github.ysbbbbbb.kaleidoscopetavern.network.message;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.network.proxy.TextOpenS2CProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TextOpenS2CMessage(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TextOpenS2CMessage> TYPE = new CustomPacketPayload.Type<>(KaleidoscopeTavern.modLoc("text_open"));

    public static final StreamCodec<FriendlyByteBuf, TextOpenS2CMessage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public TextOpenS2CMessage decode(FriendlyByteBuf buf) {
            return new TextOpenS2CMessage(buf.readBlockPos());
        }

        @Override
        public void encode(FriendlyByteBuf buf, TextOpenS2CMessage msg) {
            buf.writeBlockPos(msg.pos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TextOpenS2CMessage message, IPayloadContext context) {
        context.enqueueWork(() -> TextOpenS2CProxy.onHandle(message));
    }
}
