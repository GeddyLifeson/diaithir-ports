package cn.leolezury.eternalstarlight.common.entity.living.animal;

import cn.leolezury.eternalstarlight.common.data.ESRegistries;
import cn.leolezury.eternalstarlight.common.data.ESShimmerLacewingVariants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import java.util.Objects;
import java.util.Optional;

public record ShimmerLacewingVariant(Identifier texture, Identifier textureFull, Identifier glowTexture, Identifier glowTextureFull, boolean skyFlying, HolderSet<Biome> biomes) {
	public static final Codec<ShimmerLacewingVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("texture").forGetter(ShimmerLacewingVariant::texture),
		Identifier.CODEC.fieldOf("glow_texture").forGetter(ShimmerLacewingVariant::glowTexture),
		Codec.BOOL.optionalFieldOf("sky_flying", false).forGetter(ShimmerLacewingVariant::skyFlying),
		RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(ShimmerLacewingVariant::biomes)
	).apply(instance, ShimmerLacewingVariant::new));

	public ShimmerLacewingVariant(Identifier texture, Identifier glowTexture, HolderSet<Biome> biomes) {
		this(texture, glowTexture, false, biomes);
	}

	public ShimmerLacewingVariant(Identifier texture, Identifier glowTexture, boolean skyFlying, HolderSet<Biome> biomes) {
		this(texture, fullTextureId(texture), glowTexture, fullTextureId(glowTexture), skyFlying, biomes);
	}

	private static Identifier fullTextureId(Identifier location) {
		return location.withPath((string) -> "textures/" + string + ".png");
	}

	public static Holder<ShimmerLacewingVariant> getSpawnVariant(RegistryAccess registryAccess, Holder<Biome> holder) {
		Registry<ShimmerLacewingVariant> registry = registryAccess.lookupOrThrow(ESRegistries.SHIMMER_LACEWING_VARIANT);
		Optional<Holder.Reference<ShimmerLacewingVariant>> optional = registry.listElements().filter(reference -> reference.value().biomes().contains(holder)).findFirst().or(() -> registry.get(ESShimmerLacewingVariants.RIVER));
		Objects.requireNonNull(registry);
		return optional.or(registry::getAny).orElseThrow();
	}
}
