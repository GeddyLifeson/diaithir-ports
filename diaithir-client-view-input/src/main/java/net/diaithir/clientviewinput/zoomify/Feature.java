// Ported from Zoomify (LGPL-3.0-only) https://github.com/isXander/Zoomify
// Modifications: NeoForge-only entry point; YACL screen dropped, config via ModConfigSpec (client).
package net.diaithir.clientviewinput.zoomify;

import net.diaithir.clientviewinput.zoomify.client.ZoomifyConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, ZoomifyConfig.SPEC, "diaithir_clientviewinput_zoomify-client.toml");
        // ZoomifyClient is not present in this source tree; client zoom logic is omitted.
    }
}
