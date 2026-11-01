package net.diaithir.sharedspace;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Holds up to four small blocks, one in each quarter of the space. Not an item: it appears when a second small block is placed. */
public final class ClusterBlock extends BaseEntityBlock {
    public static final MapCodec<ClusterBlock> CODEC = simpleCodec(ClusterBlock::new);
    /** Light of the brightest member, because a block's light level cannot read its block entity. */
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    public ClusterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIGHT, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(LIGHT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClusterBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(level, pos, false, context);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(level, pos, true, context);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    static VoxelShape memberShape(ClusterBlockEntity be, int cell, BlockGetter level, BlockPos pos, boolean collision, CollisionContext ctx) {
        BlockState member = be.get(cell);
        if (member == null) {
            return Shapes.empty();
        }
        VoxelShape s = collision ? member.getCollisionShape(level, pos, ctx) : member.getShape(level, pos, ctx);
        return s.move(Cells.offsetX(cell), 0, Cells.offsetZ(cell));
    }

    private static VoxelShape shape(BlockGetter level, BlockPos pos, boolean collision, CollisionContext ctx) {
        if (!(level.getBlockEntity(pos) instanceof ClusterBlockEntity be)) {
            return Shapes.block();
        }
        VoxelShape all = Shapes.empty();
        for (int i = 0; i < Cells.COUNT; i++) {
            all = Shapes.or(all, memberShape(be, i, level, pos, collision, ctx));
        }
        return all;
    }
}
