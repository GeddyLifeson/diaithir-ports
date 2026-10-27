package cn.leolezury.eternalstarlight.common.mixin;

import cn.leolezury.eternalstarlight.common.handler.ESCommonSetupHandler;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(TagLoader.class)
public abstract class TagLoaderMixin {
	@Shadow
	@Final
	private String directory;

	@Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("RETURN"))
	public <T> void build(Map<Identifier, List<TagLoader.EntryWithSource>> map, CallbackInfoReturnable<Map<Identifier, Collection<T>>> cir) {
		if (Registries.tagsDirPath(Registries.ITEM).equals(directory)) {
			for (Map.Entry<TagKey<Item>, List<TagKey<Item>>> entry : ESCommonSetupHandler.ITEM_TAG_EXCLUSIONS.entrySet()) {
				Map<Identifier, Collection<T>> result = cir.getReturnValue();
				List<T> excluded = new ArrayList<>();
				for (TagKey<Item> tag : entry.getValue()) {
					Optional<Collection<T>> exclusion = result.entrySet().stream().filter(e -> e.getKey().equals(tag.location())).findFirst().map(Map.Entry::getValue);
					exclusion.ifPresent(excluded::addAll);
				}
				Optional<Collection<T>> vanilla = result.entrySet().stream().filter(e -> e.getKey().equals(entry.getKey().location())).findFirst().map(Map.Entry::getValue);
				if (!excluded.isEmpty() && vanilla.isPresent()) {
					Set<T> modified = new HashSet<>(vanilla.get());
					modified.removeIf(excluded::contains);
					result.put(entry.getKey().location(), ImmutableSet.copyOf(modified));
				}
			}
		}
	}
}
