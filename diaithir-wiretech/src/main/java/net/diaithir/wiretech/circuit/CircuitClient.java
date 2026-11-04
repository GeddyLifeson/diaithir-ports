package net.diaithir.wiretech.circuit;

import net.minecraft.client.Minecraft;

/** Client only: hands a refreshed view to the open circuit screen. */
public final class CircuitClient {
    private CircuitClient() {}

    public static void onView(CircuitPayloads.View v) {
        if (Minecraft.getInstance().screen instanceof CircuitScreen s && s.getMenu().pos.equals(v.pos())) {
            s.getMenu().view = v;
            if (!v.msg().isEmpty()) s.getMenu().msg = v.msg();
            s.refreshed();
        }
    }
}
