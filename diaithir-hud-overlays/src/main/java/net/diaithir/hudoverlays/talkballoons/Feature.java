// Ported from Talk Balloons (LGPL-3.0-only) https://github.com/CERBON-MODS/Talk-Balloons
// Modifications: NeoForge-only feature package, no Modern Networking/YACL/Sunset/Stonecutter; chat bubbles drawn via NeoForge extract-render-state events, server broadcasts via vanilla chat events
package net.diaithir.hudoverlays.talkballoons;

import net.diaithir.hudoverlays.talkballoons.config.TalkBalloonsConfig;
import net.diaithir.hudoverlays.talkballoons.network.TalkBalloonsNetwork;
import net.diaithir.hudoverlays.talkballoons.server.TalkBalloonsServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, TalkBalloonsConfig.SPEC, "diaithir_hud_talkballoons-common.toml");
        modBus.addListener(TalkBalloonsNetwork::register);
        NeoForge.EVENT_BUS.register(new TalkBalloonsServer());
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            net.diaithir.hudoverlays.talkballoons.client.TalkBalloonsClient.init(modBus);
        }
    }
}
