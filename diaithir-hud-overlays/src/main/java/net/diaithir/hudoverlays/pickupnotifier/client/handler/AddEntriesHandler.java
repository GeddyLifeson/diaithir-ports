// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: Puzzles Lib removed, NeoForge-only, compile-fixed for 26.1.2.
package net.diaithir.hudoverlays.pickupnotifier.client.handler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;

public final class AddEntriesHandler {
    private AddEntriesHandler() {}

    public static void onEntityPickup(ClientLevel level, int itemEntityId, int playerId, int amount) {
    }

    public static void addPickUpEntry(ClientLevel level, int itemId, int amount) {
    }

    public static void addItemEntry(ItemStack stack) {
    }
}
