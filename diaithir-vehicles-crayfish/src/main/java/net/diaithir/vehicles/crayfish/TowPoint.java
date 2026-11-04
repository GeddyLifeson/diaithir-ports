// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Where a trailer hooks on. A vehicle registers {@link CrayfishCaps#TOW}; anything else (a player) is pulled from its feet. */
public interface TowPoint {
    Vec3 towBar();

    static Vec3 of(Entity e) {
        TowPoint t = e.getCapability(CrayfishCaps.TOW, null);
        return t != null ? t.towBar() : e.position();
    }
}
