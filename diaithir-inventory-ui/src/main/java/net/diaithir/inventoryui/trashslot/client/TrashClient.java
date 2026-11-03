package net.diaithir.inventoryui.trashslot.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import net.diaithir.inventoryui.trashslot.common.TrashPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public final class TrashClient {
    private static final int SIZE = 18;
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("diaithir_inventoryui", "main"));
    private static final KeyMapping TOGGLE = new KeyMapping("key.diaithir_inventoryui.toggle_trash",
            KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, CATEGORY);

    private static final WeakHashMap<Screen, State> STATES = new WeakHashMap<>();
    private static ItemStack lastTrashed = ItemStack.EMPTY;

    private static final class State {
        int relX, relY;
        boolean dragging;
        int grabX, grabY;
    }

    private TrashClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, e -> e.register(TOGGLE));
        NeoForge.EVENT_BUS.register(new TrashClient.Handlers());
    }

    private static boolean shiftDown() {
        Minecraft mc = Minecraft.getInstance();
        return InputConstants.isKeyDown(mc.getWindow(), mc.options.keyShift.getKey().getValue());
    }

    private static boolean eligible(Screen s) {
        if (!(s instanceof AbstractContainerScreen<?>)) {
            return false;
        }
        if (s instanceof CreativeModeInventoryScreen && !TrashClientConfig.SHOW_IN_CREATIVE.get()) {
            return false;
        }
        String simple = s.getClass().getSimpleName();
        String full = s.getClass().getName();
        for (String b : TrashClientConfig.BLOCKED_SCREENS.get()) {
            if (b.equals(simple) || b.equals(full)) {
                return false;
            }
        }
        List<? extends String> allowed = TrashClientConfig.ALLOWED_SCREENS.get();
        if (allowed.isEmpty()) {
            return true;
        }
        for (String a : allowed) {
            if (a.equals(simple) || a.equals(full)) {
                return true;
            }
        }
        return false;
    }

    private static int[] defaultRel(AbstractContainerScreen<?> s) {
        int w = s.getXSize(), h = s.getYSize();
        int ox = TrashClientConfig.OFFSET_X.get(), oy = TrashClientConfig.OFFSET_Y.get();
        return switch (TrashClientConfig.ANCHOR.get()) {
            case BOTTOM_RIGHT -> new int[] {w + ox, h - SIZE - oy};
            case BOTTOM_LEFT -> new int[] {-SIZE - ox, h - SIZE - oy};
            case TOP_RIGHT -> new int[] {w + ox, oy};
            case TOP_LEFT -> new int[] {-SIZE - ox, oy};
        };
    }

    private static void resetPos(AbstractContainerScreen<?> s, State st) {
        int[] d = defaultRel(s);
        st.relX = d[0];
        st.relY = d[1];
    }

    private static State state(Screen s) {
        return STATES.get(s);
    }

    private static int absX(AbstractContainerScreen<?> s, State st) {
        return s.getGuiLeft() + st.relX;
    }

    private static int absY(AbstractContainerScreen<?> s, State st) {
        return s.getGuiTop() + st.relY;
    }

    private static boolean inSlot(AbstractContainerScreen<?> s, State st, double mx, double my) {
        int x = absX(s, st), y = absY(s, st);
        return mx >= x + 1 && mx < x + SIZE - 1 && my >= y + 1 && my < y + SIZE - 1;
    }

    private static boolean inRect(AbstractContainerScreen<?> s, State st, double mx, double my) {
        int x = absX(s, st), y = absY(s, st);
        return mx >= x && mx < x + SIZE && my >= y && my < y + SIZE;
    }

    private static boolean visible() {
        return TrashClientConfig.ENABLED.get();
    }

    private static boolean needsConfirm(ItemStack stack, int count) {
        int th = TrashClientConfig.CONFIRM_THRESHOLD.get();
        if (th > 0 && count > th) {
            return true;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (TrashClientConfig.PROTECTED_ITEMS.get().contains(id)) {
            return true;
        }
        for (String t : TrashClientConfig.CONFIRM_FOR_TAGS.get()) {
            Identifier tid = Identifier.tryParse(t);
            if (tid != null && stack.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, tid))) {
                return true;
            }
        }
        return false;
    }

    private static void send(AbstractContainerScreen<?> s, int slot, int count, ItemStack stack, int amount) {
        if (!lastTrashed.isEmpty() || TrashClientConfig.UNDO_ENABLED.get()) {
            lastTrashed = TrashClientConfig.UNDO_ENABLED.get() ? stack.copyWithCount(amount) : ItemStack.EMPTY;
        }
        var menu = s.getMenu();
        ClientPacketDistributor.sendToServer(TrashPayload.trash(menu.containerId, menu.getStateId(), slot, count));
        if (slot < 0) {
            menu.setCarried(ItemStack.EMPTY);
        }
    }

    private static void trash(AbstractContainerScreen<?> s, int slot, int count) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack stack = slot < 0 ? s.getMenu().getCarried() : s.getMenu().getSlot(slot).getItem();
        if (stack.isEmpty()) {
            return;
        }
        int amount = count <= 0 ? stack.getCount() : Math.min(count, stack.getCount());
        if (s instanceof CreativeModeInventoryScreen) {
            if (slot < 0) {
                lastTrashed = TrashClientConfig.UNDO_ENABLED.get() ? stack.copy() : ItemStack.EMPTY;
                s.getMenu().setCarried(ItemStack.EMPTY);
            }
            return;
        }
        if (needsConfirm(stack, amount)) {
            ItemStack snap = stack.copy();
            mc.setScreen(new ConfirmScreen(yes -> {
                mc.setScreen(s);
                if (yes && mc.player != null && mc.player.containerMenu == s.getMenu()) {
                    send(s, slot, count, snap, amount);
                }
            }, Component.translatable("trashslot.confirm.title"), Component.translatable("trashslot.confirm.message", snap.getHoverName())));
            return;
        }
        send(s, slot, count, stack, amount);
    }

    private static Slot hoveredSlot(AbstractContainerScreen<?> s) {
        return s.getSlotUnderMouse();
    }

    public static List<int[]> hitRects(Screen s) {
        List<int[]> out = new ArrayList<>();
        State st = state(s);
        if (st != null && s instanceof AbstractContainerScreen<?> acs && visible()) {
            out.add(new int[] {absX(acs, st), absY(acs, st), SIZE, SIZE});
        }
        return out;
    }

    public static final class Handlers {
        @SubscribeEvent
        public void onInit(ScreenEvent.Init.Post e) {
            Screen s = e.getScreen();
            if (!eligible(s)) {
                STATES.remove(s);
                return;
            }
            AbstractContainerScreen<?> acs = (AbstractContainerScreen<?>) s;
            State st = STATES.computeIfAbsent(s, k -> new State());
            if (TrashClientConfig.REMEMBER_POSITION.get() && TrashClientConfig.HAS_POS.get()) {
                st.relX = TrashClientConfig.POS_X.get();
                st.relY = TrashClientConfig.POS_Y.get();
            } else {
                resetPos(acs, st);
            }
        }

        @SubscribeEvent
        public void onRender(ScreenEvent.Render.Post e) {
            Screen s = e.getScreen();
            State st = state(s);
            if (st == null || !visible() || !(s instanceof AbstractContainerScreen<?> acs)) {
                return;
            }
            GuiGraphicsExtractor g = e.getGuiGraphics();
            int x = absX(acs, st), y = absY(acs, st);
            g.fill(x, y, x + SIZE, y + SIZE, 0xFF8B8B8B);
            g.fill(x, y, x + SIZE - 1, y + 1, 0xFF373737);
            g.fill(x, y, x + 1, y + SIZE - 1, 0xFF373737);
            g.fill(x + SIZE - 1, y + 1, x + SIZE, y + SIZE, 0xFFFFFFFF);
            g.fill(x + 1, y + SIZE - 1, x + SIZE, y + SIZE, 0xFFFFFFFF);
            g.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, 0xFF8B8B8B);
            g.fill(x + 5, y + 4, x + 13, y + 5, 0xFF373737);
            g.fill(x + 7, y + 3, x + 11, y + 4, 0xFF373737);
            g.fill(x + 6, y + 6, x + 12, y + 14, 0xFF555555);
            g.fill(x + 8, y + 7, x + 9, y + 13, 0xFF8B8B8B);
            g.fill(x + 10, y + 7, x + 11, y + 13, 0xFF8B8B8B);
            boolean hover = inSlot(acs, st, e.getMouseX(), e.getMouseY());
            if (hover && !st.dragging) {
                g.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, 0x80FFFFFF);
                List<Component> tip = new ArrayList<>();
                tip.add(Component.translatable("trashslot.tooltip"));
                if (TrashClientConfig.UNDO_ENABLED.get() && !lastTrashed.isEmpty()) {
                    tip.add(Component.translatable("trashslot.tooltip.last", lastTrashed.getCount(), lastTrashed.getHoverName()));
                }
                g.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tip, e.getMouseX(), e.getMouseY());
            }
        }

        @SubscribeEvent
        public void onClick(ScreenEvent.MouseButtonPressed.Pre e) {
            Screen s = e.getScreen();
            State st = state(s);
            if (st == null || !visible() || !(s instanceof AbstractContainerScreen<?> acs)) {
                return;
            }
            double mx = e.getMouseX(), my = e.getMouseY();
            if (!inRect(acs, st, mx, my)) {
                return;
            }
            int btn = e.getButton();
            boolean inner = inSlot(acs, st, mx, my);
            if (!inner) {
                if (btn == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    st.dragging = true;
                    st.grabX = (int) mx - absX(acs, st);
                    st.grabY = (int) my - absY(acs, st);
                    e.setCanceled(true);
                } else if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    resetPos(acs, st);
                    save(st);
                    e.setCanceled(true);
                }
                return;
            }
            var menu = acs.getMenu();
            if (btn == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                if (TrashClientConfig.UNDO_ENABLED.get() && !lastTrashed.isEmpty() && menu.getCarried().isEmpty()
                        && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
                    menu.setCarried(lastTrashed.copy());
                    lastTrashed = ItemStack.EMPTY;
                }
                e.setCanceled(true);
                return;
            }
            if (btn == GLFW.GLFW_MOUSE_BUTTON_LEFT && !menu.getCarried().isEmpty()) {
                trash(acs, -1, 0);
            }
            e.setCanceled(true);
        }

        @SubscribeEvent
        public void onDrag(ScreenEvent.MouseDragged.Pre e) {
            Screen s = e.getScreen();
            State st = state(s);
            if (st == null || !st.dragging || !(s instanceof AbstractContainerScreen<?> acs)) {
                return;
            }
            int ax = (int) e.getMouseX() - st.grabX;
            int ay = (int) e.getMouseY() - st.grabY;
            if (shiftDown()) {
                ax = Math.round(ax / 8f) * 8;
                ay = Math.round(ay / 8f) * 8;
            }
            ax = Math.max(0, Math.min(s.width - SIZE, ax));
            ay = Math.max(0, Math.min(s.height - SIZE, ay));
            st.relX = ax - acs.getGuiLeft();
            st.relY = ay - acs.getGuiTop();
            e.setCanceled(true);
        }

        @SubscribeEvent
        public void onRelease(ScreenEvent.MouseButtonReleased.Pre e) {
            State st = state(e.getScreen());
            if (st != null && st.dragging) {
                st.dragging = false;
                save(st);
                e.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onKey(ScreenEvent.KeyPressed.Pre e) {
            Screen s = e.getScreen();
            State st = state(s);
            if (st == null || !(s instanceof AbstractContainerScreen<?> acs)) {
                return;
            }
            if (s.getFocused() instanceof EditBox) {
                return;
            }
            var ev = new net.minecraft.client.input.KeyEvent(e.getKeyCode(), e.getScanCode(), e.getModifiers());
            if (TOGGLE.matches(ev)) {
                TrashClientConfig.ENABLED.set(!TrashClientConfig.ENABLED.get());
                TrashClientConfig.SPEC.save();
                e.setCanceled(true);
                return;
            }
            if (visible() && inSlot(acs, st, mouseX(), mouseY()) && hoveredSlot(acs) != null) {
                // handled via click
            }
        }

        @SubscribeEvent
        public void onClosing(ScreenEvent.Closing e) {
            State st = state(e.getScreen());
            if (st != null) {
                st.dragging = false;
                save(st);
            }
            lastTrashed = ItemStack.EMPTY;
            STATES.remove(e.getScreen());
        }

        @SubscribeEvent
        public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
            lastTrashed = ItemStack.EMPTY;
            STATES.clear();
        }
    }

    private static double mouseX() {
        Minecraft mc = Minecraft.getInstance();
        return mc.mouseHandler.getScaledXPos(mc.getWindow());
    }

    private static double mouseY() {
        Minecraft mc = Minecraft.getInstance();
        return mc.mouseHandler.getScaledYPos(mc.getWindow());
    }

    private static void save(State st) {
        if (!TrashClientConfig.REMEMBER_POSITION.get()) {
            return;
        }
        TrashClientConfig.POS_X.set(st.relX);
        TrashClientConfig.POS_Y.set(st.relY);
        TrashClientConfig.HAS_POS.set(true);
        TrashClientConfig.SPEC.save();
    }
}
