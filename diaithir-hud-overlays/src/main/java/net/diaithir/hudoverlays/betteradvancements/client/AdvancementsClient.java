package net.diaithir.hudoverlays.betteradvancements.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import net.diaithir.hudoverlays.betteradvancements.config.AdvancementsConfig;
import net.diaithir.hudoverlays.betteradvancements.config.AdvancementsConfig.CompletionFilter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT)
public final class AdvancementsClient {
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(net.minecraft.resources.Identifier.fromNamespaceAndPath("diaithir_hud", "main"));
    private static final KeyMapping FOCUS_SEARCH = new KeyMapping("key.diaithir_hud.adv_search", InputConstants.UNKNOWN.getValue(), CATEGORY);
    private static final KeyMapping NEXT_TAB = new KeyMapping("key.diaithir_hud.adv_next_tab", InputConstants.UNKNOWN.getValue(), CATEGORY);
    private static final KeyMapping PREV_TAB = new KeyMapping("key.diaithir_hud.adv_prev_tab", InputConstants.UNKNOWN.getValue(), CATEGORY);
    private static final KeyMapping CYCLE_FILTER = new KeyMapping("key.diaithir_hud.adv_cycle_filter", InputConstants.UNKNOWN.getValue(), CATEGORY);

    private static final Map<String, Integer> MEMORY = new java.util.HashMap<>();
    private static String worldKey = "";

    private static EditBox searchBox;
    private static Button filterButton;
    private static String query = "";
    private static int matchIndex = -1;
    private static boolean disabled;
    private static Field fTabs, fSelected;

    private AdvancementsClient() {}

