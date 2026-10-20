package untamedwilds.block;

import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class AlgaeBlock extends LegacyBushBlock implements BonemealableBlock, LiquidBlockContainer, net.neoforged.neoforge.common.IShearable {
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);

    public static final MapCodec<AlgaeBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, AlgaeBlock::new));

    @Override
    public MapCodec<? extends LegacyBushBlock> codec() {
        return CODEC;
    }

    public AlgaeBlock(BlockBehaviour.Properties properties) {
        super(properties.offsetType(BlockBehaviour.OffsetType.XZ));
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return state.isFaceSturdy(worldIn, pos, Direction.UP) && !state.is(Blocks.MAGMA_BLOCK);
    }
    
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8 ? super.getStateForPlacement(context) : null;
    }

    public BlockState updateShape(BlockState stateIn, LevelReader worldIn, ScheduledTickAccess amTicks, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource amRandom) {
        BlockState blockstate = super.updateShape(stateIn, worldIn, amTicks, currentPos, facing, facingPos, facingState, amRandom);
        if (!blockstate.isAir()) {
            amTicks.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        return blockstate;
    }

    public boolean isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState state) {
        return true;
    }

    public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        return true;
    }

    public void performBonemeal(ServerLevel worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        BlockState blockstate = worldIn.getBlockState(pos);
        for(int k = 0; k < 3; ++k) {
            BlockPos blockpos = pos.offset(rand.nextInt(3) - 1, 1 - rand.nextInt(3), rand.nextInt(3) - 1);
            if (worldIn.isInWorldBounds(blockpos) && worldIn.isEmptyBlock(blockpos) && worldIn.getBlockState(blockpos).is(Blocks.WATER) && blockstate.canSurvive(worldIn, blockpos)) {
                worldIn.setBlock(blockpos, blockstate, 2);
            }
        }
    }

    public FluidState getFluidState(BlockState p_154537_) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canPlaceLiquid(@org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity player, BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state, FluidState fluidIn) {
        return false;
    }

    @Override
    public java.util.List<net.minecraft.world.item.ItemStack> onSheared(@javax.annotation.Nullable net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack item, Level world, BlockPos pos) {
        return java.util.List.of(new net.minecraft.world.item.ItemStack(this.asItem()));
    }

    @Override
    public boolean isShearable(@javax.annotation.Nullable net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack item, Level world, BlockPos pos) {
        return true;
    }
}