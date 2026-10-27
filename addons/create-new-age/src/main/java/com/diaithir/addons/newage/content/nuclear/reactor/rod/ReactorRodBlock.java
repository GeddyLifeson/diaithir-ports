package com.diaithir.addons.newage.content.nuclear.reactor.rod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.diaithir.addons.newage.CNABlocks;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.nuclear.reactor.ReactorBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReactorRodBlock extends ReactorBlock implements EntityBlock {
    public ReactorRodBlock(Properties properties) {
        super(properties
                .isViewBlocking((blockState, blockGetter, blockPos) -> false)
                .lightLevel(state -> state.getValue(ACTIVE) ? 12 : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
    }

    protected static final VoxelShape Y_AXIS_AABB = Block.box(4.5, 0.0, 4.5, 11.5, 16.0, 11.5);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(4.5, 4.5, 0.0, 11.5, 11.5, 16.0);
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 4.5, 4.5, 16.0, 11.5, 11.5);

    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;
    public static final EnumProperty<Direction.Axis> AXIS =  BlockStateProperties.AXIS;

    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }



    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ACTIVE, false).setValue(AXIS, context.getClickedFace().getAxis());
    }

    public VoxelShape getShape(BlockState arg, BlockGetter arg2, BlockPos arg3, CollisionContext arg4) {
        return switch ((arg.getValue(AXIS))) {
            case X -> X_AXIS_AABB;
            case Z -> Z_AXIS_AABB;
            case Y -> Y_AXIS_AABB;
        };
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        this.explode(level, pos, state, CNAConfig.getServer().radiationDamageExplosionScale.get().floatValue(),
                CNAConfig.getServer().radiationDamageExplosionFire.get());
    }

    public void explode(LevelAccessor level, BlockPos pos, BlockState state, float explosionRadius, boolean useFire) {
        if (state.getValue(ACTIVE)) {
            if (level instanceof ServerLevel lvl && explosionRadius > 0) {
                //Remove block first, so that explosion rays are not stopped immediately
                lvl.removeBlock(pos, false);

                //Explosion Logic, uses Config for Scale and fire
                lvl.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, explosionRadius, useFire, Level.ExplosionInteraction.TNT);
            }
            //Add block after explosion logic
            level.setBlock(pos, CNABlocks.CORIUM.get().defaultBlockState(), 3);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return state.getValue(ACTIVE) ? new ArrayList<>() : super.getDrops(state, params);
    }


    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (state.getValue(ACTIVE))
            return InteractionResult.PASS;
        else {
            return super.onSneakWrenched(state, context);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE).add(AXIS);
    }

    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CNABlockEntityTypes.REACTOR_ROD.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return (level1, blockPos, blockState, blockEntity) -> {
            if (!(blockEntity instanceof ReactorRodBlockEntity ent)) return;
            ent.tick(blockPos, level1, blockState);
        };
    }

}