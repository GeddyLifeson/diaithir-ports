package com.diaithir.addons.newage.content.heat.pipe;

import net.minecraft.world.level.redstone.Orientation;
import com.zurrtum.create.api.boiler.BoilerHeater;
import com.zurrtum.create.content.decoration.encasing.EncasedBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.diaithir.addons.newage.CNABlocks;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.heat.HeatBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class EncasedHeatPipeBlock extends Block implements EntityBlock, IWrenchable, EncasedBlock {

    private final Supplier<Block> casing;

    public static BooleanProperty UP = BlockStateProperties.UP;
    public static BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static BooleanProperty EAST = BlockStateProperties.EAST;
    public static BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static BooleanProperty WEST = BlockStateProperties.WEST;

    public EncasedHeatPipeBlock(Properties properties, Supplier<Block> casing) {
        super(properties);
        this.casing = casing;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public Block getCasing() {
        return casing.get();
    }

    @Override
    public void handleEncasing(BlockState state, Level level, BlockPos pos, ItemStack heldItem, Player player, InteractionHand hand, BlockHitResult ray) {
        level.setBlockAndUpdate(pos, EncasedHeatPipeBlock.transferSixWayProperties(state, defaultBlockState()));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return CNABlocks.HEAT_PIPE.toStack();
    }

    public static BooleanProperty getDirectionProperty(Direction dir) {
        return switch (dir) {
            case Direction.DOWN -> DOWN;
            case Direction.UP -> UP;
            case Direction.NORTH -> NORTH;
            case Direction.SOUTH -> SOUTH;
            case Direction.WEST -> WEST;
            case Direction.EAST -> EAST;
        };
    }

    public static BlockState transferSixWayProperties(BlockState from, BlockState to) {
        for (Direction dir : Direction.values()) {
            to = to.setValue(getDirectionProperty(dir), from.getValue(getDirectionProperty(dir)));
        }
        return to;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (world.isClientSide())
            return InteractionResult.SUCCESS;

        context.getLevel().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, context.getClickedPos(), Block.getId(state));

        world.setBlockAndUpdate(pos, HeatPipeBlock.updateState(CNABlocks.HEAT_PIPE.get().defaultBlockState(), world, pos));
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CNABlockEntityTypes.ENCASED_HEAT_PIPE.get().create(pos, state);
    }

    public static int massPipe = 0;
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        final int on = massPipe;
        massPipe++;
        if (massPipe >= 20) {
            massPipe = 0;
        }
        return (world, blockPos, blockState, self) -> {
            if ((world.getGameTime() + on) % 20 != 0 || !(self instanceof HeatPipeBlockEntity selfC)) return;

            BlockPos heatPos = blockPos.below();

            float heat = BoilerHeater.findHeat(level, blockPos, level.getBlockState(heatPos));

            selfC.generating = 0;

            double loss =  CNAConfig.getServer().passivePipeHeatLoss.get();

            selfC.heat = (float) Math.max(0, selfC.heat - loss);

            if (heat >= 0) {
                selfC.generating = ((1+heat*3) * 6);
                selfC.generating = (float) Math.max(Math.min(selfC.generating, ((2+Math.pow(2, heat)) * 300) - selfC.heat), 0);
                selfC.heat += selfC.generating;
            }

            selfC.generating -= (float) loss;

            HeatBlockEntity.transferAround(selfC);
            HeatBlockEntity.handleOverheat(selfC);
            HeatBlockEntity.trySync(selfC);
        };
    }
}
