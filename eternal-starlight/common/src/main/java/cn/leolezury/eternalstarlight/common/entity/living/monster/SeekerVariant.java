package cn.leolezury.eternalstarlight.common.entity.living.monster;

import cn.leolezury.eternalstarlight.common.data.ESRegistries;
import cn.leolezury.eternalstarlight.common.data.ESSeekerVariants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import java.util.Objects;
import java.util.Optional;

public record SeekerVariant(Identifier texture, Identifier textureFull, Identifier glowTexture, Identifier glowTextureFull, Identifier tentacleTexture, Identifier tentacleTextureFull, Identifier tentacleEndTexture, Identifier tentacleEndTextureFull, int particleColor, HolderSet<Biome> biomes) {
	public static final Codec<SeekerVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("texture").forGetter(SeekerVariant::texture),
		Identifier.CODEC.fieldOf("glow_texture").forGetter(SeekerVariant::glowTexture),
		Identifier.CODEC.fieldOf("tentacle_texture").forGetter(SeekerVariant::tentacleTexture),
		Identifier.CODEC.fieldOf("tentacle_end_texture").forGetter(SeekerVariant::tentacleEndTexture),
		Codec.INT.fieldOf("particle_color").forGetter(SeekerVariant::particleColor),
		RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(SeekerVariant::biomes)
	).apply(instance, SeekerVariant::new));

	public SeekerVariant(Identifier texture, Identifier glowTexture, Identifier tentacleTexture, Identifier tentacleEndTexture, int particleColor, HolderSet<Biome> biomes) {
		this(texture, fullTextureId(texture), glowTexture, fullTextureId(glowTexture), tentacleTexture, fullTextureId(tentacleTexture), tentacleEndTexture, fullTextureId(tentacleEndTexture), particleColor, biomes);
	}

	private static Identifier fullTextureId(Identifier location) {
		return location.withPath((string) -> "textures/" + string + ".png");
	}

	public static Holder<SeekerVariant> getSpawnVariant(RegistryAccess registryAccess, Holder<Biome> holder) {
		Registry<SeekerVariant> registry = registryAccess.registryOrThrow(ESRegistries.SEEKER_VARIANT);
		Optional<Holder.Reference<SeekerVariant>> optional = registry.holders().filter(reference -> reference.value().biomes().contains(holder)).findFirst().or(() -> registry.getHolder(ESSeekerVariants.LUNAR));
		Objects.requireNonNull(registry);
		return optional.or(registry::getAny).orElseThrow();
	}
}
