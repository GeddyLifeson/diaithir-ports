package net.diaithir.entityanimation.skinlayers3d;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, net.diaithir.entityanimation.skinlayers3d.client.SkinLayersConfig.SPEC, "diaithir-entity-animation-skinlayers3d.toml");
        net.diaithir.entityanimation.skinlayers3d.client.ClientSetup.register(modBus);
    }
}
