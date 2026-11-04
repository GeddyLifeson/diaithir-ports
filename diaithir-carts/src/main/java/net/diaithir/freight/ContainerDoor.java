package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A door cell at the end of a container. Closed it is a solid block; open it is invisible and walk-through but still part of the container. Use it to swing all four. */
public final class ContainerDoor extends Block {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public ContainerDoor(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(ContainerWall.PAINT, Paint.RED).setValue(OPEN, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(ContainerWall.PAINT, OPEN); }

    @Override protected RenderShape getRenderShape(BlockState s) { return s.getValue(OPEN) ? RenderShape.INVISIBLE : RenderShape.MODEL; }

    @Override
    protected VoxelShape getCollisionShape(BlockState s, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return s.getValue(OPEN) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            ContainerCoreEntity core = Containers.find(sl, pos);
            if (core == null) return InteractionResult.PASS;
            Containers.setDoor(sl, core, !state.getValue(OPEN));
        }
        return InteractionResult.SUCCESS;
    }
}
