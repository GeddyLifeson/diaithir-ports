package net.diaithir.vehicles;

import net.diaithir.units.fuel.Engine;
import net.diaithir.units.fuel.Fuel;
import net.diaithir.units.fuel.FuelStore;

/** The same maths as the Carrier Tank's {@code feed}, for a store that is not a tank (a vehicle's own tank): fuel burnt = P*t / efficiency / J-per-unit. */
public final class FuelBurn {
    private FuelBurn() {}

    public static boolean canFeed(FuelStore s, Engine e) {
        Fuel f = s.fuel();
        return f != null && s.quantity() > 0 && e.accepts(f.id());
    }

    /** Burns fuel for {@code powerW} over {@code seconds}; returns the shaft joules actually delivered (0 if the engine refuses the fuel). */
    public static double feed(FuelStore s, Engine e, double powerW, double seconds) {
        if (!canFeed(s, e) || powerW <= 0 || seconds <= 0) return 0;
        Fuel f = s.fuel();
        double need = powerW * seconds / e.efficiency() / f.joulesPerUnit();
        double took = s.extract(need);
        return took * f.joulesPerUnit() * e.efficiency();
    }
}
