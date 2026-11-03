package net.diaithir.worldsys.gridkeys;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client to server request carrying one grid action. */
public record GridActionPayload(GridAction action) implements CustomPacketPayload {
    public static final Type<GridActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("diaithirworldsys", "grid_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GridActionPayload> CODEC = StreamCodec.composite(GridAction.CODEC, GridActionPayload::action, GridActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
