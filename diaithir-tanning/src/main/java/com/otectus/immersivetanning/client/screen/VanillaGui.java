package com.otectus.immersivetanning.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * The one look every Immersive Tanning screen and the JEI categories share: Minecraft's own container panel, slots,
 * buttons and scroller. Every surface is drawn from Minecraft's textures through the resource manager, so a resource
 * pack that restyles vanilla containers restyles these screens too, and nothing here is a texture of this mod. Text
 * follows vanilla containers: dark grey labels on the light panel, light text inside the dark gameplay displays.
 */
public final class VanillaGui {
    public static final Identifier PANEL = Identifier.parse("textures/gui/container/generic_54.png");
    public static final Identifier SLOTS = Identifier.parse("textures/gui/container/generic_54.png");
    public static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    public static final Identifier BUTTON_DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");
    public static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");
    public static final Identifier SCROLLER = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    public static final Identifier SCROLLER_DISABLED = Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    /** Container titles and labels (vanilla {@code 0x404040}). */
    public static final int LABEL = 0xFF404040;
    /** Secondary text on the light panel. */
    public static final int LABEL_DIM = 0xFF6B6B6B;
    /** Text on button rows, as on vanilla buttons. */
    public static final int ROW_TEXT = 0xFFFFFFFF;
    public static final int ROW_TEXT_DIM = 0xFFD0D0D0;
    public static final int ROW_TEXT_DISABLED = 0xFFA0A0A0;
    /** Light text inside dark gameplay displays. */
    public static final int DISPLAY_TEXT = 0xFFE0E0E0;
    public static final int DISPLAY_TEXT_DIM = 0xFFA8A8A8;
    /** Headings and notices inside dark displays (the countdown, a finished piece, a press too early). */
    public static final int DISPLAY_HEADING = 0xFFFFD66B;

