// Ported from Alternate Current (MIT) https://github.com/SpaceWalkerRS/alternate-current (neoforge-26.1)
// Modifications: no @Mod of its own; registered from diaithir-worldsys; upstream package alternate.current kept for its mixin config.
package net.diaithir.worldsys.alternatecurrent;

import alternate.current.AlternateCurrentMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.register(AlternateCurrentMod.ModEvents.class);
    }
}
