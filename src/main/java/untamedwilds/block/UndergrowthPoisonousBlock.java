package untamedwilds.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import untamedwilds.init.ModBlock;
import untamedwilds.init.ModTags;

import javax.annotation.Nullable;
import java.util.List;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class UndergrowthPoisonousBlock extends UndergrowthBlock implements BonemealableBlock, IPostGenUpdate, net.neoforged.neoforge.common.IShearable {

    public static final MapCodec<UndergrowthPoisonousBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> 
        instance.group(propertiesCodec()).apply(instance, props -> new UndergrowthPoisonousBlock(props, BlockBehaviour.OffsetType.NONE)));

    @Override
    public MapCodec<? extends UndergrowthBlock> codec() {
        return CODEC;
    }

    public static final IntegerProperty PROPERTY_AGE = BlockStateProperties.AGE_2;

    public UndergrowthPoisonousBlock(Properties properties, BlockBehaviour.OffsetType type) {
        super(properties, type);
        this.registerDefaultState(this.stateDefinition.any().setValue(PROPERTY_AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROPERTY_AGE);
    }

    @Override
    protected void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn, net.minecraft.world.entity.InsideBlockEffectApplier amApplier, boolean amPrecise) {
        if (entityIn instanceof LivingEntity && !entityIn.isSteppingCarefully() && entityIn.getBbHeight() > 1) {
            ((LivingEntity)entityIn).addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            entityIn.makeStuckInBlock(state, new Vec3(0.95F, 1D, 0.95F));
            if (worldIn.getRandom().nextInt(20) == 0) {
                worldIn.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.GRASS_STEP, SoundSource.AMBIENT, 1, 1, true);
            }
        }
    }

    @Nullable
    public BlockState getStateForWorldgen(LevelAccessor world, BlockPos pos) {
        BlockState blockstate = world.getBlockState(pos.below());
        if (blockstate.getBlock() == ModBlock.HEMLOCK.get()) {
            if (world.getFluidState(pos.below()).isEmpty() || world.getBlockState(pos.below(2)).getBlock() == ModBlock.HEMLOCK.get()) {
                world.setBlock(pos.below(), blockstate.setValue(PROPERTY_AGE, 1), 1);
            }
            BlockState blockstate1 = world.getBlockState(pos.above());
            return blockstate1.getBlock() != ModBlock.HEMLOCK.get() ? ModBlock.HEMLOCK.get().defaultBlockState().setValue(PROPERTY_AGE, 0) : this.defaultBlockState().setValue(PROPERTY_AGE, 1);
        }
        if (blockstate.is(ModTags.ModBlockTags.REEDS_PLANTABLE_ON)) {
            return this.defaultBlockState().setValue(PROPERTY_AGE, 0);
        }
        else {
            return null;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().below());
        if (blockstate.getBlock() == ModBlock.HEMLOCK.get()) {
            if (context.getLevel().getFluidState(context.getClickedPos().below()).isEmpty() || context.getLevel().getBlockState(context.getClickedPos().below(2)).getBlock() == ModBlock.HEMLOCK.get()) {
                context.getLevel().setBlockAndUpdate(context.getClickedPos().below(), blockstate.setValue(PROPERTY_AGE, 1));
            }
            BlockState blockstate1 = context.getLevel().getBlockState(context.getClickedPos().above());
            return blockstate1.getBlock() != ModBlock.HEMLOCK.get() ? ModBlock.HEMLOCK.get().defaultBlockState().setValue(PROPERTY_AGE, 0) : this.defaultBlockState().setValue(PROPERTY_AGE, 1);
        }
        if (blockstate.is(ModTags.ModBlockTags.REEDS_PLANTABLE_ON)) {
            return this.defaultBlockState().setValue(PROPERTY_AGE, 0);
        }
        else {
            return null;
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        if (!state.canSurvive(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.below()).is(ModTags.ModBlockTags.REEDS_PLANTABLE_ON) || worldIn.getBlockState(pos.below()).getBlock() == ModBlock.HEMLOCK.get();
    }

    @Override
    protected BlockState updateShape(BlockState stateIn, LevelReader worldIn, ScheduledTickAccess amTicks, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource amRandom) {
        if (!stateIn.canSurvive(worldIn, currentPos)) {
            amTicks.scheduleTick(currentPos, this, 1);
        }

        return super.updateShape(stateIn, worldIn, amTicks, currentPos, facing, facingPos, facingState, amRandom);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState state) {
        int i = this.getNumReedBlocksAbove(worldIn, pos);
        int j = this.getNumReedBlocksBelow(worldIn, pos);
        return i + j + 1 < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel worldIn, RandomSource rand, BlockPos pos, BlockState state) {
        int i = this.getNumReedBlocksAbove(worldIn, pos);
        int j = this.getNumReedBlocksBelow(worldIn, pos);
        int k = i + j + 1;
        int l = 1 + rand.nextInt(2);

        for(int i1 = 0; i1 < l; ++i1) {
            BlockPos blockpos = pos.above(i);
            BlockState blockstate = worldIn.getBlockState(blockpos);
            if (k >= 4 || !worldIn.isEmptyBlock(blockpos.above())) {
                return;
            }
            if (blockstate.getValue(PROPERTY_AGE) != 2) {
                worldIn.setBlock(blockpos, this.defaultBlockState().setValue(PROPERTY_AGE, 1), 3);
            }
            worldIn.setBlock(blockpos.above(), this.defaultBlockState().setValue(PROPERTY_AGE, 0), 3);
            ++i;
            ++k;
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        boolean can = player.getMainHandItem().is(net.minecraft.tags.ItemTags.SWORDS);
        return can ? 1.0F : super.getDestroyProgress(state, player, worldIn, pos);
    }

    protected int getNumReedBlocksAbove(BlockGetter worldIn, BlockPos pos) {
        int i;
        for(i = 0; i < 4 && worldIn.getBlockState(pos.above(i + 1)).getBlock() == ModBlock.HEMLOCK.get(); ++i) {
        }
        return i;
    }

    protected int getNumReedBlocksBelow(BlockGetter worldIn, BlockPos pos) {
        int i;
        for(i = 0; i < 4 && worldIn.getBlockState(pos.below(i + 1)).getBlock() == ModBlock.HEMLOCK.get(); ++i) {
        }
        return i;
    }

    public void updatePostGen(LevelAccessor worldIn, BlockPos pos) {
        if (worldIn.getRandom().nextBoolean()) {
            worldIn.setBlock(pos, this.defaultBlockState().setValue(PROPERTY_AGE, 1), 3);
            worldIn.setBlock(pos.above(), this.defaultBlockState().setValue(PROPERTY_AGE, 0), 3);
        }
    }

    // IShearable implementation - NeoForge 1.21.1
    @Override
    public List<ItemStack> onSheared(@org.jetbrains.annotations.Nullable Player player, ItemStack item, Level world, BlockPos pos) {
        return List.of(new ItemStack(this.asItem()));
    }
}