package vectorwing.farmersdelight.common.block.entity.inventory;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;

/**
 * The cooking pot's inventory as seen from one side: the top takes and gives ingredients,
 * other sides take containers and give finished meals.
 */
public class CookingPotItemHandler implements ResourceHandler<ItemResource>
{
	private static final int SLOTS_INPUT = 6;
	private static final int SLOT_CONTAINER_INPUT = 7;
	private static final int SLOT_MEAL_OUTPUT = 8;
	private final ResourceHandler<ItemResource> itemHandler;
	private final boolean top;

	public CookingPotItemHandler(ResourceHandler<ItemResource> itemHandler, @Nullable Direction side) {
		this.itemHandler = itemHandler;
		this.top = side == null || side == Direction.UP;
	}

	@Override
	public int size() {
		return itemHandler.size();
	}

	@Override
	public ItemResource getResource(int slot) {
		return itemHandler.getResource(slot);
	}

	@Override
	public long getAmountAsLong(int slot) {
		return itemHandler.getAmountAsLong(slot);
	}

	@Override
	public long getCapacityAsLong(int slot, ItemResource resource) {
		return itemHandler.getCapacityAsLong(slot, resource);
	}

	@Override
	public boolean isValid(int slot, ItemResource resource) {
		return itemHandler.isValid(slot, resource);
	}

	@Override
	public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
		boolean allowed = top ? slot < SLOTS_INPUT : slot == SLOT_CONTAINER_INPUT;
		return allowed ? itemHandler.insert(slot, resource, amount, transaction) : 0;
	}

	@Override
	public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
		boolean allowed = top ? slot < SLOTS_INPUT : slot == SLOT_MEAL_OUTPUT;
		return allowed ? itemHandler.extract(slot, resource, amount, transaction) : 0;
	}
}
