package org.diaithir.diesel.content.canister;

import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;
import org.jspecify.annotations.Nullable;

public class CanisterBlock extends Block implements IBE<CanisterBlockEntity>, IWrenchable {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public CanisterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING).getAxis()) {
            case Y -> Block.box(2, 0, 2, 14, 16, 14);
            case X -> Block.box(0, 2, 2, 16, 14, 14);
            default -> Block.box(2, 2, 0, 14, 14, 16);
        };
    }

    @Override
    public Class<CanisterBlockEntity> getBlockEntityClass() {
        return CanisterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CanisterBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.CANISTER.get();
    }
}
