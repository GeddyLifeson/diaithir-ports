package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * The search and sort grid. It is a vanilla 9x6 chest screen, so players install nothing. The top five rows show the network's items
 * (display copies only: clicks are handled here and move items through the network, so nothing can be duplicated or lost);
 * the bottom row is previous page, next page, sort mode, page and refresh, search and clear.
 * Left click an item to take a stack, right click for one, shift click to send a stack to your inventory; click with an item
 * on the cursor to store it; shift click your own items to store them. Search with /diaithirstoragenet search text (or @mod).
 */
public final class TerminalMenu extends ChestMenu {
    public enum Sort { NAME, COUNT, MOD }

    static final int VIEW = 45;
    static final int TOTAL = 54;
    private static final Map<UUID, String> SEARCH = new java.util.HashMap<>();

    private final ServerLevel level;
    private final BlockPos terminal;
    private final SimpleContainer view;
    private final List<ItemResource> shown = new ArrayList<>();
    private int page;
    private Sort sort = Sort.NAME;
    private int pages = 1;

    public TerminalMenu(int id, Inventory inv, ServerLevel level, BlockPos terminal) {
        this(id, inv, level, terminal, new SimpleContainer(TOTAL));
    }

    private TerminalMenu(int id, Inventory inv, ServerLevel level, BlockPos terminal, SimpleContainer view) {
        super(MenuType.GENERIC_9x6, id, inv, view, 6);
        this.level = level;
        this.terminal = terminal;
        this.view = view;
        refresh(inv.player);
    }

    public static void setSearch(Player p, String text) {
        if (text.isBlank()) SEARCH.remove(p.getUUID());
        else SEARCH.put(p.getUUID(), text.trim());
    }

    public static String search(Player p) {
        return SEARCH.getOrDefault(p.getUUID(), "");
    }

    public static void clearSearch(Player p) {
        SEARCH.remove(p.getUUID());
    }

    public Sort sort() {
        return sort;
    }

    public void setSort(Sort s) {
        sort = s;
    }

    public int page() {
        return page;
    }

    public int pages() {
        return pages;
    }

    public List<ItemResource> shown() {
        return shown;
    }

    private static ItemStack label(ItemStack base, String name, String... lore) {
        base.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        if (lore.length > 0) {
            List<Component> l = new ArrayList<>();
            for (String s : lore) l.add(Component.literal(s));
            base.set(DataComponents.LORE, new ItemLore(l));
        }
        return base;
    }

