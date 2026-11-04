package net.diaithir.bridges;

import net.diaithir.units.fuel.Engine;
import net.diaithir.units.fuel.Fuel;
import net.diaithir.units.fuel.FuelStore;
import net.diaithir.units.fuel.Fuels;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Carrier tank: holds one fuel from the shared carrier table (diesel, coal, hay, hydrogen, a battery bank, or mana). It is a {@link FuelStore} on every
 * face ({@link Ports#FUEL}), a fluid port for liquid fuels (tag {@code c:<fuel id>}, 1 mB = 1 L), a comparator signal, and it feeds any engine that
 * lists the fuel ({@link #feed}).
 */
public final class CarrierTankEntity extends BlockEntity {
    private Fuel fuel;
    private double qty;
    private final Store store = new Store();
    private final Port port = new Port();

    CarrierTankEntity(BlockPos pos, BlockState state) { super(DiaithirBridges.CARRIER_TANK_ENTITY.get(), pos, state); }

    static double capacityNow() { return Config.get(Config.TANK_CAPACITY); }

    public FuelStore store() { return store; }
    ResourceHandler<FluidResource> fluidPort() { return port; }

    private void changed() {
        if (qty <= 1e-9) { qty = 0; fuel = null; }
        setChanged();
    }

    /** True when the tank holds a fuel that {@code e} accepts. */
    public boolean canFeed(Engine e) { return fuel != null && qty > 0 && e.accepts(fuel.id()); }

    /**
     * Takes the fuel an engine burns to give {@code powerW} at the shaft for {@code seconds} and returns the shaft joules actually delivered
     * (less than asked when the tank runs dry; 0 when the engine does not accept the fuel).
     */
    public double feed(Engine e, double powerW, double seconds) {
        if (!canFeed(e) || powerW <= 0 || seconds <= 0) return 0;
        double need = powerW * seconds / e.efficiency() / fuel.joulesPerUnit();
        double take = Math.min(need, qty);
        double delivered = take * fuel.joulesPerUnit() * e.efficiency();
        qty -= take;
        changed();
        return delivered;
    }

    int signal() { return Ports.level(qty / capacityNow()); }

    String describe() {
        if (fuel == null) return "Empty, takes any carrier fuel (" + (int) capacityNow() + " units)";
        return fuel.name() + " " + Math.round(qty * 10) / 10.0 + " / " + (int) capacityNow() + " " + fuel.unit();
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (fuel != null) { out.putString("fuel", fuel.id()); out.putDouble("qty", qty); }
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        fuel = in.getString("fuel").map(Fuels::get).orElse(null);
        qty = fuel == null ? 0 : in.getDoubleOr("qty", 0);
    }

    private final class Store implements FuelStore {
        @Override public Fuel fuel() { return qty > 0 ? fuel : null; }
        @Override public double quantity() { return qty; }
        @Override public double capacity() { return capacityNow(); }
        @Override public double insert(Fuel f, double amount) {
            if (f == null || amount <= 0 || (qty > 0 && fuel != f)) return 0;
            double put = Math.min(amount, free());
            if (put <= 0) return 0;
            fuel = f; qty += put; changed();
            return put;
        }
        @Override public double extract(double amount) {
            double took = Math.max(0, Math.min(amount, qty));
            if (took > 0) { qty -= took; changed(); }
            return took;
        }
    }

    private record Snap(Fuel fuel, double qty) {}

    /** Fluid port: whole litres of a liquid fuel, all or nothing inside a transaction. */
    private final class Port implements ResourceHandler<FluidResource> {
        private final SnapshotJournal<Snap> journal = new SnapshotJournal<>() {
            @Override protected Snap createSnapshot() { return new Snap(fuel, qty); }
            @Override protected void revertToSnapshot(Snap s) { fuel = s.fuel(); qty = s.qty(); }
            @Override protected void onRootCommit(Snap original) { changed(); }
        };

        @Override public int size() { return 1; }
        @Override public FluidResource getResource(int i) { return fuel != null && qty >= 1 ? Ports.fluidOf(fuel) : FluidResource.EMPTY; }
        @Override public long getAmountAsLong(int i) { return fuel != null && fuel.kind() == Fuel.Kind.LIQUID ? (long) Math.floor(qty) : 0; }
        @Override public long getCapacityAsLong(int i, FluidResource r) { return (long) capacityNow(); }
        @Override public boolean isValid(int i, FluidResource r) { return Ports.fuelOf(r) != null; }

        @Override public int insert(int i, FluidResource r, int amount, TransactionContext tx) {
            Fuel f = Ports.fuelOf(r);
            if (f == null || amount <= 0 || (qty > 0 && fuel != f)) return 0;
            int put = (int) Math.min(amount, Math.floor(capacityNow() - qty));
            if (put <= 0) return 0;
            journal.updateSnapshots(tx);
            fuel = f; qty += put;
            return put;
        }

        @Override public int extract(int i, FluidResource r, int amount, TransactionContext tx) {
            if (fuel == null || fuel.kind() != Fuel.Kind.LIQUID || Ports.fuelOf(r) != fuel || amount <= 0) return 0;
            int take = (int) Math.min(amount, Math.floor(qty));
            if (take <= 0) return 0;
            journal.updateSnapshots(tx);
            qty -= take;
            return take;
        }
    }
}
