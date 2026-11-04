package org.diaithir.diesel.content.modular;

import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.diaithir.diesel.content.EngineUpgrade;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;
import org.jspecify.annotations.Nullable;

/** Engine segments that join in a line: more segments, more fuel capacity and more stress from one shared tank. */
public class ModularDieselEngineBlock extends HorizontalKineticBlock implements IBE<ModularDieselEngineBlockEntity> {
    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ModularDieselEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PIPE, true).setValue(POWERED, false).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PIPE, POWERED, LIT);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction d = context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? context.getHorizontalDirection() : context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(HORIZONTAL_FACING, d);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (oldState.getBlock() == state.getBlock() || moving) super.onPlace(state, level, pos, oldState, moving);
        withBlockEntityDo(level, pos, ModularDieselEngineBlockEntity::updateConnectivity);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moving) {
        if (level.isClientSide()) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (state.getValue(POWERED) != powered) level.setBlock(pos, state.setValue(POWERED, powered), 2);
        if (level.getBlockEntity(pos) instanceof ModularDieselEngineBlockEntity be) {
            ModularDieselEngineBlockEntity c = be.getControllerBE();
            if (c != null) {
                int max = 0;
                for (int i = 0; i < c.getHeight(); i++)
                    max = Math.max(max, level.getBestNeighborSignal(c.getBlockPos().relative(c.getBlockState().getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X ? Direction.EAST : Direction.SOUTH, i)));
                c.setAnalogSignal(max);
            }
        }
        super.neighborChanged(state, level, pos, block, orientation, moving);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        EngineUpgrade up = EngineUpgrade.forItem(stack.getItem());
        if (up == EngineUpgrade.NONE) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        withBlockEntityDo(level, pos, be -> {
            ModularDieselEngineBlockEntity c = be.getControllerBE();
            if (c == null || c.upgrade != EngineUpgrade.NONE) return;
            c.upgrade = up;
            c.reActivateSource = true;
            if (!player.isCreative()) stack.shrink(1);
            c.notifyUpdate();
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (context.getClickedFace() == Direction.UP) {
            if (!level.isClientSide()) level.setBlock(pos, state.setValue(PIPE, !state.getValue(PIPE)), 3);
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof ModularDieselEngineBlockEntity be) {
            ModularDieselEngineBlockEntity c = be.getControllerBE();
            if (c != null && c.upgrade != EngineUpgrade.NONE) {
                if (!level.isClientSide()) {
                    popResource(level, pos, new ItemStack(c.upgrade.item()));
                    c.upgrade = EngineUpgrade.NONE;
                    c.reActivateSource = true;
                    c.notifyUpdate();
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean moved) {
        if (level.getBlockEntity(pos) instanceof ModularDieselEngineBlockEntity be) {
            if (be.upgrade != EngineUpgrade.NONE && be.isController()) popResource(level, pos, new ItemStack(be.upgrade.item()));
            level.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(be);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0, 0, 0, 16, 16, 16);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HORIZONTAL_FACING).getAxis() == face.getAxis();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<ModularDieselEngineBlockEntity> getBlockEntityClass() {
        return ModularDieselEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ModularDieselEngineBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get();
    }
}
