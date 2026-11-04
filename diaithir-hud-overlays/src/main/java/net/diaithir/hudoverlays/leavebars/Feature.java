// Ported from Leave My Bars Alone (MPL-2.0) https://github.com/Fuzss/leavemybarsalone
// Modifications: NeoForge-only entry point; Puzzles Lib removed, config via ModConfigSpec, food bar via RegisterGuiLayersEvent.
package net.diaithir.hudoverlays.leavebars;

import net.diaithir.hudoverlays.leavebars.client.LeaveBarsClient;
import net.diaithir.hudoverlays.leavebars.config.LeaveBarsConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

// NOTE: The experience bar part needs one small mixin (net.diaithir.hudoverlays.leavebars.mixin.client.GuiMixin); register leavebars.mixins.json in the module's neoforge.mods.toml via [[mixins]] config = "leavebars.mixins.json".
public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, LeaveBarsConfig.SPEC, "diaithir_hud_leavebars-client.toml");
        LeaveBarsClient.init(modBus);
    }
}
