package de.ellpeck.actuallyadditions.mod.network.packet;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record NumberToTilePacket(Identifier dimension, BlockPos pos, int playerId,
                                 double number, int numberId) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, NumberToTilePacket> CODEC = CustomPacketPayload.codec(
			NumberToTilePacket::write,
			NumberToTilePacket::new);
	public static final Type<NumberToTilePacket> ID = new Type<>(Identifier.fromNamespaceAndPath(ActuallyAdditions.MODID, "number_to_tile"));


	public NumberToTilePacket(FriendlyByteBuf buf) {
		this(
				Identifier.STREAM_CODEC.decode(buf),
				BlockPos.STREAM_CODEC.decode(buf),
				buf.readInt(),
				buf.readDouble(),
				buf.readInt()
		);
	}

	public void write(FriendlyByteBuf buf) {
		Identifier.STREAM_CODEC.encode(buf, dimension);
		BlockPos.STREAM_CODEC.encode(buf, pos);
		buf.writeInt(playerId);
		buf.writeDouble(number);
		buf.writeInt(numberId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
