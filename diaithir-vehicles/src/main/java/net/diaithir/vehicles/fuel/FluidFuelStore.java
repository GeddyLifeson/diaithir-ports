package net.diaithir.vehicles.fuel;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Any NeoForge fluid handler (another mod's tank) seen as a {@link FuelStore}, so our pumps and cans work with it. Only fuel fluids pass. */
public final class FluidFuelStore implements FuelStore {
    private final ResourceHandler<FluidResource> h;

    public FluidFuelStore(ResourceHandler<FluidResource> handler) {
        this.h = handler;
    }

    @Override
    public long capacityL() {
        long c = 0;
        for (int i = 0; i < h.size(); i++) {
            FluidResource r = h.getResource(i);
            if (r.isEmpty() || Fuels.of(r.getFluid()) != null) c += h.getCapacityAsLong(i, r.isEmpty() ? probe() : r);
        }
        return c;
    }

    private FluidResource probe() {
        for (Fuel f : Fuel.all()) {
            Fluid fl = Fuels.fluidFor(f);
            if (fl != null) return FluidResource.of(fl);
        }
        return FluidResource.EMPTY;
    }

    @Override
    public long amountL() {
        long a = 0;
        for (int i = 0; i < h.size(); i++) if (Fuels.of(h.getResource(i).getFluid()) != null) a += h.getAmountAsLong(i);
        return a;
    }

    @Override
    public String fuelId() {
        for (int i = 0; i < h.size(); i++) {
            Fuel f = h.getAmountAsLong(i) > 0 ? Fuels.of(h.getResource(i).getFluid()) : null;
            if (f != null) return f.id();
        }
        return null;
    }

    @Override
    public long fill(String fuelId, long l, boolean simulate) {
        Fuel f = Fuel.get(fuelId);
        Fluid fl = f == null ? null : Fuels.fluidFor(f);
        if (fl == null || l <= 0) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int n = h.insert(FluidResource.of(fl), (int) Math.min(l, Integer.MAX_VALUE), tx);
            if (!simulate) tx.commit();
            return n;
        }
    }

    @Override
    public long drain(long l, boolean simulate) {
        String id = fuelId();
        Fuel f = id == null ? null : Fuel.get(id);
        Fluid fl = f == null ? null : Fuels.fluidFor(f);
        if (fl == null || l <= 0) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int n = h.extract(FluidResource.of(fl), (int) Math.min(l, Integer.MAX_VALUE), tx);
            if (!simulate) tx.commit();
            return n;
        }
    }
}
