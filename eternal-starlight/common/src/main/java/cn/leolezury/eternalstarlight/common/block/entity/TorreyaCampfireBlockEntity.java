package cn.leolezury.eternalstarlight.common.block.entity;

import cn.leolezury.eternalstarlight.common.block.TorreyaCampfireBlock;
import cn.leolezury.eternalstarlight.common.registry.ESBlockEntities;
import cn.leolezury.eternalstarlight.common.registry.ESMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TorreyaCampfireBlockEntity extends CampfireBlockEntity {
	public TorreyaCampfireBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(blockPos, blockState);
	}

	@Override
	public boolean isValidBlockState(BlockState blockState) {
		return this.getType().isValid(blockState);
	}

	private static final net.minecraft.world.item.crafting.RecipeManager.CachedCheck<net.minecraft.world.item.crafting.SingleRecipeInput, net.minecraft.world.item.crafting.CampfireCookingRecipe> QUICK_CHECK = net.minecraft.world.item.crafting.RecipeManager.createCheck(net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING);

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity) {
		if (blockState.getValue(BlockStateProperties.LIT)) {
			if (level instanceof net.minecraft.server.level.ServerLevel sl) cookTick(sl, blockPos, blockState, campfireBlockEntity, QUICK_CHECK);
			AABB box = AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(blockPos)).inflate(10);
			for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box)) {
				if (!(living instanceof Enemy) && !living.hasEffect(MobEffects.REGENERATION)) {
					living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100));
				}
			}
			if (blockState.getValue(TorreyaCampfireBlock.STARFIRE)) {
				for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box)) {
					if (living instanceof Enemy && !living.hasEffect(ESMobEffects.STARFIRE.asHolder())) {
						living.addEffect(new MobEffectInstance(ESMobEffects.STARFIRE.asHolder(), 100));
					}
				}
			}
		} else {
			cooldownTick(level, blockPos, blockState, campfireBlockEntity);
			if (blockState.getValue(TorreyaCampfireBlock.STARFIRE)) {
				level.setBlockAndUpdate(blockPos, blockState.setValue(TorreyaCampfireBlock.STARFIRE, false));
			}
		}
	}

	@Override
	public BlockEntityType<?> getType() {
		return ESBlockEntities.TORREYA_CAMPFIRE.get();
	}
}
