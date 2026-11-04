// Ported from GUI Scaler (LGPL-3.0-only) https://github.com/zendiik/gui-scaler
// Modifications: NeoForge-only entry point; the Minecraft#resizeGui mixin is replaced by a client tick window-size check.
package net.diaithir.clientviewinput.guiscaler;

import net.diaithir.clientviewinput.guiscaler.client.GuiScalerClient;
import net.diaithir.clientviewinput.guiscaler.config.GuiScalerConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, GuiScalerConfig.SPEC, "diaithir_clientview_guiscaler-client.toml");
        GuiScalerClient.init(modBus);
    }
}
