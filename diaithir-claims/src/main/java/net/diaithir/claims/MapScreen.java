package net.diaithir.claims;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Client only. Draws the claim map; the yellow square is the chunk you stand in. */
public final class MapScreen extends Screen {
    private final MapPayload map;

    public MapScreen(MapPayload map) {
        super(Component.literal("Claims"));
        this.map = map;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float a) {
        super.extractRenderState(g, mx, my, a);
        int cell = Math.max(4, Math.min(width, height - 40) / MapPayload.SIZE);
        int x0 = (width - cell * MapPayload.SIZE) / 2, y0 = 24;
        g.text(font, "Claims: green yours, red others, grey free, yellow you (chunk " + map.cx() + ", " + map.cz() + ")", 8, 8, 0xFFFFFFFF);
        for (int z = 0; z < MapPayload.SIZE; z++) {
            for (int x = 0; x < MapPayload.SIZE; x++) {
                int v = map.cells()[z * MapPayload.SIZE + x];
                int col = v == 1 ? 0xFF3FA34D : v == 2 ? 0xFFB5403A : 0xFF555555;
                g.fill(x0 + x * cell, y0 + z * cell, x0 + (x + 1) * cell - 1, y0 + (z + 1) * cell - 1, col);
            }
        }
        int c = MapPayload.SIZE / 2;
        g.fill(x0 + c * cell + cell / 3, y0 + c * cell + cell / 3, x0 + (c + 1) * cell - cell / 3, y0 + (c + 1) * cell - cell / 3, 0xFFFFD84A);
    }
}
