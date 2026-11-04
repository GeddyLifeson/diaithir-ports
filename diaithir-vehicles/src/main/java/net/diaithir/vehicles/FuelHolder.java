package net.diaithir.vehicles;

import net.diaithir.units.fuel.FuelStore;

/**
 * Implemented by vehicle entities (cars, tank trailers, tracked chassis, ships, aircraft) that carry fuel. Blocks expose their store through
 * {@code Ports.FUEL} (diaithir-bridges); entities have no block capability, so pumps and jerry cans find them through this interface.
 */
public interface FuelHolder {
    FuelStore fuelStore();
}
