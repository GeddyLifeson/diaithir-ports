package de.ellpeck.actuallyadditions.mod.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Exposes one of Actually Additions' pre-26.1 item handlers as the 26.1 item capability. An old handler cannot be rolled
 * back, so a transfer is simulated when asked and carried out when the outermost transaction commits; transfers already
 * queued for a slot count against later simulations, so nothing is promised twice.
 */
public class LegacyItemHandler extends SnapshotJournal<List<LegacyItemHandler.Transfer>> implements ResourceHandler<ItemResource> {
    private final IItemHandler handler;
    private List<Transfer> queued = new ArrayList<>();

    record Transfer(int slot, ItemResource resource, int amount, boolean insert) {}

    public LegacyItemHandler(IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public int size() {
        return handler.getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(handler.getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return handler.getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.isEmpty() ? handler.getSlotLimit(index) : Math.min(handler.getSlotLimit(index), resource.getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return handler.isItemValid(index, resource.toStack(1));
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        int pending = queuedAmount(index, true);
        // simulate on top of what is already queued for this slot; whatever is left over did not fit
        ItemStack remainder = handler.insertItem(index, resource.toStack(amount + pending), true);
        return queue(index, resource, amount - remainder.getCount(), true, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (!resource.matches(handler.getStackInSlot(index))) return 0;
        int pending = queuedAmount(index, false);
        int available = handler.extractItem(index, amount + pending, true).getCount() - pending;
        return queue(index, resource, Math.min(amount, available), false, transaction);
    }

    private int queue(int slot, ItemResource resource, int amount, boolean insert, TransactionContext transaction) {
        if (amount <= 0) return 0;
        updateSnapshots(transaction);
        queued.add(new Transfer(slot, resource, amount, insert));
        return amount;
    }

    private int queuedAmount(int slot, boolean insert) {
        int total = 0;
        for (Transfer transfer : queued) {
            if (transfer.slot == slot && transfer.insert == insert) total += transfer.amount;
        }
        return total;
    }

    @Override
    protected List<Transfer> createSnapshot() {
        return new ArrayList<>(queued);
    }

    @Override
    protected void revertToSnapshot(List<Transfer> snapshot) {
        queued = snapshot;
    }

    @Override
    protected void onRootCommit(List<Transfer> originalState) {
        for (Transfer transfer : queued) {
            if (transfer.insert) handler.insertItem(transfer.slot, transfer.resource.toStack(transfer.amount), false);
            else handler.extractItem(transfer.slot, transfer.amount, false);
        }
        queued = new ArrayList<>();
    }
}
