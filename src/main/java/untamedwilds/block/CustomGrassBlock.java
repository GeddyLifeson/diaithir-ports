package untamedwilds.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.LevelReader;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CustomGrassBlock extends FlowerBlock implements BonemealableBlock {
    protected static final float AABB_OFFSET = 6.0F;
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public static final MapCodec<CustomGrassBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, CustomGrassBlock::new));

    @Override
    public MapCodec<? extends FlowerBlock> codec() {
        return CODEC;
    }

    public CustomGrassBlock(MobEffect p_53512_, int p_53513_, BlockBehaviour.Properties p_53514_) {
        this(net.minecraft.core.Holder.direct(p_53512_), p_53513_, p_53514_);
    }

    public CustomGrassBlock(Holder<MobEffect> p_53512_, int p_53513_, BlockBehaviour.Properties p_53514_) {
        super(p_53512_, p_53513_, p_53514_.offsetType(BlockBehaviour.OffsetType.XZ));
    }

    // Convenience ctor for codec/property-only construction
    public CustomGrassBlock(BlockBehaviour.Properties props) {
        super(MobEffects.SPEED, 0, props.offsetType(BlockBehaviour.OffsetType.XZ));
    }

    public VoxelShape getShape(BlockState p_53517_, BlockGetter p_53518_, BlockPos p_53519_, CollisionContext p_53520_) {
        Vec3 vec3 = p_53517_.getOffset(p_53519_);
        return SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    public boolean isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState state) {
        return worldIn.getBlockState(pos.above()).isAir();
    }

    public boolean isBonemealSuccess(Level p_153802_, RandomSource p_153803_, BlockPos p_153804_, BlockState p_153805_) {
        return true;
    }

    public void performBonemeal(ServerLevel p_57298_, RandomSource p_57299_, BlockPos p_57300_, BlockState p_57301_) {
        popResource(p_57298_, p_57300_, new ItemStack(this));
    }
}