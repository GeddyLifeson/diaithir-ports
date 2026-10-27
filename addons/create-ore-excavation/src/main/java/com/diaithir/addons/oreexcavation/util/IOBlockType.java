package com.diaithir.addons.oreexcavation.util;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

public enum IOBlockType implements StringRepresentable {
	ITEM_IN(false, false),
	ITEM_OUT(false, false),
	FLUID_IN(true, false),
	FLUID_OUT(true, true),
	;
	private final String name;
	public final boolean fluid, out;

	private IOBlockType(boolean fluid, boolean out) {
		name = name().toLowerCase(Locale.ROOT);
		this.fluid = fluid;
		this.out = out;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
