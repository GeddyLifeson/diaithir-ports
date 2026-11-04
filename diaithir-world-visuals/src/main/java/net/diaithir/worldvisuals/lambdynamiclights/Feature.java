package net.diaithir.worldvisuals.lambdynamiclights;

import net.diaithir.worldvisuals.lambdynamiclights.client.ClientHandler;
import net.diaithir.worldvisuals.lambdynamiclights.client.DynamicLightsConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, DynamicLightsConfig.SPEC, "worldvisuals-dynamiclights.toml");
        ClientHandler.init(modBus);
    }
}
