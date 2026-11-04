package org.diaithir.archimedes.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.diaithir.archimedes.ShipAssembler;

/** The helm. Right-click to turn the connected blocks into a ship and take the wheel. */
public class ShipMarkerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ShipMarkerBlock> CODEC = simpleCodec(ShipMarkerBlock::new);

    public ShipMarkerBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var result = ShipAssembler.assemble((ServerLevel) level, pos);
        player.sendOverlayMessage(Component.translatable("archimedes.assemble." + result.code().name().toLowerCase()));
        if (result.ship() != null) player.startRiding(result.ship());
        return InteractionResult.CONSUME;
    }
}
