package net.diaithir.aurorian;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;

/** A boss-arena wall you can see through: it pushes everything but creative players back out. */
public class FogWallBlock extends HorizontalDirectionalBlock {
    private static final double FORCE = 0.20;

    public FogWallBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(FogWallBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity e, InsideBlockEffectApplier a, boolean precise) {
        if (e instanceof Player p && p.isCreative()) {
            return;
        }
        Vec3 v = e.getDeltaMovement();
        Direction f = state.getValue(FACING);
        e.setDeltaMovement(v.x - f.getStepX() * -FORCE, v.y, v.z - f.getStepZ() * -FORCE);
    }
}
