package net.diaithir.claims;

import net.minecraft.client.Minecraft;

final class ClientHooks {
    private ClientHooks() {}

    static void open(MapPayload p) {
        Minecraft.getInstance().setScreen(new MapScreen(p));
    }
}
