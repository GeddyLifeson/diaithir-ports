package net.diaithir.units.fuel.station;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Own dashboard drawn over the screen for the vehicle you are in or beside (speed, fuel, revs, gear, brake). Immersive Vehicles' own HUD draws only its
 * frame in this build (see the gauge instruments in the vehicle packs), so this keeps the numbers readable regardless.
 */
final class VehicleHud {
    private VehicleHud() {}

    static void init(IEventBus bus) {
        bus.addListener(RegisterGuiLayersEvent.class, e -> e.registerAboveAll(Identifier.fromNamespaceAndPath(Station.ID, "dashboard"), VehicleHud::render));
    }

    private static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui || mc.screen != null) return;
        double[] d = IvControl.dash(mc.level, mc.player.position(), 6.0);
        if (d == null) return;
        int w = 150, h = 44, x = (mc.getWindow().getGuiScaledWidth() - w) / 2, y = mc.getWindow().getGuiScaledHeight() - 90;
        g.fill(x, y, x + w, y + h, 0xB0101014);
        g.text(mc.font, String.format("%.0f km/h", d[0]), x + 6, y + 5, 0xFFFFFFFF);
        g.text(mc.font, d[5] > 0 ? "ENGINE ON" : "ENGINE OFF", x + 80, y + 5, d[5] > 0 ? 0xFF66DD66 : 0xFFDD6666);
        bar(g, x + 6, y + 18, 138, 5, d[1], d[1] < 0.15 ? 0xFFDD4444 : 0xFFDDBB33);
        bar(g, x + 6, y + 26, 138, 5, Math.min(1.0, d[2] / 3000.0), 0xFF55AADD);
        g.text(mc.font, String.format("fuel %.0f%%  %.0f rpm  gear %.0f%s", d[1] * 100, d[2], d[3], d[4] > 0.5 ? "  BRAKE" : ""), x + 6, y + 34, 0xFFBBBBBB);
    }

    private static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, double frac, int color) {
        g.fill(x, y, x + w, y + h, 0xFF303036);
        g.fill(x, y, x + (int) (w * Math.max(0, Math.min(1, frac))), y + h, color);
    }
}
