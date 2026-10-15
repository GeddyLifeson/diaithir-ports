package de.ellpeck.actuallyadditions.mod.network.packet;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ButtonToContainerPacket(Identifier dimension, int playerId, int buttonId) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ButtonToContainerPacket> CODEC = CustomPacketPayload.codec(
			ButtonToContainerPacket::write,
			ButtonToContainerPacket::new);
	public static final Type<ButtonToContainerPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(ActuallyAdditions.MODID, "button_to_container"));


	public ButtonToContainerPacket(FriendlyByteBuf buf) {
		this(
				Identifier.STREAM_CODEC.decode(buf),
				buf.readInt(),
				buf.readInt()
		);
	}

	public void write(FriendlyByteBuf buf) {
		Identifier.STREAM_CODEC.encode(buf, dimension);
		buf.writeInt(playerId);
		buf.writeInt(buttonId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
