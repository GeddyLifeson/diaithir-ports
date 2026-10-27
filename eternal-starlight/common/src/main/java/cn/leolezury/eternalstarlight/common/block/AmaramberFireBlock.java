package cn.leolezury.eternalstarlight.common.block;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;

import net.minecraft.util.RandomSource;

import net.minecraft.world.level.ScheduledTickAccess;

import cn.leolezury.eternalstarlight.common.util.ESTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AmaramberFireBlock extends BaseFireBlock {
	public static final MapCodec<AmaramberFireBlock> CODEC = simpleCodec(AmaramberFireBlock::new);

	@Override
	public MapCodec<AmaramberFireBlock> codec() {
		return CODEC;
	}

	public AmaramberFireBlock(BlockBehaviour.Properties properties) {
		super(properties, 1.0F);
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean intersects) {
		ESPortCompat.hurt(entity, level.damageSources().inFire(), 1.0F);
		if (entity instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100));
		}
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource randomSource) {
		return this.canSurvive(state, level, currentPos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return canSurviveOnBlock(level.getBlockState(pos.below()));
	}

	public static boolean canSurviveOnBlock(BlockState state) {
		return state.is(ESTags.Blocks.AMARAMBER_FIRE_SURVIVES_ON);
	}

	@Override
	protected boolean canBurn(BlockState state) {
		return true;
	}
}
