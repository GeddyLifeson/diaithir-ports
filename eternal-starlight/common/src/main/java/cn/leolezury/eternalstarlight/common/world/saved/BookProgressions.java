package cn.leolezury.eternalstarlight.common.world.saved;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class BookProgressions extends SavedData {
	private static final String TAG_PROGRESSIONS = "progressions";

	private final Map<UUID, Set<Identifier>> progressions = new HashMap<>();

	public Map<UUID, Set<Identifier>> getProgressions() {
		return progressions;
	}

	public static SavedData.Factory<BookProgressions> factory(ServerLevel serverLevel) {
		return new SavedData.Factory<>(BookProgressions::new, (compoundTag, lookup) -> load(serverLevel, compoundTag), null);
	}

	public static BookProgressions load(ServerLevel serverLevel, CompoundTag compoundTag) {
		BookProgressions progression = new BookProgressions();
		if (compoundTag.contains(TAG_PROGRESSIONS)) {
			ProgressionInstance.LIST_CODEC.parse(NbtOps.INSTANCE, compoundTag.get(TAG_PROGRESSIONS))
				.resultOrPartial(s -> EternalStarlight.LOGGER.warn("Failed to parse Book Progressions: {}", s))
				.ifPresent(list -> list.forEach(instance -> progression.getProgressions().put(instance.id(), Sets.newHashSet(instance.unlocked()))));
		}
		return progression;
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
		List<ProgressionInstance> progressions = getProgressions().entrySet().stream().map(entry -> new ProgressionInstance(entry.getKey(), Lists.newArrayList(entry.getValue()))).toList();
		compoundTag.store(TAG_PROGRESSIONS, ProgressionInstance.LIST_CODEC, progressions);
		return compoundTag;
	}

	private record ProgressionInstance(UUID id, List<Identifier> unlocked) {
		public static final Codec<ProgressionInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			UUIDUtil.CODEC.fieldOf("id").forGetter(ProgressionInstance::id),
			Identifier.CODEC.listOf().fieldOf("unlocked").forGetter(ProgressionInstance::unlocked)
		).apply(instance, ProgressionInstance::new));

		public static final Codec<List<ProgressionInstance>> LIST_CODEC = CODEC.listOf();
	}
}
