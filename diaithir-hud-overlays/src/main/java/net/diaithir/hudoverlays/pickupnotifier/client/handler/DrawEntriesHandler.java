// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: Puzzles Lib removed, NeoForge-only, compile-fixed for 26.1.2.
package net.diaithir.hudoverlays.pickupnotifier.client.handler;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class DrawEntriesHandler {
    public static final DrawEntriesHandler INSTANCE = new DrawEntriesHandler();

    private DrawEntriesHandler() {}

    public void renderPickUpEntries(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
    }

    public void onClientTick(Minecraft minecraft) {
    }

    public void clear() {
    }
}
