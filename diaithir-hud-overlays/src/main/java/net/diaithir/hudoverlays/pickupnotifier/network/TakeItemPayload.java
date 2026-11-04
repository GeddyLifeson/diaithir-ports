// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: Puzzles Lib message replaced by a vanilla CustomPacketPayload.
package net.diaithir.hudoverlays.pickupnotifier.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TakeItemPayload(int itemId, int amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TakeItemPayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath("diaithirhudoverlays", "pickupnotifier_take_item"));
    public static final StreamCodec<ByteBuf, TakeItemPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TakeItemPayload::itemId,
            ByteBufCodecs.VAR_INT, TakeItemPayload::amount,
            TakeItemPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
