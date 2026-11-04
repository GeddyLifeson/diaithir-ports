// Ported from ImmediatelyFast (LGPL-3.0-or-later) https://github.com/RaphiMC/ImmediatelyFast (26.1)
// Modifications: no @Mod of its own, ServiceLoader replaced by direct platform service, classtweaker converted to access transformer; upstream packages kept for the mixin config.
package net.diaithir.clientrenderculling.immediatelyfast;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public final class Feature {
    private Feature() {}

    /** Mixins and the reload listener are bound through immediatelyfast-common.mixins.json and ImmediatelyFastNeoForge. */
    public static void init(IEventBus modBus, ModContainer container) {}
}
