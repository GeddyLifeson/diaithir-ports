package cn.leolezury.eternalstarlight.common.entity.living.npc.boarwarf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

public record BoarwarfType(Holder<Biome> biome, Identifier texture, Identifier textureFull) {
	public static final Codec<BoarwarfType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Biome.CODEC.fieldOf("biome").forGetter(BoarwarfType::biome),
		Identifier.CODEC.fieldOf("texture").forGetter(BoarwarfType::texture)
	).apply(instance, BoarwarfType::new));

	public BoarwarfType(Holder<Biome> biome, Identifier texture) {
		this(biome, texture, fullTextureId(texture));
	}

	private static Identifier fullTextureId(Identifier location) {
		return location.withPath((string) -> "textures/" + string + ".png");
	}
}
