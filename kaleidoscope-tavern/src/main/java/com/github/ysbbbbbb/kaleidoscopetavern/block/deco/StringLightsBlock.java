package com.github.ysbbbbbb.kaleidoscopetavern.block.deco;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class StringLightsBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<StringLightsBlock> CODEC = simpleCodec(p -> new StringLightsBlock(p, DyeColor.WHITE));
    public static final Map<Item, StringLightsBlock> TRANSFORM_MAP = Maps.newHashMap();

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape SOUTH_SHAPE = Block.box(0, 4, 0, 16, 16, 6);
    public static final VoxelShape NORTH_SHAPE = Block.box(0, 4, 10, 16, 16, 16);
    public static final VoxelShape EAST_SHAPE = Block.box(0, 4, 0, 6, 16, 16);
    public static final VoxelShape WEST_SHAPE = Block.box(10, 4, 0, 16, 16, 16);

    public final @Nullable Item dyeItem;

    public StringLightsBlock(Properties properties, @Nullable DyeColor dyeColor) {
        super(properties);
        if (dyeColor != null) {
            this.dyeItem = switch (dyeColor) {
                case ORANGE -> Items.ORANGE_DYE;
                case MAGENTA -> Items.MAGENTA_DYE;
                case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
                case YELLOW -> Items.YELLOW_DYE;
                case LIME -> Items.LIME_DYE;
                case PINK -> Items.PINK_DYE;
                case GRAY -> Items.GRAY_DYE;
                case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
                case CYAN -> Items.CYAN_DYE;
                case PURPLE -> Items.PURPLE_DYE;
                case BLUE -> Items.BLUE_DYE;
                case BROWN -> Items.BROWN_DYE;
                case GREEN -> Items.GREEN_DYE;
                case RED -> Items.RED_DYE;
                case BLACK -> Items.BLACK_DYE;
                default -> Items.WHITE_DYE;
            };
            TRANSFORM_MAP.put(dyeItem, this);
        } else {
            this.dyeItem = null;
        }
    }

    public StringLightsBlock(Identifier id, @Nullable DyeColor dyeColor) {
        this(Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .mapColor(Objects.requireNonNullElse(dyeColor, DyeColor.WHITE))
                .instrument(NoteBlockInstrument.HAT)
                .strength(0.8F)
                .sound(SoundType.CHAIN)
                .noCollision()
                .lightLevel(s -> 15), dyeColor);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
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
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hitResult) {
        Item item = stack.getItem();
        if (TRANSFORM_MAP.containsKey(item) && item != this.dyeItem) {
            BlockState transform = TRANSFORM_MAP.get(item)
                    .defaultBlockState()
                    .setValue(FACING, state.getValue(FACING))
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
            level.setBlockAndUpdate(pos, transform);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS);
            ParticleUtils.spawnParticleInBlock(level, pos, 10, ParticleTypes.HAPPY_VILLAGER);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isVertical()) {
            clickedFace = context.getHorizontalDirection().getOpposite();
        }
        boolean waterLogged = context.getLevel().isWaterAt(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, clickedFace)
                .setValue(WATERLOGGED, waterLogged);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
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
