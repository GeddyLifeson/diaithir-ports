package com.diaithir.addons.oreexcavation.block.entity;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;

import com.diaithir.addons.oreexcavation.block.MultiblockPart;
import com.diaithir.addons.oreexcavation.block.IOBlock;
import com.diaithir.addons.oreexcavation.util.IOBlockType;

public class IOBlockEntity extends SmartBlockEntity {

	public IOBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
		super(pType, pWorldPosition, pBlockState);
	}

	private IOBlockType type() {
		return getBlockState().getValue(IOBlock.TYPE);
	}

	private @Nullable MultiblockCapHandler main() {
		BlockEntity te = MultiblockPart.findMain(level, worldPosition);
		return te instanceof MultiblockCapHandler h ? h : null;
	}

	public @Nullable Container itemInv(@Nullable Direction side) {
		IOBlockType t = type();
		MultiblockCapHandler h = t.fluid ? null : main();
		return h == null ? null : h.getItemInv(t);
	}

	public @Nullable FluidInventory fluidInv() {
		IOBlockType t = type();
		MultiblockCapHandler h = t.fluid ? main() : null;
		return h == null ? null : h.getFluidInv(t);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
	}
}
