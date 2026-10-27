package cn.leolezury.eternalstarlight.common.item.menu;

import cn.leolezury.eternalstarlight.common.block.entity.AlloyFurnaceBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlloyFurnaceFuelSlot extends Slot {
	private final net.minecraft.world.level.Level level;

	public AlloyFurnaceFuelSlot(Container container, int slot, int xPosition, int yPosition, net.minecraft.world.level.Level level) {
		super(container, slot, xPosition, yPosition);
		this.level = level;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return AlloyFurnaceBlockEntity.isFuel(level, stack);
	}
}
