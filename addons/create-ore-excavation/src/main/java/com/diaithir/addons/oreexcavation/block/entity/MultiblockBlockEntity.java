package com.diaithir.addons.oreexcavation.block.entity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

/** Ghost part of a multiblock. Smart so Create's tooltip behaviours can attach to it. */
public class MultiblockBlockEntity extends SmartBlockEntity {

	public MultiblockBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
		super(pType, pWorldPosition, pBlockState);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
	}
}
