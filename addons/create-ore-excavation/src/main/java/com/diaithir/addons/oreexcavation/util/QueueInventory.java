package com.diaithir.addons.oreexcavation.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Extract-only output queue exposed to Create's item transfer as a plain Container. */
public class QueueInventory implements Container {
	private final List<ItemStack> content = new ArrayList<>();

	@Override
	public int getContainerSize() {
		return content.size();
	}

	@Override
	public boolean isEmpty() {
		return content.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot < content.size() ? content.get(slot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot >= content.size())
			return ItemStack.EMPTY;
		return content.get(slot).split(amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot >= content.size())
			return ItemStack.EMPTY;
		return content.set(slot, ItemStack.EMPTY);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot < content.size())
			content.set(slot, stack);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		content.clear();
	}

	public void add(ItemStack is) {
		content.add(is);
	}

	public void save(ValueOutput out) {
		ValueOutput.TypedOutputList<ItemStack> l = out.list("inv", ItemStack.CODEC);
		content.stream().filter(s -> !s.isEmpty()).forEach(l::add);
	}

	public void load(ValueInput in) {
		content.clear();
		in.listOrEmpty("inv", ItemStack.CODEC).forEach(content::add);
	}

	public boolean hasSpace() {
		content.removeIf(ItemStack::isEmpty);
		return content.size() < 16;
	}
}
