// Ported from Mouse Tweaks (BSD-3-Clause) https://github.com/YaLTeR/MouseTweaks
// Modifications: Feature entry point; client-only, registers a ModConfigSpec client config and the NeoForge screen event hooks.
package net.diaithir.inventoryui.mousetweaks;

import net.diaithir.inventoryui.mousetweaks.client.MouseTweaksClient;
import net.diaithir.inventoryui.mousetweaks.client.MouseTweaksConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, MouseTweaksConfig.SPEC, "diaithir_inventoryui_mousetweaks-client.toml");
        MouseTweaksClient.init(modBus);
    }
}
