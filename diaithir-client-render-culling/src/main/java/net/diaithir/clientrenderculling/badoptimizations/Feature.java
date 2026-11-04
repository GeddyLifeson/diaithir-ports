// Ported from BadOptimizations (MIT) https://github.com/ItsThosea/BadOpitmizations (26.1)
// Modifications: no @Mod of its own, Architectury platform layer merged, config screen dropped (text config file kept), hosted in diaithir-client-render-culling.
package net.diaithir.clientrenderculling.badoptimizations;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public final class Feature {
    private Feature() {}

    /** Everything is driven by badoptimizations.mixins.json and its mixin plugin; config is config/badoptimizations.txt. */
    public static void init(IEventBus modBus, ModContainer container) {}
}
