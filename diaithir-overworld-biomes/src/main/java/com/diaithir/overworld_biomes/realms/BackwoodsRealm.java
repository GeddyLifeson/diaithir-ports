package com.diaithir.overworld_biomes.realms;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Clean-room replacement for The Backwoods: same modid and ids, original worldgen. */
@Mod(BackwoodsRealm.MOD_ID)
public final class BackwoodsRealm {
    public static final String MOD_ID = "the_backwoods";

    public BackwoodsRealm(IEventBus bus) {
        RealmRegistrar.register(MOD_ID, bus);
    }
}
