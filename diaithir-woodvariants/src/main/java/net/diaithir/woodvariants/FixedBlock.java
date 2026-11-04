package net.diaithir.woodvariants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The stool: no facing, and you can sit on it. */
public final class FixedBlock extends Block implements Seat {
    public static final MapCodec<FixedBlock> CODEC = simpleCodec(FixedBlock::new);
    private final VoxelShape shape = Box.shape(Piece.STOOL_BOXES, Direction.NORTH);

    public FixedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public double seatHeight() {
        return SEAT_TOP;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return Seats.sit(level, pos, player, seatHeight(), player.getYRot());
    }
}
