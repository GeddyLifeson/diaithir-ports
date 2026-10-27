package com.diaithir.addons.oreexcavation.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Excavation recipes are looked up by id/type; they never match crafting inputs. */
public final class NoInput implements RecipeInput {
	public static final NoInput INSTANCE = new NoInput();

	private NoInput() {
	}

	@Override
	public ItemStack getItem(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public int size() {
		return 0;
	}
}
