package com.diaithir.overworld_biomes.realms;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Clean-room replacement for The Aether II: same modid and ids, original worldgen. */
@Mod(AetherIIRealm.MOD_ID)
public final class AetherIIRealm {
    public static final String MOD_ID = "aether_ii";

    public AetherIIRealm(IEventBus bus) {
        RealmRegistrar.register(MOD_ID, bus);
    }
}
