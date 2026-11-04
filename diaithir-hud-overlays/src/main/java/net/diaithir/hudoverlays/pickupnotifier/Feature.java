// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: Puzzles Lib removed; common server/network part plus dist-guarded client part, direct NeoForge APIs.
package net.diaithir.hudoverlays.pickupnotifier;

import net.diaithir.hudoverlays.pickupnotifier.client.PickupClient;
import net.diaithir.hudoverlays.pickupnotifier.client.config.ClientConfig;
import net.diaithir.hudoverlays.pickupnotifier.config.ServerConfig;
import net.diaithir.hudoverlays.pickupnotifier.network.PickupNetwork;
import net.diaithir.hudoverlays.pickupnotifier.server.PickupServerHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        // common / server side
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, "diaithir_hud_pickupnotifier-server.toml");
        modBus.addListener(RegisterPayloadHandlersEvent.class, PickupNetwork::register);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, PickupServerHandler::onEntityItemPickupPre1);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, true, PickupServerHandler::onEntityItemPickupPre2);
        NeoForge.EVENT_BUS.addListener(PickupServerHandler::onItemPickupPost);

        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "diaithir_hud_pickupnotifier-client.toml");
        PickupClient.init(modBus);
    }
}
