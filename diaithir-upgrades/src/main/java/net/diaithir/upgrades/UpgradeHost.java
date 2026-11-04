package net.diaithir.upgrades;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;

/**
 * Implemented by (or exposed from) any block entity that takes modules. Query it from anywhere:
 * {@code level.getCapability(UpgradeHost.CAP, pos, null)}. A mod with no code of its own can ship
 * a Target rule instead and the host layer wraps its block entity.
 */
public interface UpgradeHost {
    BlockCapability<UpgradeHost, Void> CAP =
            BlockCapability.createVoid(Identifier.fromNamespaceAndPath("diaithirupgrades", "host"), UpgradeHost.class);

    /** Installed modules and slot rules. */
    Slotted slots();

    /** Beacons currently covering this block; the host layer keeps the list current. */
    default List<Beacon> beacons() { return List.of(); }

    /** Final clamped effect: own modules plus beacons. */
    default Effects effects() { return Resolver.resolve(slots(), beacons()); }

    /** Power draw in watts for a base draw (1 FE/t = 20 W). */
    default double powerWatts(double baseWatts) { return Resolver.powerWatts(baseWatts, effects()); }

    /** Work done this tick: 1.0 normally; call from the machine's tick and add it to progress. */
    default double progressPerTick() { return effects().speedMultiplier(); }

    /** Output items for a craft: base count plus productivity, rolled with roll in [0,1). */
    default int outputCount(int base, double roll) {
        double exact = base * effects().outputMultiplier();
        int whole = (int) exact;
        return roll < exact - whole ? whole + 1 : whole;
    }
}
