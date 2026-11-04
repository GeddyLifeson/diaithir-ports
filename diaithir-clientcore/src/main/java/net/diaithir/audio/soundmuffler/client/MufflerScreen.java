// Ported from Extreme Sound Muffler (LGPL-3.0-only) https://github.com/LeoBeliik/ExtremeSoundMuffler
// Modifications: Rewritten GUI drawn with fills/vanilla widgets (no mod textures); anchors/blocks/mods tabs dropped; recent/all/muffled lists with volume sliders.
package net.diaithir.audio.soundmuffler.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class MufflerScreen extends Screen {
    private enum Tab { RECENT, ALL, MUFFLED }

    private static final int W = 256;
    private static final int H = 212;
    private static final int ROW_H = 13;
    private static final int VISIBLE_ROWS = 9;

    private Tab tab = Tab.RECENT;
    private int scroll;
    private EditBox search;
    private List<String> rows = new ArrayList<>();
    private int dragRow = -1;

    private int left, top;
    private int listTop, listLeft, rowW;
    private final boolean dark = MufflerConfig.USE_DARK_THEME.get();
    private final boolean leftButtons = MufflerConfig.LEFT_BUTTONS.get();

    public MufflerScreen() {
        super(Component.translatable("diaithiraudio.muffler.title"));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        listLeft = left + 8;
        listTop = top + 52;
        rowW = W - 16;

        int tx = left + 8;
        addRenderableWidget(Button.builder(Component.translatable("diaithiraudio.muffler.tab.recent"), b -> setTab(Tab.RECENT))
                .bounds(tx, top + 20, 50, 14).build());
        addRenderableWidget(Button.builder(Component.translatable("diaithiraudio.muffler.tab.all"), b -> setTab(Tab.ALL))
                .bounds(tx + 54, top + 20, 50, 14).build());
        addRenderableWidget(Button.builder(Component.translatable("diaithiraudio.muffler.tab.muffled"), b -> setTab(Tab.MUFFLED))
                .bounds(tx + 108, top + 20, 50, 14).build());

        addRenderableWidget(Button.builder(muffleToggleLabel(), b -> {
            MufflerState.setEnabled(!MufflerState.isEnabled());
            b.setMessage(muffleToggleLabel());
        }).bounds(left + W - 8 - 88, top + 20, 88, 14).build());

        search = new EditBox(font, left + 8, top + 36, W - 16, 13, Component.empty());
        search.setMaxLength(32);
        search.setHint(Component.translatable("diaithiraudio.muffler.search"));
        search.setResponder(s -> {
            scroll = 0;
            rebuild();
        });
        addRenderableWidget(search);

        addRenderableWidget(Button.builder(Component.translatable("diaithiraudio.muffler.clear"), b -> {
            if (tab == Tab.RECENT) {
                MufflerState.clearRecent();
            } else if (tab == Tab.MUFFLED) {
                MufflerState.muffled().clear();
                MufflerState.save();
            }
            rebuild();
        }).bounds(left + 8, top + H - 22, 60, 14).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(left + W - 8 - 60, top + H - 22, 60, 14).build());

        rebuild();
    }

    private Component muffleToggleLabel() {
        return Component.translatable(MufflerState.isEnabled()
                ? "diaithiraudio.muffler.muffling.on" : "diaithiraudio.muffler.muffling.off");
    }

    private void setTab(Tab t) {
        tab = t;
        scroll = 0;
        rebuild();
    }

    private void rebuild() {
        List<String> src = new ArrayList<>();
        switch (tab) {
            case RECENT -> src.addAll(MufflerState.recent());
            case ALL -> {
                boolean lawful = MufflerConfig.LAWFUL_ALL_LIST.get();
                for (String s : MufflerState.allSounds()) {
                    if (lawful || !MufflerState.isForbidden(s)) src.add(s);
                }
                Collections.sort(src);
            }
            case MUFFLED -> {
                src.addAll(MufflerState.muffled().keySet());
                Collections.sort(src);
            }
        }
        String q = search == null ? "" : search.getValue().toLowerCase(Locale.ROOT);
        rows = new ArrayList<>();
        for (String s : src) {
            if (q.isEmpty() || s.toLowerCase(Locale.ROOT).contains(q)) rows.add(s);
        }
        int max = Math.max(0, rows.size() - VISIBLE_ROWS);
        scroll = Mth.clamp(scroll, 0, max);
    }

    private int rowTop(int visibleIdx) {
        return listTop + visibleIdx * ROW_H;
    }

    private int sliderX() {
        return leftButtons ? listLeft + 26 : listLeft;
    }

    private int sliderW() {
        return rowW - 26;
    }

    private int muteX() {
        return leftButtons ? listLeft : listLeft + rowW - 24;
    }

    private int playX() {
        return muteX() + 12;
    }

    private int textColor() { return dark ? 0xFFFFFFFF : 0xFF202020; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        extractBackground(g, mouseX, mouseY, partial);
        int bg = dark ? 0xFF202020 : 0xFFC6C6C6;
        int edgeHi = dark ? 0xFF505050 : 0xFFFFFFFF;
        int edgeLo = dark ? 0xFF000000 : 0xFF555555;
        g.fill(left, top, left + W, top + H, bg);
        g.fill(left, top, left + W, top + 1, edgeHi);
        g.fill(left, top, left + 1, top + H, edgeHi);
        g.fill(left, top + H - 1, left + W, top + H, edgeLo);
        g.fill(left + W - 1, top, left + W, top + H, edgeLo);
        g.centeredText(font, title, left + W / 2, top + 7, textColor());

        g.fill(listLeft - 1, listTop - 1, listLeft + rowW + 1, listTop + VISIBLE_ROWS * ROW_H + 1, 0xFF000000);
        g.fill(listLeft, listTop, listLeft + rowW, listTop + VISIBLE_ROWS * ROW_H, 0xFF303030);

        if (rows.isEmpty()) {
            g.centeredText(font, Component.translatable("diaithiraudio.muffler.empty"),
                    left + W / 2, listTop + 40, 0xFFAAAAAA);
        }

        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int idx = scroll + i;
            if (idx >= rows.size()) break;
            String id = rows.get(idx);
            int y = rowTop(i);
            boolean muffled = MufflerState.muffled().containsKey(id);
            double vol = muffled ? MufflerState.muffled().get(id) : 1.0D;
            int sx = sliderX();
            int sw = sliderW();
            g.fill(sx, y, sx + sw, y + ROW_H - 1, (i % 2 == 0) ? 0xFF2A2A2A : 0xFF353535);
            if (muffled) {
                g.fill(sx, y, sx + (int) (vol * (sw - 4)) + 4, y + ROW_H - 1, 0xA0227722);
            }
            boolean hover = mouseX >= sx && mouseX < sx + sw && mouseY >= y && mouseY < y + ROW_H - 1;
            String label = font.plainSubstrByWidth(id, sw - 6);
            g.text(font, label, sx + 3, y + 3, hover ? 0xFF55FFFF : (muffled ? 0xFF55FF55 : 0xFFFFFFFF), true);
            if (muffled && hover) {
                int kx = sx + (int) (vol * (sw - 4));
                g.fill(kx, y + 1, kx + 4, y + ROW_H - 2, 0xFFFFFFFF);
                g.centeredText(font, Component.translatable("diaithiraudio.muffler.volume", (int) (vol * 100)),
                        sx + sw / 2, y + 3, 0xFF55FFFF);
            }

            int mx = muteX();
            boolean hm = mouseX >= mx && mouseX < mx + 11 && mouseY >= y && mouseY < y + ROW_H - 1;
            g.fill(mx, y, mx + 11, y + ROW_H - 1, hm ? 0xFF777777 : 0xFF555555);
            g.centeredText(font, Component.literal(muffled ? "M" : "-"), mx + 6, y + 3, muffled ? 0xFF55FF55 : 0xFFDDDDDD);
            int px = playX();
            boolean hp = mouseX >= px && mouseX < px + 11 && mouseY >= y && mouseY < y + ROW_H - 1;
            g.fill(px, y, px + 11, y + ROW_H - 1, hp ? 0xFF777777 : 0xFF555555);
            g.centeredText(font, Component.literal(">"), px + 6, y + 3, 0xFFDDDDDD);
            if (hm) {
                g.setComponentTooltipForNextFrame(font, List.of(Component.translatable(muffled
                        ? "diaithiraudio.muffler.unmuffle" : "diaithiraudio.muffler.muffle")), mouseX, mouseY);
            } else if (hp) {
                g.setComponentTooltipForNextFrame(font, List.of(Component.translatable("diaithiraudio.muffler.play")), mouseX, mouseY);
            }
        }

        if (rows.size() > VISIBLE_ROWS) {
            int trackH = VISIBLE_ROWS * ROW_H;
            int barH = Math.max(8, trackH * VISIBLE_ROWS / rows.size());
            int range = rows.size() - VISIBLE_ROWS;
            int by = listTop + (trackH - barH) * scroll / range;
            int bx = listLeft + rowW + 2;
            g.fill(bx, listTop, bx + 3, listTop + trackH, 0xFF111111);
            g.fill(bx, by, bx + 3, by + barH, 0xFFAAAAAA);
        }

        super.extractRenderState(g, mouseX, mouseY, partial);
    }

    private int rowAt(double mx, double my) {
        if (my < listTop || my >= listTop + VISIBLE_ROWS * ROW_H) return -1;
        int i = (int) ((my - listTop) / ROW_H);
        int idx = scroll + i;
        return idx < rows.size() ? i : -1;
    }

    private void setVolume(String id, double mx) {
        double v = (mx - (sliderX() + 2)) / (sliderW() - 4);
        v = Mth.clamp(v, 0.0D, 0.9D);
        MufflerState.muffled().put(id, v);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        double mx = event.x(), my = event.y();
        int vi = rowAt(mx, my);
        if (vi < 0 || event.button() != 0) return false;
        String id = rows.get(scroll + vi);
        int y = rowTop(vi);
        if (mx >= muteX() && mx < muteX() + 11) {
            if (MufflerState.muffled().containsKey(id)) {
                MufflerState.muffled().remove(id);
            } else {
                MufflerState.muffled().put(id, MufflerConfig.DEFAULT_MUTE_VOLUME.get());
            }
            MufflerState.save();
            if (tab == Tab.MUFFLED) rebuild();
            return true;
        }
        if (mx >= playX() && mx < playX() + 11) {
            MufflerClient.playPreview(id);
            return true;
        }
        if (mx >= sliderX() && mx < sliderX() + sliderW() && MufflerState.muffled().containsKey(id)) {
            dragRow = vi;
            setVolume(id, mx);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragRow >= 0 && scroll + dragRow < rows.size()) {
            setVolume(rows.get(scroll + dragRow), event.x());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragRow >= 0) {
            dragRow = -1;
            MufflerState.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        double d = dy == 0 ? dx : dy;
        int max = Math.max(0, rows.size() - VISIBLE_ROWS);
        scroll = Mth.clamp(scroll - (int) Math.signum(d) * 3, 0, max);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (search != null && search.isFocused()) {
            return super.keyPressed(event);
        }
        if (minecraft.options.keyInventory.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        MufflerState.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
