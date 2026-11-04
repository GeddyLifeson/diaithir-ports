// Ported from Enchantment Descriptions (LGPL-2.1-only) https://github.com/Darkhax-Minecraft/Enchantment-Descriptions
// Modifications: Feature entry point; client-only, registers client config and tooltip listener via NeoForge events (no mixins, no Prickle).
package net.diaithir.tooltips.enchantmentdescriptions;

import net.diaithir.tooltips.enchantmentdescriptions.client.EnchDescClient;
import net.diaithir.tooltips.enchantmentdescriptions.config.EnchDescConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, EnchDescConfig.SPEC, "diaithir_tooltipfx_enchantmentdescriptions-client.toml");
        EnchDescClient.init();
    }
}
