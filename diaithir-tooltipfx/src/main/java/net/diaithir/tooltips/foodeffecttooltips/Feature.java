// Ported from Food Effect Tooltips (LGPL-3.0-only) https://github.com/DaFuqs/FoodEffectTooltips
// Modifications: Feature entry point; client-only, config registered as CLIENT, no @Mod/ConfigurationScreen, handler registered on NeoForge.EVENT_BUS.
package net.diaithir.tooltips.foodeffecttooltips;

import net.diaithir.tooltips.foodeffecttooltips.client.FoodEffectTooltipsClient;
import net.diaithir.tooltips.foodeffecttooltips.config.FoodEffectsConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, FoodEffectsConfig.CONFIG_SPEC, "diaithir_tooltipfx_foodeffecttooltips-client.toml");
        FoodEffectTooltipsClient.init();
    }
}
