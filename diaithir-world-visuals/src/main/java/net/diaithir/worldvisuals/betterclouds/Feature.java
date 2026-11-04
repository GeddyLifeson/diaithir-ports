package net.diaithir.worldvisuals.betterclouds;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

/** Better Clouds (clean-room behaviour, no upstream code): config plus a mixin on the vanilla CloudRenderer. */
public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, CloudConfig.SPEC, "diaithir_worldvisuals_betterclouds-client.toml");
    }
}
