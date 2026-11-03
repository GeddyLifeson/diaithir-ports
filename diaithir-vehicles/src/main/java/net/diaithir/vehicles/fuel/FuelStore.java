package net.diaithir.vehicles.fuel;

/**
 * The shared fuel capability. A road vehicle, ship, aircraft, pump, tank or jerry can exposes one of these; amounts are whole litres
 * (1 L = 1 mB, the pack's fluid unit; a bucket is 1000 L). One fuel kind at a time: a store that holds diesel refuses petrol until it is empty.
 */
public interface FuelStore {
    long capacityL();

    long amountL();

    /** Id of the fuel held, or null when empty. */
    String fuelId();

    /** Fills up to {@code l}; returns how much was (or would be) accepted. Never mixes fuels. */
    long fill(String fuelId, long l, boolean simulate);

    /** Drains up to {@code l}; returns how much was (or would be) removed. */
    long drain(long l, boolean simulate);

    default boolean isEmpty() {
        return amountL() <= 0;
    }

    default long spaceL() {
        return capacityL() - amountL();
    }

    /** Stored chemical energy in joules. */
    default double energyJoules() {
        Fuel f = fuelId() == null ? null : Fuel.get(fuelId());
        return f == null ? 0 : amountL() * f.joulesPerLitre();
    }

    /** Moves as much as fits from this store into {@code to}; returns the litres moved. */
    default long transferTo(FuelStore to, long maxL) {
        String id = fuelId();
        if (id == null || maxL <= 0) return 0;
        long can = Math.min(Math.min(maxL, amountL()), to.fill(id, maxL, true));
        if (can <= 0) return 0;
        long took = drain(can, false);
        long put = to.fill(id, took, false);
        if (put < took) fill(id, took - put, false); // give back what the target refused
        return put;
    }
}
