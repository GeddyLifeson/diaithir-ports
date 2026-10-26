package com.zurrtum.create.content.kinetics.motor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Cuibhleseòlmara, the tide wheel: burns magic reagents (tag diaithir_integration:magic_fuel) and turns a shaft. Four tiers: the same wheel in
 * brass, gold and silver trim turns faster (32, 64, 128, 256 RPM), burns faster (1, 2, 4, 8 times) and carries more stress per RPM.
 */
public class TideWheelBlock extends DirectionalKineticBlock implements IBE<TideWheelBlockEntity> {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    /** Tier 1 to 4: RPM, burn speed and stress capacity per RPM are in {@link #TIERS}. */
    public static final int[][] TIERS = {{32, 1, 128}, {64, 2, 192}, {128, 4, 256}, {256, 8, 384}};

    private final int tier;

    public TideWheelBlock(Properties properties) {
        this(properties, 1);
    }

    public TideWheelBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    public int tier() {
        return tier;
    }

    public int speed() {
        return TIERS[tier - 1][0];
    }

    public int burnPerTick() {
        return TIERS[tier - 1][1];
    }

    public float stressPerRpm() {
        return TIERS[tier - 1][2];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.MOTOR_BLOCK.get(state.getValue(FACING));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) || preferred == null) {
            return super.getStateForPlacement(context);
        }
        return defaultBlockState().setValue(FACING, preferred);
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (!TideWheelBlockEntity.isFuel(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return onBlockEntityUse(level, pos, be -> be.feed(player, stack) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<TideWheelBlockEntity> getBlockEntityClass() {
        return TideWheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TideWheelBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TIDE_WHEEL;
    }

}
