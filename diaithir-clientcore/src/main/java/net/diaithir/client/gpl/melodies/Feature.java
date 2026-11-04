// GPL-3.0-only module: Immersive Melodies by Luke100000 https://github.com/Luke100000/ImmersiveMelodies (branch 26.1.2).
// Its code lives in package immersive_melodies; licences/immersive-melodies-GPL-3.0.txt applies to that package and its assets.
// Modifications: NeoForge entry points folded into diaithir-clientcore; event subscribers bound to diaithirclientcore.
package net.diaithir.client.gpl.melodies;

import immersive_melodies.neoforge.CommonNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        CommonNeoForge.init(modBus);
    }
}
