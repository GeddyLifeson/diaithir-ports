package com.diaithir.addons.oreexcavation.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.Container;

import com.zurrtum.create.infrastructure.fluids.FluidInventory;

import com.diaithir.addons.oreexcavation.util.IOBlockType;

public interface MultiblockCapHandler {
	@Nullable Container getItemInv(IOBlockType type);

	@Nullable FluidInventory getFluidInv(IOBlockType type);

	void addKinetic(Kinetic k);

	public static interface Kinetic {
		float getRotationSpeed();
		void setStress(float stress);
	}

	void dropInv();
}