    public static void init(IEventBus modBus, ModContainer container) {
        modBus.addListener(AdvancementsClient::registerKeys);
        container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> new ConfigurationScreen(c, parent));
    }

    private static void registerKeys(RegisterKeyMappingsEvent e) {
        e.registerCategory(CATEGORY);
        e.register(FOCUS_SEARCH);
        e.register(NEXT_TAB);
        e.register(PREV_TAB);
        e.register(CYCLE_FILTER);
    }

    // ---- reflection access (fields exposed via AT; reflection fallback keeps compat safe) ----

    private static Field find(Class<?> c, Class<?> type, String... names) {
        for (String n : names) {
            try {
                Field f = c.getDeclaredField(n);
                f.setAccessible(true);
                return f;
            } catch (Throwable ignored) {}
        }
        for (Field f : c.getDeclaredFields()) {
            if (type.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }

    private static boolean resolve() {
        if (disabled) return false;
        if (fTabs != null && fSelected != null) return true;
        try {
            Class<?> c = AdvancementsScreen.class;
            fTabs = find(c, Map.class, "tabs");
            for (Field f : c.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals("AdvancementTab") || f.getName().equals("selectedTab")) {
                    f.setAccessible(true);
                    fSelected = f;
                    break;
                }
            }
        } catch (Throwable t) {
            disabled = true;
            return false;
        }
        if (fTabs == null || fSelected == null) disabled = true;
        return !disabled;
    }

    private static List<Object> tabs(AdvancementsScreen s) {
        try {
            Object v = fTabs.get(s);
            if (v instanceof Map<?, ?> m) return new ArrayList<>(m.values());
            if (v instanceof Collection<?> c) return new ArrayList<>(c);
        } catch (Throwable t) {
            disabled = true;
        }
        return List.of();
    }

    private static Object selected(AdvancementsScreen s) {
        try {
            return fSelected.get(s);
        } catch (Throwable t) {
            disabled = true;
            return null;
        }
    }

    private static void select(AdvancementsScreen s, int idx) {
        List<Object> t = tabs(s);
        if (idx < 0 || idx >= t.size()) return;
        try {
            Object tab = t.get(idx);
            for (var m : AdvancementsScreen.class.getDeclaredMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == tab.getClass()
                        && m.getReturnType() == void.class && !java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                    m.setAccessible(true);
                    m.invoke(s, tab);
                    return;
                }
            }
            fSelected.set(s, tab);
        } catch (Throwable t2) {
            disabled = true;
        }
    }

    private static int selectedIndex(AdvancementsScreen s) {
        return tabs(s).indexOf(selected(s));
    }

    private static void cycle(AdvancementsScreen s, int dir) {
        int n = tabs(s).size();
        if (n == 0) return;
        int i = selectedIndex(s);
        select(s, Math.floorMod((i < 0 ? 0 : i) + dir, n));
    }

    // ---- events ----

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCurrentServer() != null) worldKey = mc.getCurrentServer().ip;
        else if (mc.getSingleplayerServer() != null) worldKey = mc.getSingleplayerServer().getWorldData().getLevelName();
        else worldKey = "";
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        MEMORY.clear();
        worldKey = "";
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post e) {
        if (!(e.getScreen() instanceof AdvancementsScreen s) || !resolve()) return;
        Minecraft mc = Minecraft.getInstance();
        String prev = searchBox != null ? searchBox.getValue() : query;
        if (AdvancementsConfig.SEARCH_ENABLED.get()) {
            searchBox = new EditBox(mc.font, s.width / 2 - 80, 4, 160, 14, Component.translatable("diaithir_hud.adv.search"));
            searchBox.setHint(Component.translatable("diaithir_hud.adv.search"));
            searchBox.setValue(prev);
            searchBox.setResponder(v -> {
                query = v.toLowerCase(Locale.ROOT);
                matchIndex = -1;
            });
            e.addListener(searchBox);
            filterButton = Button.builder(filterLabel(), b -> {
                cycleFilter();
                b.setMessage(filterLabel());
            }).bounds(s.width / 2 + 84, 3, 60, 16).build();
            e.addListener(filterButton);
        } else {
            searchBox = null;
            filterButton = null;
        }
        if (AdvancementsConfig.REMEMBER_LAST.get()) {
            Integer idx = MEMORY.get(worldKey);
            if (idx != null) select(s, idx);
        }
    }

    private static Component filterLabel() {
        return Component.literal(switch (AdvancementsConfig.COMPLETION_FILTER.get()) {
            case ALL -> "All";
            case HIDE_COMPLETED -> "Todo";
            case ONLY_INCOMPLETE -> "Open";
        });
    }

    private static void cycleFilter() {
        CompletionFilter[] v = CompletionFilter.values();
        AdvancementsConfig.COMPLETION_FILTER.set(v[(AdvancementsConfig.COMPLETION_FILTER.get().ordinal() + 1) % v.length]);
    }

    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post e) {
        if (!(e.getScreen() instanceof AdvancementsScreen s) || !resolve()) return;
        GuiGraphicsExtractor g = e.getGuiGraphics();
        int tint = AdvancementsConfig.BACKGROUND_TINT.get();
        if ((tint >>> 24) != 0) g.fill(0, 0, s.width, s.height, tint);
        if (AdvancementsConfig.SHOW_PROGRESS.get()) {
            Minecraft mc = Minecraft.getInstance();
            var adv = mc.player != null ? mc.player.connection.getAdvancements() : null;
            if (adv != null) {
                int total = 0;
                for (var node : adv.getTree().nodes()) {
                    if (node.advancement().display().isEmpty()) continue;
                    total++;
                }
                g.centeredText(mc.font, Component.literal(total + " advancements"), s.width / 2, 20, 0xFFAAAAAA);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onKey(ScreenEvent.KeyPressed.Pre e) {
        if (!(e.getScreen() instanceof AdvancementsScreen s) || !resolve()) return;
        int key = e.getKeyCode();
        boolean ctrl = (e.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (e.getModifiers() & GLFW.GLFW_MOD_SHIFT) != 0;
        boolean typing = searchBox != null && searchBox.isFocused();

        if (ctrl && key == GLFW.GLFW_KEY_F && searchBox != null) {
            s.setFocused(searchBox);
            searchBox.setFocused(true);
            e.setCanceled(true);
            return;
        }
        if (ctrl && key == GLFW.GLFW_KEY_TAB) {
            cycle(s, shift ? -1 : 1);
            e.setCanceled(true);
            return;
        }
        if (FOCUS_SEARCH.matches(new net.minecraft.client.input.KeyEvent(key, e.getScanCode(), e.getModifiers())) && searchBox != null) {
            searchBox.setFocused(true);
            s.setFocused(searchBox);
            e.setCanceled(true);
            return;
        }
        if (CYCLE_FILTER.matches(new net.minecraft.client.input.KeyEvent(key, e.getScanCode(), e.getModifiers())) && !typing) {
            cycleFilter();
            if (filterButton != null) filterButton.setMessage(filterLabel());
            e.setCanceled(true);
            return;
        }
        if (typing) {
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                matchIndex += shift ? -1 : 1;
                jumpToMatch(s);
                e.setCanceled(true);
            }
            return;
        }
        if (AdvancementsConfig.NUMBER_KEYS.get() && key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9 && !ctrl) {
            int idx = key - GLFW.GLFW_KEY_1;
            if (idx < tabs(s).size()) {
                select(s, idx);
                e.setCanceled(true);
            }
        }
    }

    private static void jumpToMatch(AdvancementsScreen s) {
        if (query.isEmpty()) return;
        List<Object> tabs = tabs(s);
        List<Integer> hits = new ArrayList<>();
        for (int i = 0; i < tabs.size(); i++) {
            String name = tabName(tabs.get(i));
            if (name != null && name.contains(query)) hits.add(i);
        }
        if (hits.isEmpty()) return;
        matchIndex = Math.floorMod(matchIndex, hits.size());
        select(s, hits.get(matchIndex));
    }

    private static String tabName(Object tab) {
        try {
            for (var m : tab.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() == 0 && Component.class.isAssignableFrom(m.getReturnType())) {
                    m.setAccessible(true);
                    return ((Component) m.invoke(tab)).getString().toLowerCase(Locale.ROOT);
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onScroll(ScreenEvent.MouseScrolled.Pre e) {
        if (!(e.getScreen() instanceof AdvancementsScreen s) || !resolve()) return;
        if (!AdvancementsConfig.SCROLL_SWITCH.get()) return;
        double my = e.getMouseY();
        if (my > 30 || my < 0) return;
        if (e.getScrollDeltaY() == 0) return;
        cycle(s, e.getScrollDeltaY() > 0 ? -1 : 1);
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClosing(ScreenEvent.Closing e) {
        if (!(e.getScreen() instanceof AdvancementsScreen s) || !resolve()) return;
        if (!AdvancementsConfig.REMEMBER_LAST.get()) return;
        int i = selectedIndex(s);
        if (i >= 0) MEMORY.put(worldKey, i);
        searchBox = null;
        filterButton = null;
        query = "";
    }
}
