// Ported from Falling Leaves NeoForge (LGPL-3.0-only) https://github.com/cheaterpaul/fallingleaves (neo/26.1)
// Modifications: no @Mod of its own; upstream packages kept (mixin config), event subscribers bound to diaithirworldvisuals, datagen dropped.
package net.diaithir.worldvisuals.fallingleaves;

import de.cheaterpaul.fallingleaves.FallingLeavesMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        FallingLeavesMod.init(modBus, container);
    }
}
