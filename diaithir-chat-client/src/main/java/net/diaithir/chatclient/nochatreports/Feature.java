// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: NeoForge feature entrypoint; config registration and client/common mixin wiring (config screens and server-list UI dropped).
package net.diaithir.chatclient.nochatreports;

import net.diaithir.chatclient.nochatreports.config.NCRClientConfig;
import net.diaithir.chatclient.nochatreports.config.NCRCommonConfig;
import net.diaithir.chatclient.nochatreports.client.NCRClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

// NOTE: Mixins are required for this feature (no NeoForge event can strip chat signatures or alter
// server status encoding). The integrator must register three mixin configs in neoforge.mods.toml:
// [[mixins]] config="nochatreports-common.mixins.json" (server+client, loaded on both dists),
// and the client/server sections are declared inside the same json (client list only loads on CLIENT).
public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, NCRCommonConfig.SPEC, "diaithir_chat_nochatreports-common.toml");
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, NCRClientConfig.SPEC, "diaithir_chat_nochatreports-client.toml");
        NCRClient.init(modBus);
    }
}
