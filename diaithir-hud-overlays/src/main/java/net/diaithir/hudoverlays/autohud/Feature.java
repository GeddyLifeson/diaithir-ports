// Ported from AutoHUD (LGPL-3.0-only) https://github.com/Crendgrim/AutoHUD
// Modifications: Clean NeoForge 26.1.2 port using RenderGuiLayerEvent Pre/Post for alpha fade + cancel, ModConfigSpec config, no mixins.
package net.diaithir.hudoverlays.autohud;

import net.diaithir.hudoverlays.autohud.client.AutoHudClient;
import net.diaithir.hudoverlays.autohud.config.AutoHudConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, AutoHudConfig.SPEC, "diaithir_hud_autohud-client.toml");
        AutoHudClient.init(modBus);
    }
}
