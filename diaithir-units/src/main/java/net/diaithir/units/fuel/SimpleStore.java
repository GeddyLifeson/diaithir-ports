package net.diaithir.units.fuel;

/** Plain in-memory {@link FuelStore}; hosts wrap it in a block entity, item component or vehicle. */
public final class SimpleStore implements FuelStore {
    private final double capacity;
    private Fuel fuel;
    private double quantity;

    public SimpleStore(double capacity) { this.capacity = capacity; }

    @Override public Fuel fuel() { return quantity > 0 ? fuel : null; }
    @Override public double quantity() { return quantity; }
    @Override public double capacity() { return capacity; }

    @Override public double insert(Fuel f, double amount) {
        if (amount <= 0 || (quantity > 0 && fuel != f)) return 0;
        double put = Math.min(amount, free());
        if (put > 0) { fuel = f; quantity += put; }
        return put;
    }

    @Override public double extract(double amount) {
        double took = Math.max(0, Math.min(amount, quantity));
        quantity -= took;
        return took;
    }
}
