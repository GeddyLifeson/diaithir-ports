package com.diaithir.overworld_biomes.realms;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Clean-room replacement for Aerial Hell: same modid and ids, original worldgen. */
@Mod(AerialHellRealm.MOD_ID)
public final class AerialHellRealm {
    public static final String MOD_ID = "aerialhell";

    public AerialHellRealm(IEventBus bus) {
        RealmRegistrar.register(MOD_ID, bus);
    }
}
