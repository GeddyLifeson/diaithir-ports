package vectorwing.farmersdelight.common.block.entity.container;

import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import vectorwing.farmersdelight.common.block.entity.inventory.FDItemStacksHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CookingPotMealSlot extends ResourceHandlerSlot
{
	public CookingPotMealSlot(FDItemStacksHandler inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, inventoryIn::set, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		return false;
	}
}
