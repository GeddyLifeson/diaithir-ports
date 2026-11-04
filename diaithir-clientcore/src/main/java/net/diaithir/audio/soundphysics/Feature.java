// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: NeoForge-native feature entry; ModConfigSpec config replaces YACL; mixins retained for sound engine hooks only.
package net.diaithir.audio.soundphysics;

import net.diaithir.audio.soundphysics.client.SoundPhysicsClient;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, SoundPhysicsConfig.SPEC, "diaithir_audio_soundphysics-client.toml");
        SoundPhysicsClient.init(modBus, container);
    }
}
