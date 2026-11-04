// Ported from Extreme Sound Muffler (LGPL-3.0-only) https://github.com/LeoBeliik/ExtremeSoundMuffler
// Modifications: Clean NeoForge port, no mixins/art; per-sound volume GUI, hotkey, no anchors/block.
package net.diaithir.audio.soundmuffler;

import net.diaithir.audio.soundmuffler.client.MufflerClient;
import net.diaithir.audio.soundmuffler.client.MufflerConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, MufflerConfig.SPEC, "diaithir_audio_soundmuffler-client.toml");
        MufflerClient.init(modBus);
    }
}
