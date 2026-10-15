package vectorwing.farmersdelight.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.IShearable;
import vectorwing.farmersdelight.common.world.WildCropGeneration;

public class SandyShrubBlock extends VegetationBlock implements IShearable, BonemealableBlock
{
	public static final MapCodec<SandyShrubBlock> CODEC = simpleCodec(SandyShrubBlock::new);

	@Override
	protected MapCodec<? extends VegetationBlock> codec() {
		return CODEC;
	}

	protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

	public SandyShrubBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(BlockTags.SAND);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		// 26.1 dropped random_patch, so the old patch spread (32 tries, 2 across, 3 up or down) is done here
		level.registryAccess().lookup(Registries.CONFIGURED_FEATURE).flatMap((value) -> value.get(WildCropGeneration.FEATURE_PATCH_SANDY_SHRUB)).ifPresent(feature -> {
			for (int i = 0; i < 32; i++) {
				BlockPos target = pos.offset(random.nextInt(5) - 2, random.nextInt(7) - 3, random.nextInt(5) - 2);
				if (level.isEmptyBlock(target) && level.getBlockState(target.below()).is(BlockTags.SAND)) {
					feature.value().place(level, level.getChunkSource().getGenerator(), random, target);
				}
			}
		});
	}
}
