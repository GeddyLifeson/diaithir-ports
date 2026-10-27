package cn.leolezury.eternalstarlight.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record ESPaintingVariant(int width, int height, Identifier texture, Identifier backTexture, boolean sidesFromPainting) {
	public static final Codec<ESPaintingVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ExtraCodecs.intRange(1, 16).fieldOf("width").forGetter(ESPaintingVariant::width),
		ExtraCodecs.intRange(1, 16).fieldOf("height").forGetter(ESPaintingVariant::height),
		Identifier.CODEC.fieldOf("texture").forGetter(ESPaintingVariant::texture),
		Identifier.CODEC.fieldOf("back_texture").forGetter(ESPaintingVariant::backTexture),
		Codec.BOOL.optionalFieldOf("sides_from_painting", false).forGetter(ESPaintingVariant::sidesFromPainting)
	).apply(instance, ESPaintingVariant::new));

	public int area() {
		return this.width() * this.height();
	}
}
