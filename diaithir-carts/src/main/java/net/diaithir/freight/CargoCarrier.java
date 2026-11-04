package net.diaithir.freight;

import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Anything that carries a {@link CargoBay}: a trailer, truck bed, flatcar, wagon, ship hold, or a depot floor. Vehicle entities offer it through
 * {@link Freight#CARRIER}; things that are not entities (a ship built from blocks) add a finder to {@link Carriers#SOURCES}. The carrier owns the bay and its
 * saving; freight code puts loads in and takes them out, and calls {@link #changed()} afterwards.
 */
public interface CargoCarrier {
    CargoBay bay();

    /** Bay column {x, z} under a world point (where a player aimed), or null when the point is off the deck. */
    @Nullable int[] columnAt(Vec3 world);

    /** Called after loads or their items changed, so the carrier can save and sync. */
    void changed();
}
