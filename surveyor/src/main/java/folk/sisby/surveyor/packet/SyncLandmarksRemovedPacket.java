package folk.sisby.surveyor.packet;

import com.google.common.collect.Multimap;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.util.MapUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SyncLandmarksRemovedPacket(ResourceKey<Level> dimension, Multimap<UUID, Identifier> landmarks) implements SyncPacket {
	public static final Type<SyncLandmarksRemovedPacket> ID = new Type<>(Surveyor.id("landmarks_removed"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncLandmarksRemovedPacket> CODEC = StreamCodec.composite(
		ResourceKey.streamCodec(Registries.DIMENSION), SyncLandmarksRemovedPacket::dimension,
		ByteBufCodecs.<RegistryFriendlyByteBuf, UUID, List<Identifier>, Map<UUID, List<Identifier>>>map(HashMap::new, UUIDUtil.STREAM_CODEC, Identifier.STREAM_CODEC.apply(ByteBufCodecs.list())).map(MapUtil::asMultiMap, MapUtil::asListMap), SyncLandmarksRemovedPacket::landmarks,
		SyncLandmarksRemovedPacket::new
	);

	public static SyncLandmarksRemovedPacket of(ResourceKey<Level> dimension, UUID uuid, Identifier id) {
		return new SyncLandmarksRemovedPacket(dimension, MapUtil.asMultiMap(Map.of(uuid, List.of(id))));
	}

	@Override
	public Type<SyncLandmarksRemovedPacket> type() {
		return ID;
	}
}
