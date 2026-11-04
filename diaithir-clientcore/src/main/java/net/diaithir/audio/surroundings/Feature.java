// Ported from Dynamic Surroundings Rebirth (MIT) https://github.com/uawyiegrfv/DynamicSurroundingsRebirth
// Modifications: reduced to biome/weather/footstep ambience on NeoForge events and vanilla sounds; no libs; client-only.
package net.diaithir.audio.surroundings;

import net.diaithir.audio.surroundings.client.SurroundingsClient;
import net.diaithir.audio.surroundings.config.SurroundingsConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, SurroundingsConfig.SPEC, "diaithir_audio_surroundings-client.toml");
        SurroundingsClient.init(modBus);
    }
}
