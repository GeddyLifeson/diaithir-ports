package net.diaithir.voice.client;

import net.diaithir.voice.DiaithirVoice;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.UUID;

/**
 * A small panel at the top right: a microphone while you transmit, and the names of players speaking nearby.
 * Colours are the Age I palette of the per-Age UI (docs/ui/age_palettes.json: outline, panel, highlight, hot).
 */
@EventBusSubscriber(modid = DiaithirVoice.ID, value = Dist.CLIENT)
public final class VoiceHud {
    static final int GREY = 0xFF6E5438, OUTLINE = 0xFF221810, PANEL = 0xFFB2946A, HIGHLIGHT = 0xFFD8BE90, HOT = 0xFFDEAA46, INK = 0xFF221810;

    private VoiceHud() {}

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath(DiaithirVoice.ID, "talking"), VoiceHud::render);
    }

    private static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        boolean me = VoiceClient.transmitting();
        var others = VoiceClient.speaking();
        if (!me && others.isEmpty()) return;
        int right = g.guiWidth() - 6, y = 6;
        if (me) {
            row(g, mc, right, y, Component.translatable("hud.diaithirvoice.speaking").getString(), Component.translatable("hud.diaithirvoice.native").getString(), true);
            y += 22;
        }
        for (UUID id : others) {
            PlayerInfo info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(id);
            row(g, mc, right, y, info == null ? "?" : info.getProfile().name(), null, false);
            y += 14;
        }
    }

    private static void row(GuiGraphicsExtractor g, Minecraft mc, int right, int y, String label, String nativeName, boolean self) {
        int w = Math.max(mc.font.width(label), nativeName == null ? 0 : mc.font.width(nativeName) * 3 / 4) + 24;
        int x = right - w;
        int h = nativeName == null ? 12 : 20;
        g.fill(x - 1, y - 1, right + 1, y + h + 1, OUTLINE);
        g.fill(x, y, right, y + h, self ? HIGHLIGHT : PANEL);
        mic(g, x + 3, y + 2, self ? HOT : INK);
        g.text(mc.font, label, x + 18, y + 2, INK, false);
        if (nativeName != null) { // the native word, smaller and grey, under the English label
            var pose = g.pose();
            pose.pushMatrix();
            pose.translate(x + 18, y + 12);
            pose.scale(0.75f, 0.75f);
            g.text(mc.font, nativeName, 0, 0, GREY, false);
            pose.popMatrix();
        }
    }

    /** A microphone drawn from rectangles: capsule, cradle and stand. */
    private static void mic(GuiGraphicsExtractor g, int x, int y, int c) {
        g.fill(x + 3, y, x + 6, y + 5, c);
        g.fill(x + 2, y + 1, x + 7, y + 4, c);
        g.fill(x + 1, y + 4, x + 2, y + 6, c);
        g.fill(x + 7, y + 4, x + 8, y + 6, c);
        g.fill(x + 2, y + 6, x + 7, y + 7, c);
        g.fill(x + 4, y + 7, x + 5, y + 9, c);
        g.fill(x + 2, y + 9, x + 7, y + 10, c);
    }
}
