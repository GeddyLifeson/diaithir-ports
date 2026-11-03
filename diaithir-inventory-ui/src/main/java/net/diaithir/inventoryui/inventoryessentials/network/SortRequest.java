package net.diaithir.inventoryui.inventoryessentials.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SortRequest(int containerId, int[] lockedSlots) implements CustomPacketPayload {
    public static final Type<SortRequest> TYPE = new Type<>(Identifier.fromNamespaceAndPath("inventoryui", "ie_sort_request"));
    public static final StreamCodec<ByteBuf, SortRequest> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SortRequest::containerId,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(64)).map(
                    l -> l.stream().mapToInt(Integer::intValue).toArray(),
                    a -> java.util.Arrays.stream(a).boxed().toList()),
            SortRequest::lockedSlots,
            SortRequest::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
