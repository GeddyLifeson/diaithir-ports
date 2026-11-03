package net.diaithir.vehicles.fuel;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A {@link FuelTank} seen as a NeoForge fluid handler (one tank, fuel fluids only), so pipes, buckets and every other mod's pumps can fill and empty it.
 * Amounts are litres, 1 L = 1 mB.
 */
public class FuelFluidView implements ResourceHandler<FluidResource> {
    private final FuelTank tank;
    private final Runnable changed;
    private final SnapshotJournal<FuelTank.Snapshot> journal = new SnapshotJournal<>() {
        @Override
        protected FuelTank.Snapshot createSnapshot() {
            return tank.snapshot();
        }

        @Override
        protected void revertToSnapshot(FuelTank.Snapshot s) {
            tank.restore(s);
        }

        @Override
        protected void onRootCommit(FuelTank.Snapshot original) {
            changed.run();
        }
    };

    public FuelFluidView(FuelTank tank, Runnable changed) {
        this.tank = tank;
        this.changed = changed;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        String id = tank.fuelId();
        Fuel f = id == null ? null : Fuel.get(id);
        Fluid fl = f == null ? null : Fuels.fluidFor(f);
        return fl == null ? FluidResource.EMPTY : FluidResource.of(fl);
    }

    @Override
    public long getAmountAsLong(int index) {
        return tank.amountL();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return resource.isEmpty() || isValid(index, resource) ? tank.capacityL() : 0;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Fuel f = Fuels.of(resource.getFluid());
        return f != null && (tank.fuelId() == null || tank.fuelId().equals(f.id()));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
        Fuel f = Fuels.of(resource.getFluid());
        if (f == null) return 0;
        int put = (int) tank.fill(f.id(), amount, true);
        if (put <= 0) return 0;
        journal.updateSnapshots(tx);
        return (int) tank.fill(f.id(), put, false);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
        Fuel f = Fuels.of(resource.getFluid());
        if (f == null || !f.id().equals(tank.fuelId())) return 0;
        int took = (int) tank.drain(amount, true);
        if (took <= 0) return 0;
        journal.updateSnapshots(tx);
        return (int) tank.drain(took, false);
    }
}
