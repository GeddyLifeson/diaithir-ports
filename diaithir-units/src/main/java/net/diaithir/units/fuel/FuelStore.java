package net.diaithir.units.fuel;

/** What a tank, jerry can, pump or vehicle exposes. Quantities are litres (liquids) or kg (solids, batteries). */
public interface FuelStore {
    Fuel fuel();
    double quantity();
    double capacity();

    /** Adds up to {@code amount} of {@code f}; returns what was accepted. Refuses a different fuel unless empty. */
    double insert(Fuel f, double amount);

    /** Removes up to {@code amount}; returns what was taken. */
    double extract(double amount);

    default double free() { return capacity() - quantity(); }

    /** Moves up to {@code amount} from {@code from} to {@code to}; returns what moved. Never creates or loses fuel. */
    static double transfer(FuelStore from, FuelStore to, double amount) {
        Fuel f = from.fuel();
        if (f == null || amount <= 0) return 0;
        double want = Math.min(Math.min(amount, from.quantity()), to.free());
        double taken = from.extract(want);
        double put = to.insert(f, taken);
        if (put < taken) from.insert(f, taken - put);
        return put;
    }
}
