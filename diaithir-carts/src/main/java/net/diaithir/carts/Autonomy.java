package net.diaithir.carts;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.IntSupplier;

/**
 * Crewed or autonomous. Before {@link #AUTONOMY_AGE} (Age VII) a scheduled vehicle only runs while someone drives it (a player or any other living rider,
 * villagers and NPCs included). From that Age on it runs with no crew. Other mods read {@link #status} (for example the wages thread, which charges
 * crew only while the status is CREWED) and the Age comes from {@link #ageSupplier}, which the Ages module replaces at startup.
 */
public final class Autonomy {
    private Autonomy() {}

    public static final int AUTONOMY_AGE = 7;

    public enum Status { UNCREWED, CREWED, AUTONOMOUS }

    /** The current server Age. Replaced by the Ages module; 0 until then, so nothing is autonomous by accident. */
    public static volatile IntSupplier ageSupplier = () -> 0;

    public static boolean unlocked() {
        return ageSupplier.getAsInt() >= AUTONOMY_AGE;
    }

    /** Crew jobs that automation replaces at Age VII. */
    public static final java.util.Set<String> AUTOMATED_JOBS = java.util.Set.of("driver", "train_driver", "cart_driver", "crane_operator", "machine_operator");

    /** The predicate behind global.AUTONOMOUS_CREW(job, culture). The Age is server-wide, so culture does not change the answer; it is kept so callers can pass both. */
    public static boolean autonomousCrew(String job, String culture) {
        return unlocked() && AUTOMATED_JOBS.contains(job);
    }

    public static Status status(Entity vehicle) {
        if (unlocked()) return Status.AUTONOMOUS;
        for (Entity rider : vehicle.getPassengers()) if (rider instanceof net.minecraft.world.entity.LivingEntity && !(rider instanceof Player p && p.isSpectator())) return Status.CREWED;
        return Status.UNCREWED;
    }

    /** True when the vehicle may run its schedule now. */
    public static boolean mayRun(Entity vehicle) {
        return status(vehicle) != Status.UNCREWED;
    }
}
