package folk.sisby.surveyor.packet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.util.MapUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SyncLandmarksAddedPacket(ResourceKey<Level> dimension, Table<UUID, Identifier, Landmark> landmarks) implements SyncPacket {
	public static final Type<SyncLandmarksAddedPacket> ID = new Type<>(Surveyor.id("landmarks_added"));
	public static final StreamCodec<ByteBuf, SyncLandmarksAddedPacket> CODEC = StreamCodec.composite(
		ResourceKey.streamCodec(Registries.DIMENSION), SyncLandmarksAddedPacket::dimension,
		SurveyorPacketCodecs.LANDMARK_SUMMARIES, SyncLandmarksAddedPacket::landmarks,
		SyncLandmarksAddedPacket::new
	);

	public static SyncLandmarksAddedPacket of(Multimap<UUID, Identifier> keySet, WorldLandmarks summary) {
		return summary.createUpdatePacket(keySet);
	}

	@Override
	public List<SurveyorPacket> toPayloads(RegistryAccess registryManager) {
		List<SurveyorPacket> payloads = new ArrayList<>();
		FriendlyByteBuf buf = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.buffer()), registryManager);
		CODEC.encode(buf, this);
		if (buf.readableBytes() < MAX_PAYLOAD_SIZE) {
			payloads.add(this);
		} else {
			Multimap<UUID, Identifier> keySet = MapUtil.keyMultiMap(landmarks);
			if (keySet.size() == 1) {
				Surveyor.LOGGER.error("Couldn't create a landmark update packet for {} at {} - an individual landmark would be too large to send!", keySet.keys().stream().findFirst().orElseThrow(), keySet.values().stream().findFirst().orElseThrow());
				return List.of();
			}
			Multimap<UUID, Identifier> firstHalf = HashMultimap.create();
			Multimap<UUID, Identifier> secondHalf = HashMultimap.create();
			keySet.forEach((key, pos) -> {
				if (firstHalf.size() < keySet.size() / 2) {
					firstHalf.put(key, pos);
				} else {
					secondHalf.put(key, pos);
				}
			});
			payloads.addAll(new SyncLandmarksAddedPacket(dimension, MapUtil.splitByKeyMap(landmarks, firstHalf)).toPayloads(registryManager));
			payloads.addAll(new SyncLandmarksAddedPacket(dimension, MapUtil.splitByKeyMap(landmarks, secondHalf)).toPayloads(registryManager));
		}
		return payloads;
	}

	@Override
	public Type<SyncLandmarksAddedPacket> type() {
		return ID;
	}
}
