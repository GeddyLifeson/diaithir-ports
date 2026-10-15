package vectorwing.farmersdelight.common.block.entity.container;

import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import vectorwing.farmersdelight.common.block.entity.inventory.FDItemStacksHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CookingPotResultSlot extends ResourceHandlerSlot
{
	public final CookingPotBlockEntity cookingPot;
	private final Player player;
	private int removeCount;

	public CookingPotResultSlot(Player player, CookingPotBlockEntity blockEntity, FDItemStacksHandler inventory, int index, int xPosition, int yPosition) {
		super(inventory, inventory::set, index, xPosition, yPosition);
		this.cookingPot = blockEntity;
		this.player = player;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}


	@Override
	public void onTake(Player player, ItemStack stack) {
		this.removeCount += stack.getCount();
		this.checkTakeAchievements(stack);
		super.onTake(player, stack);
	}

	@Override
	protected void onQuickCraft(ItemStack stack, int amount) {
		this.removeCount += amount;
		this.checkTakeAchievements(stack);
	}

	@Override
	protected void checkTakeAchievements(ItemStack stack) {
		stack.onCraftedBy(this.player, this.removeCount);

		if (!this.player.level().isClientSide()) {
			cookingPot.awardUsedRecipes(this.player, cookingPot.getDroppableInventory());
		}

		this.removeCount = 0;
	}
}
