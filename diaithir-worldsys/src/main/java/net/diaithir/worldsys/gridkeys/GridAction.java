package net.diaithir.worldsys.gridkeys;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Actions a player can request for the open crafting grid. */
public enum GridAction {
    ROTATE,
    BALANCE,
    CLEAR;

    public static final StreamCodec<ByteBuf, GridAction> CODEC = ByteBufCodecs.idMapper(i -> values()[Math.floorMod(i, values().length)], GridAction::ordinal);
}
