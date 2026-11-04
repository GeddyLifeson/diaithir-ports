// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: Puzzles Lib client events replaced by NeoForge events and RegisterGuiLayersEvent.
package net.diaithir.hudoverlays.pickupnotifier.client;

import net.diaithir.hudoverlays.pickupnotifier.client.handler.DrawEntriesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

public final class PickupClient {
    private PickupClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAbove(
                VanillaGuiLayers.EFFECTS,
                Identifier.fromNamespaceAndPath("diaithirhudoverlays", "pickupnotifier_entries"),
                (graphics, deltaTracker) -> DrawEntriesHandler.INSTANCE.renderPickUpEntries(graphics, deltaTracker)));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class,
                e -> DrawEntriesHandler.INSTANCE.onClientTick(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class,
                e -> DrawEntriesHandler.INSTANCE.clear());
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.Clone.class,
                e -> DrawEntriesHandler.INSTANCE.clear());
    }
}
