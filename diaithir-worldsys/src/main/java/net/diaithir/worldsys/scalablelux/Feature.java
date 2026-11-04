// Ported from ScalableLux (LGPL-3.0-only) https://github.com/RelativityMC/ScalableLux (ver/26.1.0) with FlowSched (MIT)
// Modifications: Fabric entrypoint replaced by this Feature, Fabric incompat scan dropped, access widener converted to access transformer, FlowSched bundled under ca.spottedleaf.starlight.interndep.
package net.diaithir.worldsys.scalablelux;

import ca.spottedleaf.starlight.common.config.Config;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        Config.init();
    }
}
