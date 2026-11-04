package net.diaithir.hostile.celestials;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Client side of the moon tint: the fog and horizon colour lean towards the event colour while it is up. */
@EventBusSubscriber(modid = DiaithirHostile.ID, value = Dist.CLIENT)
public final class ClientMoon {
    private static volatile int rgb;
    private static volatile String event = Lunar.DEFAULT_ID;
    private static float blend;

    static void set(String ev, int color) { event = ev; rgb = color; }

    public static String event() { return event; }
    public static int rgb() { return rgb; }

    @SubscribeEvent
    public static void fog(ViewportEvent.ComputeFogColor e) {
        int c = rgb;
        blend += ((c != 0 ? 1F : 0F) - blend) * 0.02F;
        if (blend < 0.01F) return;
        float k = 0.45F * blend;
        e.setRed(e.getRed() + (((c >> 16) & 255) / 255F - e.getRed()) * k);
        e.setGreen(e.getGreen() + (((c >> 8) & 255) / 255F - e.getGreen()) * k);
        e.setBlue(e.getBlue() + ((c & 255) / 255F - e.getBlue()) * k);
    }

    private ClientMoon() {}
}
