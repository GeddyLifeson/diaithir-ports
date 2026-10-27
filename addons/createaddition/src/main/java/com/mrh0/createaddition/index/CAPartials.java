package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;

import net.minecraft.resources.Identifier;

/** Client-only; touched from the client init only. */
public class CAPartials {
	public static final PartialModel SMALL_LIGHT = block("connector/small_light");
	public static final PartialModel ACCUMULATOR_GUAGE = block("modular_accumulator/guage");
	public static final PartialModel ACCUMULATOR_DIAL = block("modular_accumulator/dial");

	private static PartialModel block(String path) {
		return PartialModel.of(Identifier.fromNamespaceAndPath(CreateAddition.MODID, "block/" + path));
	}

	public static void init() {}
}
