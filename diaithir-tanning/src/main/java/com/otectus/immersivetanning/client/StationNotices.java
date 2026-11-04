package com.otectus.immersivetanning.client;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.config.ClientConfig;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Station messages that stay readable. Up to three recent notices sit above the hotbar (newest lowest), each for the
 * configured time; a message repeated while still shown refreshes instead of stacking, and a new failure never pushes
 * out the reason the player is reading. Every notice is also narrated when the narrator reads system messages. With
 * {@code stationNotices} off, messages go to the vanilla action bar as before.
 */
@EventBusSubscriber(modid = ImmersiveTanning.MOD_ID, value = Dist.CLIENT)
public final class StationNotices {
    private static final int MAX_NOTICES = 3;
    private static final int MAX_LINES = 2;
    private static final int FADE_MS = 600;

    private record Notice(Component text, boolean failure, long shownMs) {}

    private static final Deque<Notice> NOTICES = new ArrayDeque<>();

    public static void show(Component text, boolean failure) {
        Minecraft mc = Minecraft.getInstance();
        if (!ClientConfig.get(ClientConfig.STATION_NOTICES)) {
            mc.gui.setOverlayMessage(text, false);
        } else {
            NOTICES.removeIf(n -> n.text().getString().equals(text.getString()));
            NOTICES.addLast(new Notice(text, failure, Util.getMillis()));
            while (NOTICES.size() > MAX_NOTICES) NOTICES.removeFirst();
        }
        mc.getNarrator().saySystemQueued(text);
    }

    public static void clear() {
        NOTICES.clear();
    }

    /** Notices currently kept (expired ones are dropped when drawn). For the client smoke test. */
    public static int count() {
        return NOTICES.size();
    }

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.OVERLAY_MESSAGE, ImmersiveTanning.id("station_notices"), StationNotices::render);
    }

    private static void render(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        if (NOTICES.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        int width = g.guiWidth();
        int height = g.guiHeight();
        long now = Util.getMillis();
        long lifetime = ClientConfig.get(ClientConfig.NOTICE_SECONDS) * 1000L;
        NOTICES.removeIf(n -> now - n.shownMs() > lifetime);
        if (NOTICES.isEmpty() || mc.options.hideGui) return;
        Font font = mc.font;
        int wrap = Math.max(80, Math.min(width - 16, 340));
        // Above the action bar and whatever the HUD stacked above the hotbar (health, armour, air, mounts).
        int bottom = height - Math.max(84, Math.max(mc.gui.leftHeight, mc.gui.rightHeight) + 24);
        List<Notice> newestFirst = new ArrayList<>(NOTICES);
        java.util.Collections.reverse(newestFirst);
        // Older notices stop short of the crosshair; the newest is always drawn.
        int ceiling = height / 2 + 12;
        int y = bottom;
        for (Notice notice : newestFirst) {
            List<FormattedCharSequence> lines = font.split(notice.text(), wrap);
            if (lines.size() > MAX_LINES) lines = lines.subList(0, MAX_LINES);
            if (y != bottom && y - lines.size() * 10 < ceiling) break;
            int lineWidth = 0;
            for (FormattedCharSequence line : lines) lineWidth = Math.max(lineWidth, font.width(line));
            float fade = Mth.clamp((lifetime - (now - notice.shownMs())) / (float) FADE_MS, 0F, 1F);
            int alpha = Math.max(8, Math.round(255 * fade));
            int top = y - lines.size() * 10;
            int left = (width - lineWidth) / 2;
            int background = mc.options.getBackgroundColor(0.5F);
            int backgroundAlpha = Math.round(((background >>> 24) & 0xFF) * fade);
            g.fill(left - 4, top - 2, left + lineWidth + 4, y, (backgroundAlpha << 24) | (background & 0xFFFFFF));
            int color = notice.failure() ? failureColor() : 0xFFFFFF;
            for (int i = 0; i < lines.size(); i++) {
                FormattedCharSequence line = lines.get(i);
                g.text(font, line, (width - font.width(line)) / 2, top + i * 10, (alpha << 24) | color, true);
            }
            y = top - 4;
        }
    }

    private static int failureColor() {
        return ClientConfig.get(ClientConfig.COLORBLIND_SAFE_COLORS) ? 0xFFB050 : 0xFF9A80;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private StationNotices() {}
}
