// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.jspecify.annotations.Nullable;

/** Capabilities other vehicles register to join the fold: health (hammer repair, crash rules) and a tow bar. */
public final class CrayfishCaps {
    public static final EntityCapability<VehicleHealth, @Nullable Void> HEALTH = EntityCapability.createVoid(Identifier.fromNamespaceAndPath("diaithir", "vehicle_health"), VehicleHealth.class);
    public static final EntityCapability<TowPoint, @Nullable Void> TOW = EntityCapability.createVoid(Identifier.fromNamespaceAndPath("diaithir", "tow_point"), TowPoint.class);

    private CrayfishCaps() {}
}
