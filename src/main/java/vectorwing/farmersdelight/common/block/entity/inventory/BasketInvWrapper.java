package vectorwing.farmersdelight.common.block.entity.inventory;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import vectorwing.farmersdelight.common.block.entity.Basket;

/** The basket's inventory for automation: filling an empty basket starts its pickup cooldown once the insert commits. */
public class BasketInvWrapper implements ResourceHandler<ItemResource>
{
	protected final Basket basket;
	private final ResourceHandler<ItemResource> items;
	private boolean cooldownPending;
	private final SnapshotJournal<Boolean> cooldownJournal = new SnapshotJournal<>()
	{
		@Override
		protected Boolean createSnapshot() {
			return cooldownPending;
		}

		@Override
		protected void revertToSnapshot(Boolean snapshot) {
			cooldownPending = snapshot;
		}

		@Override
		protected void onRootCommit(Boolean original) {
			if (cooldownPending && !basket.isOnCustomCooldown()) {
				basket.setCooldown(8);
			}
			cooldownPending = false;
		}
	};

	public BasketInvWrapper(Basket basket) {
		this.basket = basket;
		this.items = VanillaContainerWrapper.of(basket);
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public ItemResource getResource(int slot) {
		return items.getResource(slot);
	}

	@Override
	public long getAmountAsLong(int slot) {
		return items.getAmountAsLong(slot);
	}

	@Override
	public long getCapacityAsLong(int slot, ItemResource resource) {
		return items.getCapacityAsLong(slot, resource);
	}

	@Override
	public boolean isValid(int slot, ItemResource resource) {
		return items.isValid(slot, resource);
	}

	@Override
	public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
		boolean wasEmpty = basket.isEmpty();
		int inserted = items.insert(slot, resource, amount, transaction);
		if (wasEmpty && inserted > 0) {
			cooldownJournal.updateSnapshots(transaction);
			cooldownPending = true;
		}
		return inserted;
	}

	@Override
	public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
		return items.extract(slot, resource, amount, transaction);
	}
}
