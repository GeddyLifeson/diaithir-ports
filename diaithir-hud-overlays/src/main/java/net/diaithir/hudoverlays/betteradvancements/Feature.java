package net.diaithir.hudoverlays.betteradvancements;

import net.diaithir.hudoverlays.betteradvancements.client.AdvancementsClient;
import net.diaithir.hudoverlays.betteradvancements.config.AdvancementsConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, AdvancementsConfig.SPEC, "diaithir_hud_betteradvancements-client.toml");
        AdvancementsClient.init(modBus, container);
    }
}
