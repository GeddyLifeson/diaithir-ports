// Ported from AutoHUD (LGPL-3.0-only) https://github.com/Crendgrim/AutoHUD
// Modifications: Replaces mixins with RenderGuiLayerEvent.Pre/Post wrapping of vanilla GUI layers; alpha fade via GuiGraphicsExtractor pose + overlay quad.
package net.diaithir.hudoverlays.autohud.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.Map;
import net.diaithir.hudoverlays.autohud.config.AutoHudConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public final class AutoHudClient {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("diaithirhudoverlays", "autohud"));
    private static final KeyMapping TOGGLE_HUD = new KeyMapping("key.autohud.toggle-hud",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
    private static final KeyMapping PEEK_HUD = new KeyMapping("key.autohud.peek-hud",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);

    private static final Map<Identifier, HudComponent> LAYERS = new HashMap<>();
    private static final Map<Identifier, Boolean> OPEN = new HashMap<>();

    static {
        LAYERS.put(VanillaGuiLayers.PLAYER_HEALTH, HudState.HEALTH);
        LAYERS.put(VanillaGuiLayers.ARMOR_LEVEL, HudState.ARMOR);
        LAYERS.put(VanillaGuiLayers.FOOD_LEVEL, HudState.HUNGER);
        LAYERS.put(VanillaGuiLayers.AIR_LEVEL, HudState.AIR);
        LAYERS.put(VanillaGuiLayers.VEHICLE_HEALTH, HudState.MOUNT_HEALTH);
        LAYERS.put(VanillaGuiLayers.CONTEXTUAL_INFO_BAR, HudState.LOCATOR_BAR);
        LAYERS.put(VanillaGuiLayers.EXPERIENCE_LEVEL, HudState.XP_LEVEL);
        LAYERS.put(VanillaGuiLayers.SCOREBOARD_SIDEBAR, HudState.SCOREBOARD);
        LAYERS.put(VanillaGuiLayers.HOTBAR, HudState.HOTBAR);
        LAYERS.put(VanillaGuiLayers.SELECTED_ITEM_NAME, HudState.TOOLTIP);
        LAYERS.put(VanillaGuiLayers.CROSSHAIR, HudState.CROSSHAIR);
        LAYERS.put(VanillaGuiLayers.CHAT, HudState.CHAT);
        LAYERS.put(VanillaGuiLayers.TITLE, HudState.ACTION_BAR);
        LAYERS.put(VanillaGuiLayers.BOSS_OVERLAY, HudState.BOSS_BAR);
        LAYERS.put(VanillaGuiLayers.EFFECTS, HudState.STATUS_EFFECTS);
    }

    private AutoHudClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, e -> {
            e.register(TOGGLE_HUD);
            e.register(PEEK_HUD);
        });
        NeoForge.EVENT_BUS.register(new Handlers());
    }

    private static boolean doRender(HudComponent c) {
        if (!c.isActive()) return true;
        return c.shouldRender();
    }

    private static float partial() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }

    public static final class Handlers {
        @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
        public void preLayer(RenderGuiLayerEvent.Pre e) {
            HudComponent c = LAYERS.get(e.getName());
            if (c == null) return;
            if (!c.isActive()) {
                OPEN.put(e.getName(), false);
                return;
            }
            if (!doRender(c)) {
                e.setCanceled(true);
                OPEN.put(e.getName(), false);
                return;
            }
            GuiGraphicsExtractor g = e.getGuiGraphics();
            float pt = partial();
            g.pose().pushMatrix();
            if (c.isHidden() && (AutoHudConfig.ANIMATION_MOVE.get() || !AutoHudConfig.ANIMATION_FADE.get())) {
                g.pose().translate((float) c.offsetX(pt), (float) c.offsetY(pt));
            }
            HudAlpha.begin(c.getAlpha(pt));
            OPEN.put(e.getName(), true);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public void cancelledLayer(RenderGuiLayerEvent.Pre e) {
            if (e.isCanceled()) {
                end(e.getName(), e.getGuiGraphics());
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public void postLayer(RenderGuiLayerEvent.Post e) {
            end(e.getName(), e.getGuiGraphics());
        }

        private void end(Identifier name, GuiGraphicsExtractor g) {
            if (Boolean.TRUE.equals(OPEN.get(name))) {
                OPEN.put(name, false);
                HudAlpha.end(g);
                g.pose().popMatrix();
            }
        }

        @SubscribeEvent
        public void tick(ClientTickEvent.Post e) {
            Minecraft mc = Minecraft.getInstance();
            while (TOGGLE_HUD.consumeClick()) HudState.toggle();
            HudState.peek(PEEK_HUD.isDown());
            if (mc.player != null && mc.level != null) {
                HudState.tick(mc, mc.player);
            }
        }

        @SubscribeEvent
        public void login(ClientPlayerNetworkEvent.LoggingIn e) {
            HudState.reset(e.getPlayer());
        }

        @SubscribeEvent
        public void respawn(ClientPlayerNetworkEvent.Clone e) {
            HudState.reset(e.getNewPlayer());
        }

        @SubscribeEvent
        public void logout(ClientPlayerNetworkEvent.LoggingOut e) {
            HudState.unregister();
        }

        @SubscribeEvent
        public void chatReceived(ClientChatReceivedEvent e) {
            HudState.onChatMessage();
        }

        @SubscribeEvent
        public void indicator(RenderGuiLayerEvent.Post e) {
            if (!VanillaGuiLayers.CHAT.equals(e.getName())) return;
            HudComponent ind = HudState.CHAT_INDICATOR;
            if (!ind.isActive() || ind.fullyHidden()) return;
            GuiGraphicsExtractor g = e.getGuiGraphics();
            Minecraft mc = Minecraft.getInstance();
            float pt = partial();
            int x = 10 + (int) ind.offsetX(pt);
            int y = mc.getWindow().getGuiScaledHeight() - 30 + (int) ind.offsetY(pt);
            int a = Math.max(0, Math.min(255, Math.round(ind.getAlpha(pt) * 255)));
            int col = (a << 24);
            g.fill(x, y, x + 12, y + 12, col | 0x303030);
            g.fill(x + 1, y + 1, x + 11, y + 11, col | 0xFFFFFF);
            g.fill(x + 3, y + 4, x + 9, y + 5, col | 0x303030);
            g.fill(x + 3, y + 6, x + 9, y + 7, col | 0x303030);
            g.fill(x + 3, y + 8, x + 7, y + 9, col | 0x303030);
        }
    }
}
