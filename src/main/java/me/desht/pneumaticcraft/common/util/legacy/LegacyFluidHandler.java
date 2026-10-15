package me.desht.pneumaticcraft.common.util.legacy;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Exposes one of Actually Additions' pre-26.1 fluid handlers as the 26.1 fluid capability. An old handler cannot be rolled
 * back, so a transfer is simulated when asked and carried out when the outermost transaction commits; transfers already
 * queued in the transaction count against later simulations, so nothing is promised twice.
 */
public class LegacyFluidHandler extends SnapshotJournal<List<LegacyFluidHandler.Transfer>> implements ResourceHandler<FluidResource> {
    private final IFluidHandler handler;
    private List<Transfer> queued = new ArrayList<>();

    record Transfer(FluidStack fluid, boolean fill) {}

    public LegacyFluidHandler(IFluidHandler handler) {
        this.handler = handler;
    }

    @Override
    public int size() {
        return handler.getTanks();
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.of(handler.getFluidInTank(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return handler.getFluidInTank(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return handler.getTankCapacity(index);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return handler.isFluidValid(index, resource.toStack(1));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        int room = handler.fill(resource.toStack(amount + queuedAmount(resource, true)), FluidAction.SIMULATE) - queuedAmount(resource, true);
        return queue(resource, Math.min(amount, room), true, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        int available = handler.drain(resource.toStack(amount + queuedAmount(resource, false)), FluidAction.SIMULATE).getAmount() - queuedAmount(resource, false);
        return queue(resource, Math.min(amount, available), false, transaction);
    }

    private int queue(FluidResource resource, int amount, boolean fill, TransactionContext transaction) {
        if (amount <= 0) return 0;
        updateSnapshots(transaction);
        queued.add(new Transfer(resource.toStack(amount), fill));
        return amount;
    }

    private int queuedAmount(FluidResource resource, boolean fill) {
        int total = 0;
        for (Transfer transfer : queued) {
            if (transfer.fill == fill && resource.matches(transfer.fluid)) total += transfer.fluid.getAmount();
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
            if (transfer.fill) handler.fill(transfer.fluid, FluidAction.EXECUTE);
            else handler.drain(transfer.fluid, FluidAction.EXECUTE);
        }
        queued = new ArrayList<>();
    }
}
