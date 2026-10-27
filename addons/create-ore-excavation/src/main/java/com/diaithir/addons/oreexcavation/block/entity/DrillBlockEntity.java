package com.diaithir.addons.oreexcavation.block.entity;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.recipe.DrillingRecipe;
import com.diaithir.addons.oreexcavation.util.IOBlockType;
import com.diaithir.addons.oreexcavation.util.QueueInventory;

public class DrillBlockEntity extends ExcavatingBlockEntityImpl<DrillingRecipe> {
	private final QueueInventory inventory = new QueueInventory();

	public DrillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public @Nullable Container getItemInv(IOBlockType type) {
		return type == IOBlockType.ITEM_OUT ? inventory : null;
	}

	@Override
	protected void read(ValueInput tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		if(!clientPacket) {
			inventory.load(tag);
		}
	}

	@Override
	protected void write(ValueOutput tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		if(!clientPacket) {
			inventory.save(tag);
		}
	}

	@Override
	public void dropInv() {
		super.dropInv();
		for(int i = 0; i < inventory.getContainerSize(); ++i) {
			dropItemStack(inventory.getItem(i));
		}
	}

	@Override
	protected boolean canExtract() {
		return inventory.hasSpace() && super.canExtract();
	}

	@Override
	protected void onFinished() {
		current.value().getOutput().stream().map(o -> o.rollOutput(level.getRandom())).filter(i -> !i.isEmpty()).forEach(inventory::add);
		super.onFinished();
	}

	@Override
	public RecipeType<DrillingRecipe> getRecipeType() {
		return CreateOreExcavation.DRILLING_RECIPES.getRecipeType();
	}
}
