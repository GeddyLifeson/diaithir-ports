package net.diaithir.hostile.raided.client.state;

import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.item.ItemStack;

public class IncineratorRenderState extends IllagerRenderState {
	public ItemStack headStack = ItemStack.EMPTY;
	public boolean throwing = false;
}
