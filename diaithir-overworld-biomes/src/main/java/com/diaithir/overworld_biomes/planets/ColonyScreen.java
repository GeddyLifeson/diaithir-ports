package com.diaithir.overworld_biomes.planets;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Colony picker (client). A scrollable grid of the planets' 64 px icons
 * (assets/diaithir_planets/textures/gui/colony/&lt;planet_id&gt;.png); a click opens the planet's profile with a Found button that
 * runs /colony found &lt;id&gt; (the server checks the Age). Open it with the client command /colonies.
 */
public final class ColonyScreen extends Screen {
    private static final int CELL = 76, ICON = 64;
    private final List<String> all = PlanetColony.planetIds();
    private List<String> shown = new ArrayList<>();
    private EditBox filter;
    private int scroll, left, top, cols, rows;
    private String detail;

    public ColonyScreen() { super(Component.literal("Colonies")); }

    private static Identifier icon(String id) { return Identifier.fromNamespaceAndPath("diaithir_planets", "textures/gui/colony/" + id + ".png"); }

    private static String name(String id) {
        JsonObject p = PlanetColony.profile(id);
        return p != null && p.has("name") ? p.get("name").getAsString() : id;
    }

    @Override
    protected void init() {
        clearWidgets();
        if (detail == null) {
            filter = new EditBox(font, width / 2 - 100, 12, 200, 16, Component.empty());
            filter.setResponder(s -> refilter());
            addRenderableWidget(filter);
            cols = Math.max(1, (width - 20) / CELL);
            left = (width - cols * CELL) / 2;
            top = 36;
            rows = Math.max(1, (height - top - 8) / CELL);
            refilter();
        } else {
            addRenderableWidget(Button.builder(Component.literal("Found colony"), b -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) mc.player.connection.sendCommand("colony found " + detail);
                onClose();
            }).bounds(width / 2 - 105, height - 28, 100, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Back"), b -> { detail = null; init(); })
                .bounds(width / 2 + 5, height - 28, 100, 20).build());
        }
    }

    private void refilter() {
        String f = filter == null ? "" : filter.getValue().toLowerCase();
        shown = new ArrayList<>(all.stream().filter(id -> f.isEmpty() || id.contains(f) || name(id).toLowerCase().contains(f)).toList());
        scroll = 0;
    }

    private static String line(JsonObject p, String k) {
        if (p == null || !p.has(k) || !p.get(k).isJsonArray()) return "none";
        List<String> l = new ArrayList<>();
        p.getAsJsonArray(k).forEach(e -> l.add(e.getAsString().replace('_', ' ')));
        return l.isEmpty() ? "none" : String.join(", ", l);
    }

    private static String s(JsonObject p, String k) { return p != null && p.has(k) && !p.get(k).isJsonNull() && p.get(k).isJsonPrimitive() ? p.get(k).getAsString() : ""; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0xE0101418);
        if (detail != null) {
            JsonObject p = PlanetColony.profile(detail);
            int x = width / 2 - 150, y = 16;
            g.blit(RenderPipelines.GUI_TEXTURED, icon(detail), x, y, 0f, 0f, ICON, ICON, ICON, ICON);
            g.text(font, Component.literal(name(detail)), x + ICON + 10, y + 4, 0xFFFFFFFF, true);
            g.text(font, Component.literal(s(p, "soil") + " soil, " + s(p, "realm") + " realm"), x + ICON + 10, y + 18, 0xFFB0B8C0, false);
            g.text(font, Component.literal(s(p, "terrain") + " terrain, " + s(p, "sea") + " seas, " + s(p, "weather") + " weather"), x + ICON + 10, y + 30, 0xFFB0B8C0, false);
            g.text(font, Component.literal("Opens in Age " + Math.max(PlanetColony.MIN_SPACE_AGE, p == null || !p.has("opens_age") ? 1 : p.get("opens_age").getAsInt())), x + ICON + 10, y + 42, 0xFFB0B8C0, false);
            int ty = y + ICON + 12;
            List<String> lines = new ArrayList<>(List.of(
                "Rich in: " + line(p, "rich_in") + ";  poor in: " + line(p, "poor_in"),
                "Hazards: " + line(p, "hazards"),
                "Settlements: " + s(p, "settlements")));
            JsonObject n = p != null && p.has("named") ? p.getAsJsonObject("named") : null;
            if (n != null) lines.add("Own flora and ore: " + s(n, "tree") + " trees, " + s(n, "plant") + ", " + s(n, "ore") + " ore");
            String sum = s(p, "summary");
            if (sum.isEmpty()) sum = s(p, "notes");
            for (String l : lines) { g.text(font, Component.literal(l), x, ty, 0xFFE0E0E0, false); ty += 12; }
            ty += 6;
            for (net.minecraft.util.FormattedCharSequence seq : font.split(Component.literal(sum), 300)) { g.text(font, seq, x, ty, 0xFFC8D0D8, false); ty += 10; }
        } else {
            g.centeredText(font, Component.literal("Choose a planet for your colony (" + shown.size() + ")"), width / 2, 2, 0xFFFFFFFF);
            String hover = null;
            for (int i = 0; i < rows * cols; i++) {
                int idx = scroll * cols + i;
                if (idx >= shown.size()) break;
                String id = shown.get(idx);
                int x = left + (i % cols) * CELL, y = top + (i / cols) * CELL;
                boolean over = mx >= x && mx < x + CELL && my >= y && my < y + CELL;
                if (over) { g.fill(x, y, x + CELL - 2, y + CELL - 2, 0x80B87333); hover = id; }
                g.blit(RenderPipelines.GUI_TEXTURED, icon(id), x + 6, y + 2, 0f, 0f, ICON, ICON, ICON, ICON);
                g.centeredText(font, Component.literal(font.plainSubstrByWidth(name(id), CELL - 4)), x + CELL / 2 - 1, y + ICON + 4, 0xFFE0E0E0);
            }
            if (hover != null) {
                JsonObject p = PlanetColony.profile(hover);
                g.setTooltipForNextFrame(font, List.of(Component.literal(name(hover)), Component.literal("Hazards: " + line(p, "hazards")), Component.literal("Rich in: " + line(p, "rich_in"))).stream().map(c -> c.getVisualOrderText()).toList(), mx, my);
            }
        }
        super.extractRenderState(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent ev, boolean dbl) {
        if (super.mouseClicked(ev, dbl)) return true;
        if (detail != null) return false;
        int cx = (int) ((ev.x() - left) / CELL), cy = (int) ((ev.y() - top) / CELL);
        if (ev.x() < left || ev.y() < top || cx >= cols || cy >= rows) return false;
        int idx = (scroll + cy) * cols + cx;
        if (idx >= 0 && idx < shown.size()) { detail = shown.get(idx); init(); return true; }
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (detail != null) return false;
        int maxScroll = Math.max(0, (shown.size() + cols - 1) / cols - rows);
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(dy)));
        return true;
    }
}
