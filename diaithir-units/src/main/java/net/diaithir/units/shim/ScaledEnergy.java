package net.diaithir.units.shim;

import net.diaithir.units.Units;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Presents a third-party store that counts in another unit (Mekanism joules, EU, AE, mana...) as a plain
 * joule/FE handler. Amounts are converted with the pack ratio and always rounded so that no energy is created:
 * a store takes whole foreign units only, and a caller is told it moved exactly what the store really took.
 */
public final class ScaledEnergy implements EnergyHandler {
    private final EnergyHandler inner;
    private final double jPerUnit;

    /** inner counts in the unit with the given id (e.g. "mekanism:joule"). */
    public ScaledEnergy(EnergyHandler inner, String foreignUnitId) {
        this.inner = inner;
        this.jPerUnit = Units.get(foreignUnitId).factor();
    }

    @Override public long getAmountAsLong() { return (long) Math.floor(inner.getAmountAsLong() * jPerUnit); }

    @Override public long getCapacityAsLong() { return (long) Math.floor(inner.getCapacityAsLong() * jPerUnit); }

    @Override public int insert(int joules, TransactionContext tx) {
        int units = (int) Math.floor(joules / jPerUnit);
        if (units <= 0) return 0;
        int took = inner.insert(units, tx);
        return (int) Math.min(joules, Math.floor(took * jPerUnit));
    }

    @Override public int extract(int joules, TransactionContext tx) {
        int units = (int) Math.floor(joules / jPerUnit);
        if (units <= 0) return 0;
        int gave = inner.extract(units, tx);
        return (int) Math.min(joules, Math.floor(gave * jPerUnit));
    }
}
