package com.diaithir.overworld_biomes.naturescompass;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BiomeSelectScreen extends Screen {
    private static final int ROW_H = 14;
    private static String rememberedFilter = "";
    private final List<String> all;
    private final boolean mainHand;
    private List<String> shown = new ArrayList<>();
    private EditBox filter;
    private CompassConfig.SortMode sort = CompassConfig.GUI_SORT.get();
    private int scroll;
    private int left, top, width, height;

    public BiomeSelectScreen(List<String> ids, boolean mainHand) {
        super(Component.translatable("gui.diaithir_overworld_biomes.title"));
        this.all = ids;
        this.mainHand = mainHand;
    }

    private static Component name(String id) {
        Identifier rl = Identifier.parse(id);
        return Component.translatable("biome." + rl.getNamespace() + "." + rl.getPath());
    }

    @Override
    protected void init() {
        width = Math.min(256, this.width - 20);
        height = this.height - 40;
        left = (this.width - width) / 2;
        top = 20;
        filter = new EditBox(font, left + 4, top + 4, width - 90, 16, Component.empty());
        if (CompassConfig.REMEMBER_FILTER.get()) filter.setValue(rememberedFilter);
        filter.setResponder(s -> { rememberedFilter = s; refilter(); });
        addRenderableWidget(filter);
        addRenderableWidget(Button.builder(Component.translatable("gui.diaithir_overworld_biomes.sort." + sort.name().toLowerCase()), b -> {
            sort = sort == CompassConfig.SortMode.ALPHABETICAL ? CompassConfig.SortMode.SOURCE_MOD : CompassConfig.SortMode.ALPHABETICAL;
            b.setMessage(Component.translatable("gui.diaithir_overworld_biomes.sort." + sort.name().toLowerCase()));
            refilter();
        }).bounds(left + width - 82, top + 4, 78, 16).build());
        refilter();
    }

    private void refilter() {
        String f = filter == null ? "" : filter.getValue().toLowerCase();
        shown = new ArrayList<>(all.stream().filter(id -> f.isEmpty()
                || id.toLowerCase().contains(f) || name(id).getString().toLowerCase().contains(f)).toList());
        Comparator<String> byName = Comparator.comparing(id -> name(id).getString());
        shown.sort(sort == CompassConfig.SortMode.SOURCE_MOD
                ? Comparator.<String, String>comparing(id -> Identifier.parse(id).getNamespace()).thenComparing(byName) : byName);
        scroll = 0;
    }

    private int visibleRows() { return (height - 28) / ROW_H; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        g.fill(left, top, left + width, top + height, 0xE0262A2E);
        g.fill(left + 1, top + 1, left + width - 1, top + height - 1, 0xE0383E44);
        int y = top + 26;
        for (int i = 0; i < visibleRows() && i + scroll < shown.size(); i++) {
            String id = shown.get(i + scroll);
            boolean hover = mx >= left + 4 && mx < left + width - 10 && my >= y && my < y + ROW_H;
            if (hover) g.fill(left + 4, y, left + width - 10, y + ROW_H, 0x80B87333);
            g.fill(left + 6, y + 3, left + 10, y + 11, 0xFF000000 | (id.hashCode() & 0xFFFFFF));
            g.text(font, name(id), left + 14, y + 3, 0xFFE0E0E0, false);
            y += ROW_H;
        }
        int maxScroll = Math.max(1, shown.size() - visibleRows());
        int railH = height - 30;
        int knob = Math.min(railH - 10, railH * visibleRows() / Math.max(1, shown.size()));
        int ky = top + 26 + (railH - knob) * scroll / maxScroll;
        g.fill(left + width - 8, top + 26, left + width - 4, top + 26 + railH, 0xFF15181B);
        g.fill(left + width - 8, ky, left + width - 4, ky + Math.max(8, knob), 0xFFB87333);
        super.extractRenderState(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent ev, boolean dbl) {
        if (super.mouseClicked(ev, dbl)) return true;
        double mx = ev.x(), my = ev.y();
        if (mx >= left + 4 && mx < left + width - 10 && my >= top + 26) {
            int idx = (int) ((my - top - 26) / ROW_H) + scroll;
            if (idx >= 0 && idx < shown.size() && idx - scroll < visibleRows()) {
                ClientPacketDistributor.sendToServer(new CompassNetwork.SelectBiome(Identifier.parse(shown.get(idx)), mainHand));
                onClose();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        scroll = Math.max(0, Math.min(Math.max(0, shown.size() - visibleRows()), scroll - (int) Math.signum(dy)));
        return true;
    }
}
