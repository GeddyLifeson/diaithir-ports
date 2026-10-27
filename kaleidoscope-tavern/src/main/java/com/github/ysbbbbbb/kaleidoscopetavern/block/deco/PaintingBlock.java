package com.github.ysbbbbbb.kaleidoscopetavern.block.deco;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PaintingBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<PaintingBlock> CODEC = simpleCodec(PaintingBlock::new);

    public static final EnumProperty<AttachFace> ATTACH_FACE = BlockStateProperties.ATTACH_FACE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape NORTH_SHAPE = Block.box(1, 1, 15, 15, 15, 16);
    public static final VoxelShape SOUTH_SHAPE = Block.box(1, 1, 0, 15, 15, 1);
    public static final VoxelShape WEST_SHAPE = Block.box(15, 1, 1, 16, 15, 15);
    public static final VoxelShape EAST_SHAPE = Block.box(0, 1, 1, 1, 15, 15);
    public static final VoxelShape FLOOR_SHAPE = Block.box(1, 0, 1, 15, 1, 15);
    public static final VoxelShape CEILING_SHAPE = Block.box(1, 15, 1, 15, 16, 15);

    public PaintingBlock(Identifier id) {
        super(Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.HAT)
                .strength(0.8F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .overrideDescription("block.kaleidoscope_tavern.painting"));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ATTACH_FACE, AttachFace.WALL)
                .setValue(WATERLOGGED, false));
    }

    public PaintingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                  BlockPos pos, Direction direction, BlockPos neighborPos,
                                  BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACH_FACE, WATERLOGGED);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        AttachFace attachFace = AttachFace.WALL;
        if (clickedFace.getAxis().isVertical()) {
            attachFace = clickedFace == Direction.UP ? AttachFace.FLOOR : AttachFace.CEILING;
            Direction horizontalDirection = context.getHorizontalDirection();
            clickedFace = clickedFace == Direction.UP ? horizontalDirection.getOpposite() : horizontalDirection;
        }
        boolean waterLogged = context.getLevel().isWaterAt(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, clickedFace)
                .setValue(ATTACH_FACE, attachFace)
                .setValue(WATERLOGGED, waterLogged);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        AttachFace attachFace = pState.getValue(ATTACH_FACE);
        if (attachFace == AttachFace.FLOOR) {
            return FLOOR_SHAPE;
        } else if (attachFace == AttachFace.CEILING) {
            return CEILING_SHAPE;
        }
        return switch (pState.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