    /** Rebuilds the grid from the network. Runs on open and after clicks, never per tick. */
    public void refresh(Player player) {
        Network net = Networks.find(level, terminal);
        Map<ItemResource, Long> all = net == null ? Map.of() : net.contents();
        String q = search(player).toLowerCase(Locale.ROOT);
        List<Map.Entry<ItemResource, Long>> list = new ArrayList<>();
        for (Map.Entry<ItemResource, Long> e : all.entrySet()) if (matches(e.getKey(), q)) list.add(e);
        Comparator<Map.Entry<ItemResource, Long>> byName = Comparator.comparing(e -> e.getKey().getHoverName().getString().toLowerCase(Locale.ROOT));
        switch (sort) {
            case NAME -> list.sort(byName);
            case COUNT -> list.sort(Comparator.<Map.Entry<ItemResource, Long>>comparingLong(e -> -e.getValue()).thenComparing(byName));
            case MOD -> list.sort(Comparator.<Map.Entry<ItemResource, Long>, String>comparing(e -> ns(e.getKey())).thenComparing(byName));
        }
        pages = Math.max(1, (list.size() + VIEW - 1) / VIEW);
        page = Math.max(0, Math.min(page, pages - 1));
        shown.clear();
        for (int i = 0; i < VIEW; i++) {
            int idx = page * VIEW + i;
            if (idx < list.size()) {
                Map.Entry<ItemResource, Long> e = list.get(idx);
                shown.add(e.getKey());
                ItemStack st = e.getKey().toStack((int) Math.max(1, Math.min(e.getValue(), e.getKey().getMaxStackSize())));
                st.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Stored: " + e.getValue()),
                        Component.literal("Left click: a stack. Right click: one."))));
                view.setItem(i, st);
            } else {
                view.setItem(i, ItemStack.EMPTY);
            }
        }
        view.setItem(45, label(new ItemStack(Items.ARROW), "Previous page"));
        view.setItem(46, label(new ItemStack(Items.ARROW), "Next page"));
        view.setItem(47, label(new ItemStack(Items.HOPPER), "Sort: " + sort.name().toLowerCase(Locale.ROOT), "Click to change"));
        view.setItem(48, label(new ItemStack(Items.COMPASS), "Page " + (page + 1) + " of " + pages, list.size() + " kinds of item", "Click to refresh"));
        view.setItem(49, label(new ItemStack(Items.NAME_TAG), q.isEmpty() ? "No search" : "Search: " + q, "/diaithirstoragenet search <text>", "@mod searches a mod id"));
        view.setItem(50, label(new ItemStack(Items.BARRIER), "Clear search"));
        for (int i = 51; i < TOTAL; i++) view.setItem(i, label(new ItemStack(Items.GRAY_STAINED_GLASS_PANE), " "));
    }

    private static String ns(ItemResource r) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(r.getItem()).getNamespace();
    }

    private static boolean matches(ItemResource r, String q) {
        if (q.isEmpty()) return true;
        if (q.startsWith("@")) return ns(r).contains(q.substring(1));
        return r.getHoverName().getString().toLowerCase(Locale.ROOT).contains(q);
    }

    @Override
    public void clicked(int slot, int button, ContainerInput input, Player player) {
        if (slot >= 0 && slot < TOTAL) {
            viewClick(slot, button, input, player);
            return;
        }
        super.clicked(slot, button, input, player);
    }

    private void viewClick(int slot, int button, ContainerInput input, Player player) {
        Network net = Networks.find(level, terminal);
        if (net != null && (input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE)) {
            if (slot < VIEW) {
                ItemStack carried = getCarried();
                if (!carried.isEmpty() && input == ContainerInput.PICKUP) {
                    ItemStack put = button == 1 ? carried.copyWithCount(1) : carried.copy();
                    ItemStack rest = net.insertStack(put);
                    int moved = put.getCount() - rest.getCount();
                    carried.shrink(moved);
                    setCarried(carried);
                } else if (carried.isEmpty() && slot < shown.size()) {
                    ItemResource r = shown.get(slot);
                    int want = button == 1 && input == ContainerInput.PICKUP ? 1 : r.getMaxStackSize();
                    ItemStack got = net.extractStack(r, want);
                    if (!got.isEmpty()) {
                        if (input == ContainerInput.QUICK_MOVE) {
                            player.getInventory().add(got);
                            if (!got.isEmpty()) net.insertStack(got); // inventory full: put the rest back
                        } else {
                            setCarried(got);
                        }
                    }
                }
            } else if (input == ContainerInput.PICKUP) {
                switch (slot) {
                    case 45 -> page = Math.max(0, page - 1);
                    case 46 -> page = page + 1;
                    case 47 -> sort = Sort.values()[(sort.ordinal() + 1) % Sort.values().length];
                    case 50 -> clearSearch(player);
                    default -> { }
                }
            }
        }
        refresh(player);
        sendAllDataToRemote();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < TOTAL) return ItemStack.EMPTY;
        Slot s = slots.get(slotIndex);
        if (!s.hasItem()) return ItemStack.EMPTY;
        Network net = Networks.find(level, terminal);
        if (net == null) return ItemStack.EMPTY;
        ItemStack stack = s.getItem();
        ItemStack before = stack.copy();
        ItemStack rest = net.insertStack(stack.copy());
        if (rest.getCount() == stack.getCount()) return ItemStack.EMPTY;
        if (rest.isEmpty()) s.setByPlayer(ItemStack.EMPTY);
        else s.setByPlayer(rest);
        s.setChanged();
        return before;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
        return target.container != view && super.canTakeItemForPickAll(carried, target);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.container != view && super.canDragTo(slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return level.getBlockState(terminal).getBlock() instanceof TerminalBlock
                && player.distanceToSqr(terminal.getX() + 0.5, terminal.getY() + 0.5, terminal.getZ() + 0.5) <= Math.pow(Config.MAX_TERMINAL_DISTANCE.get(), 2);
    }
}