    /** A vanilla container panel of any size: the standard bevelled light grey frame. */
    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        final int b = 4, tw = 176, th = 222;
        int mw = w - 2 * b, mh = h - 2 * b;
        g.blit(RenderPipelines.GUI_TEXTURED, PANEL, x, y, 0, 0, b, b, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, PANEL, x + w - b, y, tw - b, 0, b, b, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, PANEL, x, y + h - b, 0, th - b, b, b, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, PANEL, x + w - b, y + h - b, tw - b, th - b, b, b, 256, 256);
        if (mw > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, PANEL, x + b, y, b, 0, mw, b, tw - 2 * b, b, 256, 256);
            g.blit(RenderPipelines.GUI_TEXTURED, PANEL, x + b, y + h - b, b, th - b, mw, b, tw - 2 * b, b, 256, 256);
        }
        if (mh > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, PANEL, x, y + b, 0, b, b, mh, b, th - 2 * b, 256, 256);
            g.blit(RenderPipelines.GUI_TEXTURED, PANEL, x + w - b, y + b, tw - b, b, b, mh, b, th - 2 * b, 256, 256);
        }
        if (mw > 0 && mh > 0) g.fill(x + b, y + b, x + b + mw, y + b + mh, 0xFFC6C6C6);
    }

    /** A recessed field in the colours of a vanilla slot (dark top-left edge, white bottom-right edge). */
    public static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF8B8B8B);
        g.fill(x, y, x + w - 1, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + h - 1, 0xFF373737);
        g.fill(x + 1, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        g.fill(x + w - 1, y + 1, x + w, y + h, 0xFFFFFFFF);
    }

    /** A recessed dark display for gameplay surfaces that need contrast (the fleshing beam, the stitching board). */
    public static void display(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        inset(g, x, y, w, h);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF262626);
    }

    /**
     * Text in a dark display sized to it: for readouts whose colours (quality grades, datapack trait colours) are
     * chosen for dark backgrounds and would lose contrast on the light panel. Returns the height used.
     */
    public static int readout(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int w, int maxLines) {
        int lines = Math.max(1, Math.min(maxLines, font.split(text, w - 8).size()));
        display(g, x, y, w, 8 + lines * 10);
        wrapped(g, font, text, x + 4, y + 5, w - 8, DISPLAY_TEXT, lines);
        return 8 + lines * 10;
    }

    /** The standard 18 px item slot. */
    public static void slot(GuiGraphicsExtractor g, int x, int y) {
        g.blit(RenderPipelines.GUI_TEXTURED, SLOTS, x, y, 7, 17, 18, 18, 256, 256);
    }

    /** A list row or card drawn as a vanilla button: disabled, normal, or highlighted (hovered). */
    public static void row(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active, boolean highlighted) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, !active ? BUTTON_DISABLED : highlighted ? BUTTON_HIGHLIGHTED : BUTTON, x, y, w, h);
    }

    /** A vanilla scroll bar: a recessed track and the creative inventory's scroller handle. */
    public static void scrollbar(GuiGraphicsExtractor g, int x, int y, int h, float position, boolean enabled) {
        inset(g, x, y, 14, h);
        int travel = Math.max(0, h - 17);
        int thumb = y + 1 + Math.round(travel * Math.max(0F, Math.min(1F, position)));
        g.blitSprite(RenderPipelines.GUI_TEXTURED, enabled ? SCROLLER : SCROLLER_DISABLED, x + 1, thumb, 12, 15);
    }

    /** True when (mx, my) is on a scroll bar drawn with {@link #scrollbar}. */
    public static boolean onScrollbar(double mx, double my, int x, int y, int h) {
        return mx >= x && mx < x + 14 && my >= y && my < y + h;
    }

    /** A vanilla-style bar: recessed track, flat fill and a one-pixel highlight on top. */
    public static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, float fraction, int color) {
        inset(g, x - 1, y - 1, w + 2, h + 2);
        g.fill(x, y, x + w, y + h, 0xFF373737);
        int filled = Math.round(w * Math.max(0F, Math.min(1F, fraction)));
        if (filled > 0) {
            g.fill(x, y, x + filled, y + h, color);
            if (h > 2) g.fill(x, y, x + filled, y + 1, 0x60FFFFFF);
        }
    }

    public static Button button(int x, int y, int width, int height, Component label, Button.OnPress onPress) {
        return Button.builder(label, onPress).bounds(x, y, width, height).build();
    }

    /** The text cut to {@code width} with an ellipsis; the full text belongs in a tooltip. */
    public static String clipped(Font font, Component text, int width) {
        if (font.width(text) <= width) return text.getString();
        return font.plainSubstrByWidth(text.getString(), Math.max(0, width - font.width("..."))) + "...";
    }

    public static boolean isClipped(Font font, Component text, int width) {
        return font.width(text) > width;
    }

    /** Wrapped text, at most {@code maxLines} lines; a clipped last line ends in "...". Returns the lines drawn. */
    public static int wrapped(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int width, int color, int maxLines, boolean shadow) {
        List<FormattedCharSequence> lines = font.split(text, width);
        int n = Math.min(lines.size(), Math.max(0, maxLines));
        for (int i = 0; i < n; i++) {
            if (i == n - 1 && n < lines.size()) {
                StringBuilder sb = new StringBuilder();
                lines.get(i).accept((index, style, cp) -> {
                    sb.appendCodePoint(cp);
                    return true;
                });
                String clipped = font.plainSubstrByWidth(sb.toString(), Math.max(0, width - font.width("..."))) + "...";
                g.text(font, clipped, x, y + i * 10, color, shadow);
            } else {
                g.text(font, lines.get(i), x, y + i * 10, color, shadow);
            }
        }
        return n;
    }

    public static int wrapped(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int width, int color, int maxLines) {
        return wrapped(g, font, text, x, y, width, color, maxLines, false);
    }

    /** True when {@code text} needs more than {@code maxLines} lines at {@code width}. */
    public static boolean isWrapClipped(Font font, Component text, int width, int maxLines) {
        return font.split(text, width).size() > Math.max(0, maxLines);
    }

    /** Centred wrapped text; returns the number of lines drawn. */
    public static int wrappedCentered(GuiGraphicsExtractor g, Font font, Component text, int centerX, int y, int width, int color, boolean shadow) {
        List<FormattedCharSequence> lines = font.split(text, width);
        for (int i = 0; i < lines.size(); i++) {
            g.text(font, lines.get(i), centerX - font.width(lines.get(i)) / 2, y + i * 10, color, shadow);
        }
        return lines.size();
    }

    public static void centered(GuiGraphicsExtractor g, Font font, Component text, int centerX, int y, int color) {
        g.text(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    private VanillaGui() {}
}
