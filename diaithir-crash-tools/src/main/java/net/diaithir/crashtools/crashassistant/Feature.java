package net.diaithir.crashtools.crashassistant;

import net.diaithir.crashtools.crashassistant.config.CrashToolsConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, CrashToolsConfig.SPEC, "crashtools-crashassistant-client.toml");
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            net.diaithir.crashtools.crashassistant.client.ClientBootstrap.init(modBus);
        }
    }
}
