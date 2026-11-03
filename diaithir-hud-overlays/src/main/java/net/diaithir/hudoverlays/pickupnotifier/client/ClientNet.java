// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: client-side receivers for the take-item payloads.
package net.diaithir.hudoverlays.pickupnotifier.client;

import net.diaithir.hudoverlays.pickupnotifier.client.handler.AddEntriesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;

public final class ClientNet {
    private ClientNet() {}

    public static void takeItem(int itemId, int amount) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            AddEntriesHandler.addPickUpEntry(level, itemId, amount);
        }
    }

    public static void takeItemStack(ItemStack stack) {
        if (Minecraft.getInstance().level != null) {
            AddEntriesHandler.addItemEntry(stack);
        }
    }
}
