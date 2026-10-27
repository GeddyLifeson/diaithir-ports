package net.id.paradise_lost.world.feature.features;

import com.mojang.serialization.Codec;
import net.id.paradise_lost.world.feature.configs.RandomPatchConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/** Port of the removed vanilla random_patch / flower feature. */
public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
    public RandomPatchFeature(Codec<RandomPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomPatchConfiguration> context) {
        RandomPatchConfiguration config = context.config();
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
}
