package untamedwilds.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class UndergrowthBlock extends LegacyBushBlock implements BonemealableBlock, net.neoforged.neoforge.common.IShearable {

    public static final MapCodec<UndergrowthBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> 
        instance.group(propertiesCodec()).apply(instance, UndergrowthBlock::new));

    @Override
    public MapCodec<? extends LegacyBushBlock> codec() {
        return CODEC;
    }

    public UndergrowthBlock(Properties properties) {
        super(properties);
    }

    public UndergrowthBlock(Properties properties, BlockBehaviour.OffsetType type) {
        super(properties.offsetType(type));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return state.isFaceSturdy(worldIn, pos, Direction.UP) && !state.is(Blocks.MAGMA_BLOCK);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (entityIn instanceof Player && !entityIn.isSteppingCarefully()) {
            entityIn.makeStuckInBlock(state, new Vec3(0.95F, 1D, 0.95F));
            if (worldIn.getRandom().nextInt(20) == 0) {
                worldIn.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.GRASS_STEP, SoundSource.AMBIENT, 1, 1, true);
            }
        }
    }

    // BonemealableBlock implementation - 1.21.1 signature
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockState blockstate = level.getBlockState(pos);
        for(int k = 0; k < 3; ++k) {
            BlockPos blockpos = pos.offset(random.nextInt(3) - 1, 1 - random.nextInt(3), random.nextInt(3) - 1);
            if (level.isInWorldBounds(blockpos) && level.isEmptyBlock(blockpos) && blockstate.canSurvive(level, blockpos)) {
                level.setBlock(blockpos, blockstate, 2);
            }
        }
    }

    public BlockState getPlant(BlockGetter level, BlockPos pos) {
        return defaultBlockState();
    }

    // IShearable implementation - NeoForge 1.21.1
    public List<ItemStack> onSheared(@org.jetbrains.annotations.Nullable Player player, ItemStack item, Level world, BlockPos pos) {
        return List.of(new ItemStack(this.asItem()));
    }
}