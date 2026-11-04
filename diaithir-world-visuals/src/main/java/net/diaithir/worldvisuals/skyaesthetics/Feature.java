// Ported from Sky Aesthetics (MIT) https://github.com/TathanDev/SkyAesthetics (26.1)
// Modifications: Architectury/exoconfig dropped, no @Mod of its own, access widener converted to access transformer, hosted in diaithir-world-visuals.
package net.diaithir.worldvisuals.skyaesthetics;

import fr.tathan.SkyAesthetics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        SkyAesthetics.init();
    }
}
