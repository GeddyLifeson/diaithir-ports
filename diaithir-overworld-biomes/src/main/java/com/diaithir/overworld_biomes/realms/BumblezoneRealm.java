package com.diaithir.overworld_biomes.realms;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Clean-room replacement for The Bumblezone: same modid and ids, original worldgen. */
@Mod(BumblezoneRealm.MOD_ID)
public final class BumblezoneRealm {
    public static final String MOD_ID = "the_bumblezone";

    public BumblezoneRealm(IEventBus bus) {
        RealmRegistrar.register(MOD_ID, bus);
    }
}
