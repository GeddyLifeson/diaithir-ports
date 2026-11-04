package net.diaithir.bosses;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Age Wardens: one mini-boss per Age (I to VI, and VIII). Each is an ordinary boss profile (tier {@code age_warden}, id
 * {@code age_warden_<age>}) built on the module's existing attacks, so the controller, bar, arena, Age-gated loot, no-despawn
 * flag and the Dwarfs and Giants / Remnants guards all apply unchanged. This class is the clean entry point for the rites:
 * <pre>
 *   AgeWardens.spawn(level, age, pos, altarOrNull)      // the warden of that Age (the nearest one at or below it)
 *   AgeWardens.spawnCurrent(level, pos, altarOrNull)    // the warden of the world's current Age
 *   AgeWardens.profileFor(age)                          // the Data.Profile, e.g. for display
 * </pre>
 * Age 7 (Dubhan) has no warden of its own; it gets the Age VI one. The Age VIII warden is stored under age 8 (Rodos in Ages).
 * Listen for {@link EventHooks.BossDefeatedEvent} (boss id {@code age_warden_<age>}) to learn that a rite's warden fell.
 */
public final class AgeWardens {
    public static final String TIER = "age_warden", PREFIX = "age_warden_";

    /** Ages that have a warden, ascending. */
    public static List<Integer> ages() {
        List<Integer> out = new ArrayList<>();
        for (Data.Profile p : Data.profiles().values()) if (TIER.equals(p.tier)) out.add(p.age);
        out.sort(null);
        return out;
    }

    /** The warden profile id for an Age: the highest warden Age at or below it (Age 7 gives the Age VI warden), else the lowest. */
    public static @Nullable String idFor(int age) {
        List<Integer> ages = ages();
        if (ages.isEmpty()) return null;
        int best = ages.get(0);
        for (int a : ages) if (a <= age) best = a;
        return PREFIX + best;
    }

    public static Data.@Nullable Profile profileFor(int age) {
        String id = idFor(age);
        return id == null ? null : Data.profiles().get(id);
    }

    /** Spawns the warden of an Age. altar (may be null) is the altar that holds the arena and the rest rule. Returns null when none exists. */
    public static @Nullable Mob spawn(ServerLevel level, int age, Vec3 at, @Nullable BlockPos altar) {
        Data.Profile p = profileFor(age);
        return p == null ? null : DiaithirBosses.spawn(level, p, at, altar);
    }

    /** Spawns the warden of the world's current Age. */
    public static @Nullable Mob spawnCurrent(ServerLevel level, Vec3 at, @Nullable BlockPos altar) {
        return spawn(level, Ages.current(level.getServer()), at, altar);
    }

    private AgeWardens() {}
}
