// Ported from Mouse Tweaks (BSD-3-Clause) https://github.com/YaLTeR/MouseTweaks
// Modifications: replaces upstream mixins/Neo entrypoint with NeoForge ScreenEvent hooks (pressed/released/dragged/scrolled).
package net.diaithir.inventoryui.mousetweaks.client;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class MouseTweaksClient {
    private MouseTweaksClient() {}

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(new Handlers());
    }

    public static final class Handlers {
        @SubscribeEvent
        public void onPressed(ScreenEvent.MouseButtonPressed.Pre e) {
            Screen s = e.getScreen();
            if (MouseTweaksLogic.onMouseClicked(s, e.getMouseX(), e.getMouseY(), e.getButton())) {
                e.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onReleased(ScreenEvent.MouseButtonReleased.Pre e) {
            Screen s = e.getScreen();
            if (MouseTweaksLogic.onMouseReleased(s, e.getMouseX(), e.getMouseY(), e.getButton())) {
                e.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onDragged(ScreenEvent.MouseDragged.Pre e) {
            Screen s = e.getScreen();
            MouseTweaksLogic.onMouseDrag(s, e.getMouseX(), e.getMouseY(), e.getMouseButton());
        }

        @SubscribeEvent
        public void onScrolled(ScreenEvent.MouseScrolled.Post e) {
            // Sent only when the screen itself did not consume the scroll.
            Screen s = e.getScreen();
            MouseTweaksLogic.onMouseScrolled(s, e.getMouseX(), e.getMouseY(), e.getScrollDeltaY());
        }

        @SubscribeEvent
        public void onClosing(ScreenEvent.Closing e) {
            MouseTweaksLogic.reset();
        }

        @SubscribeEvent
        public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
            MouseTweaksLogic.reset();
        }
    }
}
