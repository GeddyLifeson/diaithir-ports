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

	public static final Codec<BookProgressions> CODEC = ProgressionInstance.LIST_CODEC.xmap(list -> {
		BookProgressions progression = new BookProgressions();
		list.forEach(instance -> progression.getProgressions().put(instance.id(), Sets.newHashSet(instance.unlocked())));
		return progression;
	}, progression -> progression.getProgressions().entrySet().stream().map(entry -> new ProgressionInstance(entry.getKey(), Lists.newArrayList(entry.getValue()))).toList());
	public static final net.minecraft.world.level.saveddata.SavedDataType<BookProgressions> TYPE = new net.minecraft.world.level.saveddata.SavedDataType<>(EternalStarlight.id("book_progressions"), BookProgressions::new, CODEC, null);

	private record ProgressionInstance(UUID id, List<Identifier> unlocked) {
		public static final Codec<ProgressionInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			UUIDUtil.CODEC.fieldOf("id").forGetter(ProgressionInstance::id),
			Identifier.CODEC.listOf().fieldOf("unlocked").forGetter(ProgressionInstance::unlocked)
		).apply(instance, ProgressionInstance::new));

		public static final Codec<List<ProgressionInstance>> LIST_CODEC = CODEC.listOf();
	}
}
