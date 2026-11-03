package net.diaithir.vehicles.fuel;

/** Plain implementation of {@link FuelStore} that vehicles embed and save with {@link #amountL()} and {@link #fuelId()}. */
public class FuelTank implements FuelStore {
    private final long capacity;
    private long amount;
    private String fuel;

    public FuelTank(long capacityL) {
        this.capacity = Math.max(0, capacityL);
    }

    @Override
    public long capacityL() {
        return capacity;
    }

    @Override
    public long amountL() {
        return amount;
    }

    @Override
    public String fuelId() {
        return amount > 0 ? fuel : null;
    }

    @Override
    public long fill(String fuelId, long l, boolean simulate) {
        if (fuelId == null || l <= 0 || Fuel.get(fuelId) == null) return 0;
        if (amount > 0 && !fuelId.equals(fuel)) return 0;
        long put = Math.min(l, capacity - amount);
        if (put > 0 && !simulate) {
            fuel = fuelId;
            amount += put;
        }
        return Math.max(put, 0);
    }

    @Override
    public long drain(long l, boolean simulate) {
        long took = Math.min(Math.max(l, 0), amount);
        if (took > 0 && !simulate) {
            amount -= took;
            if (amount == 0) fuel = null;
        }
        return took;
    }

    /** Restore from saved data (clamped; unknown fuel clears the tank). */
    public void load(String fuelId, long l) {
        if (fuelId == null || Fuel.get(fuelId) == null || l <= 0) {
            fuel = null;
            amount = 0;
        } else {
            fuel = fuelId;
            amount = Math.min(l, capacity);
        }
    }

    private double credit; // joules of already-drained fuel not yet used (under one litre; not saved)

    /**
     * Burns fuel to supply {@code joules} of shaft work at the given efficiency (0..1). Returns the joules actually delivered
     * (less than asked if the tank ran dry). Litres are drained whole, and the unused part of the last litre is kept as credit,
     * so many small per-tick calls use the right total. This is the one place vehicles turn power into fuel use.
     */
    public double burn(double joules, double efficiency) {
        if (joules <= 0 || efficiency <= 0) return 0;
        double delivered = 0;
        while (joules - delivered > 1e-6) {
            if (credit <= 1e-9) {
                if (amount <= 0) break;
                Fuel f = Fuel.get(fuel);
                drain(1, false);
                credit += f.joulesPerLitre() * efficiency;
            }
            double use = Math.min(credit, joules - delivered);
            credit -= use;
            delivered += use;
        }
        return delivered;
    }

    /** State copy for transaction rollback. */
    public record Snapshot(String fuel, long amount, double credit) {}

    public Snapshot snapshot() {
        return new Snapshot(fuel, amount, credit);
    }

    public void restore(Snapshot s) {
        fuel = s.fuel();
        amount = s.amount();
        credit = s.credit();
    }
}
