package com.diaithir.addons.oreexcavation.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.recipe.ExtractorRecipe;
import com.diaithir.addons.oreexcavation.util.IOBlockType;

public class ExtractorBlockEntity extends ExcavatingBlockEntityImpl<ExtractorRecipe> {
	/** Output tank: only the machine fills it, pipes may only drain it. */
	public final FluidTank fluidTankOut;

	public ExtractorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		fluidTankOut = new FluidTank(16000 * MB) {
			@Override
			public void markDirty() {
				notifyUpdate();
			}

			@Override
			public boolean isValid(int slot, FluidStack stack) {
				return false;
			}
		};
	}

	@Override
	public @Nullable FluidInventory getFluidInv(IOBlockType type) {
		return type == IOBlockType.FLUID_OUT ? fluidTankOut : super.getFluidInv(type);
	}

	private boolean fitsOutput() {
		FluidStack out = current.value().getOutput();
		FluidStack have = fluidTankOut.getFluid();
		return have.isEmpty() ? out.getAmount() <= fluidTankOut.getMaxAmountPerStack()
				: FluidStack.areFluidsAndComponentsEqual(have, out) && have.getAmount() + out.getAmount() <= fluidTankOut.getMaxAmountPerStack();
	}

	@Override
	protected boolean canExtract() {
		return super.canExtract() && fitsOutput();
	}

	@Override
	protected void onFinished() {
		FluidStack out = current.value().getOutput();
		FluidStack have = fluidTankOut.getFluid();
		if (have.isEmpty())
			fluidTankOut.setFluid(out.copy());
		else {
			have.setAmount(have.getAmount() + out.getAmount());
			fluidTankOut.markDirty();
		}
		super.onFinished();
	}

	@Override
	protected void read(ValueInput tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		fluidTankOut.read(tag.childOrEmpty("tankOut"));
	}

	@Override
	protected void write(ValueOutput tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		fluidTankOut.write(tag.child("tankOut"));
	}

	@Override
	public RecipeType<ExtractorRecipe> getRecipeType() {
		return CreateOreExcavation.EXTRACTING_RECIPES.getRecipeType();
	}

	@Override
	protected String getTankInName() {
		return "tankIn";
	}
}
