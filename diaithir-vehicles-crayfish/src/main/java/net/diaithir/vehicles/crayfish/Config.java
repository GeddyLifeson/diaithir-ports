// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server settings (no player-facing switches in the pack; these are for server owners). */
public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue VEHICLE_DAMAGE;
    public static final ModConfigSpec.DoubleValue MAX_HOSE_DISTANCE;
    public static final ModConfigSpec.DoubleValue TRAILER_DETACH;
    public static final ModConfigSpec.IntValue PUMP_LITRES_PER_USE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        VEHICLE_DAMAGE = b.comment("Vehicles take damage from hits and hard landings").define("vehicleDamage", true);
        MAX_HOSE_DISTANCE = b.comment("Metres a player can walk from a pump with the hose").defineInRange("maxHoseDistance", 10.0, 2.0, 64.0);
        TRAILER_DETACH = b.comment("Metres beyond the hitch length at which a trailer snaps free").defineInRange("trailerDetachThreshold", 6.0, 1.0, 64.0);
        PUMP_LITRES_PER_USE = b.comment("Litres a pump moves per use of the nozzle").defineInRange("pumpLitresPerUse", 50, 1, 1000);
        SPEC = b.build();
    }

    private Config() {}
}
