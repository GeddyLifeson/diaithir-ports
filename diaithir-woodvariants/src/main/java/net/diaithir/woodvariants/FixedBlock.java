package net.diaithir.woodvariants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A piece with no facing (stool, table). */
public abstract class FixedBlock extends Block {
    private final VoxelShape shape;

    protected FixedBlock(Properties properties, int[][] boxes) {
        super(properties);
        this.shape = Box.shape(boxes, net.minecraft.core.Direction.NORTH);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    public static final class Stool extends FixedBlock {
        public static final MapCodec<Stool> CODEC = simpleCodec(Stool::new);

        public Stool(Properties p) {
            super(p, Piece.STOOL_BOXES);
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return CODEC;
        }
    }

    public static final class Table extends FixedBlock {
        public static final MapCodec<Table> CODEC = simpleCodec(Table::new);

        public Table(Properties p) {
            super(p, Piece.TABLE_BOXES);
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return CODEC;
        }
    }
}
