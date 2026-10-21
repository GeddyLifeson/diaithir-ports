package net.diaithir.compat;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.LoadingErrorScreen;
import net.neoforged.neoforge.common.NeoForge;

/** Puts Diaithir's own buttons where vanilla and NeoForge link to Realms, Mojang feedback and the NeoForge warnings screen. */
@Mod(value = "diaithircompat", dist = Dist.CLIENT)
public final class DiaithirClient {
    static final String JOIN_PAGE = "https://github.com/GeddyLifeson/diaithir-ports/blob/claude/project-thread-ysjkxr/README.md";
    static final String HELP_PAGE = JOIN_PAGE + "#if-something-goes-wrong";

    public DiaithirClient() {
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, DiaithirClient::onScreenInit);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Opening.class, DiaithirClient::onScreenOpening);
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            swap(event, "menu.online", "diaithir.menu.help", HELP_PAGE);
        } else if (event.getScreen() instanceof PauseScreen) {
            swap(event, "menu.sendFeedback", "diaithir.menu.help", HELP_PAGE);
            swap(event, "menu.reportBugs", "diaithir.menu.join_page", JOIN_PAGE);
        }
    }

    private static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof LoadingErrorScreen screen) {
            event.setNewScreen(new WarningsScreen(screen));
        }
    }

    private static void swap(ScreenEvent.Init.Post event, String key, String labelKey, String url) {
        for (var listener : event.getListenersList()) {
            if (listener instanceof AbstractWidget widget && widget.getMessage().getContents() instanceof TranslatableContents t && t.getKey().equals(key)) {
                event.removeListener(widget);
                event.addListener(Button.builder(Component.translatable(labelKey), b -> Util.getPlatform().openUri(url))
                        .bounds(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight())
                        .build());
                return;
            }
        }
    }
}
