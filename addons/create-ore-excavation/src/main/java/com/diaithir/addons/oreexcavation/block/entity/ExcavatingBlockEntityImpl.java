package com.diaithir.addons.oreexcavation.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;

import com.diaithir.addons.oreexcavation.recipe.ExcavatingRecipe;
import com.diaithir.addons.oreexcavation.util.IOBlockType;

public abstract class ExcavatingBlockEntityImpl<R extends ExcavatingRecipe> extends ExcavatingBlockEntity<R> {
	/** Create's fluid unit is 1/81 mB. */
	public static final int MB = 81;
	public final FluidTank fluidTank;

	protected ExcavatingBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		fluidTank = new FluidTank(16000 * MB) {
			@Override
			public void markDirty() {
				notifyUpdate();
			}
		};
	}

	@Override
	public @Nullable FluidInventory getFluidInv(IOBlockType type) {
		return type == IOBlockType.FLUID_IN ? fluidTank : null;
	}

	@Override
	public @Nullable Container getItemInv(IOBlockType type) {
		return null;
	}

	@Override
	protected void read(ValueInput tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		fluidTank.read(tag.childOrEmpty(getTankInName()));
	}

	@Override
	protected void write(ValueOutput tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		fluidTank.write(tag.child(getTankInName()));
	}

	@Override
	protected boolean validateRecipe(R recipe) {
		return super.validateRecipe(recipe) && (recipe.getDrillingFluid().isEmpty() || recipe.getDrillingFluid().get().test(fluidTank.getFluid()));
	}

	@Override
	protected boolean canExtract() {
		return current.value().getDrillingFluid().isEmpty() ||
				(current.value().getDrillingFluid().get().test(fluidTank.getFluid()) &&
						fluidTank.getFluid().getAmount() >= current.value().getDrillingFluid().get().amount());
	}

	@Override
	protected void onFinished() {
		current.value().getDrillingFluid().ifPresent(fluid -> {
			fluidTank.extract(fluidTank.getFluid(), fluid.amount());
		});
	}

	protected String getTankInName() {
		return "tank";
	}
}
