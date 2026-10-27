package cn.leolezury.eternalstarlight.common.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** PORT: vanilla removed Feature.RANDOM_PATCH in 26.1 (patches are placement modifiers now); this keeps the old behaviour for the mod's own data. */
public class ESRandomPatchFeature extends Feature<ESRandomPatchFeature.Configuration> {
	public ESRandomPatchFeature(Codec<Configuration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<Configuration> context) {
		Configuration config = context.config();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		WorldGenLevel level = context.level();
		int placed = 0;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int xz = config.xzSpread() + 1;
		int y = config.ySpread() + 1;
		for (int i = 0; i < config.tries(); i++) {
			pos.setWithOffset(origin, random.nextInt(xz) - random.nextInt(xz), random.nextInt(y) - random.nextInt(y), random.nextInt(xz) - random.nextInt(xz));
			if (config.feature().value().place(level, context.chunkGenerator(), random, pos)) {
				placed++;
			}
		}
		return placed > 0;
	}

	public record Configuration(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements FeatureConfiguration {
		public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter(Configuration::tries),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(Configuration::xzSpread),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(Configuration::ySpread),
			PlacedFeature.CODEC.fieldOf("feature").forGetter(Configuration::feature)
		).apply(instance, Configuration::new));
	}
}
