// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: Puzzles Lib message replaced by a vanilla CustomPacketPayload.
package net.diaithir.hudoverlays.pickupnotifier.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record TakeItemStackPayload(ItemStack itemStack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TakeItemStackPayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath("diaithirhudoverlays", "pickupnotifier_take_item_stack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TakeItemStackPayload> CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, TakeItemStackPayload::itemStack,
            TakeItemStackPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
