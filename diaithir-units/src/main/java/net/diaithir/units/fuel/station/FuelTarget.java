package net.diaithir.units.fuel.station;

import net.diaithir.units.fuel.Fuel;

/** Something a pump can fill: a vehicle tank, a tank car, a ship. Litres in and out. */
public interface FuelTarget {
    /** Litres of {@code f} this target would accept now; 0 when it holds another fuel or cannot burn this one. */
    double free(Fuel f);

    /** Adds up to {@code litres} of {@code f}; returns what was accepted. */
    double fill(Fuel f, double litres);
}
