package com.diaithir.overworld_biomes.realms;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Clean-room replacement for The Undergarden: same modid and ids, original worldgen. */
@Mod(UndergardenRealm.MOD_ID)
public final class UndergardenRealm {
    public static final String MOD_ID = "undergarden";

    public UndergardenRealm(IEventBus bus) {
        RealmRegistrar.register(MOD_ID, bus);
    }
}
