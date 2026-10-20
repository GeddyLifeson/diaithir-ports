package net.diaithir.compat;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A wild plant: a cross-model plant that stands on dirt, sand, gravel, stone, terracotta and snow (tundra and steppe ground). */
public class WildPlantBlock extends VegetationBlock {
    public static final MapCodec<WildPlantBlock> CODEC = simpleCodec(WildPlantBlock::new);
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 12.0);

    public WildPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.TERRACOTTA) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.FARMLAND);
    }
}
