package net.diaithir.diaithirfurniture;

import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FurnitureBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<FurnitureBlock> CODEC = simpleCodec(p -> new FurnitureBlock(p, List.of(new double[] {0, 0, 0, 16, 16, 16}), 0));

    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
    private final int rows;

    public FurnitureBlock(BlockBehaviour.Properties props, List<double[]> boxes, int rows) {
        super(props);
        this.rows = rows;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            VoxelShape s = Shapes.empty();
            for (double[] b : boxes) {
                s = Shapes.or(s, rotate(b, d));
            }
            shapes.put(d, s);
        }
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private static VoxelShape rotate(double[] b, Direction facing) {
        double x1 = b[0], y1 = b[1], z1 = b[2], x2 = b[3], y2 = b[4], z2 = b[5];
        double nx1, nz1, nx2, nz2;
        switch (facing) {
            case EAST -> {
                nx1 = 16 - z2; nz1 = x1; nx2 = 16 - z1; nz2 = x2;
            }
            case SOUTH -> {
                nx1 = 16 - x2; nz1 = 16 - z2; nx2 = 16 - x1; nz2 = 16 - z1;
            }
            case WEST -> {
                nx1 = z1; nz1 = 16 - x2; nx2 = z2; nz2 = 16 - x1;
            }
            default -> {
                nx1 = x1; nz1 = z1; nx2 = x2; nz2 = z2;
            }
        }
        return Block.box(nx1, y1, nz1, nx2, y2, nz2);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (rows == 0) {
            return null;
        }
        return new FurnitureBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (rows == 0) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof FurnitureBlockEntity be) {
            final int r = rows;
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable(FurnitureBlock.this.getDescriptionId());
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    MenuType<?> type = r == 6 ? MenuType.GENERIC_9x6 : MenuType.GENERIC_9x3;
                    return new ChestMenu(type, id, inv, be, r);
                }
            });
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
