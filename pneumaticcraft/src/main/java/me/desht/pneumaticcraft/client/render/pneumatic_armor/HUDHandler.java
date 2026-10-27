/*
 * Stub: the client side of this class has not been ported to 26.1.2 yet (see PORT_NOTES.md).
 * The original source is kept under port-stash/.
 */
package me.desht.pneumaticcraft.client.render.pneumatic_armor;


public final class HUDHandler {
    private static final HUDHandler INSTANCE = new HUDHandler();
    public static HUDHandler getInstance() { return INSTANCE; }
    public void addMessage(ArmorMessage message) {}
    public void addMessage(net.minecraft.network.chat.Component title, java.util.List<net.minecraft.network.chat.Component> lines, int duration, int color) {}
}
