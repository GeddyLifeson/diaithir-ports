package net.diaithir.filters;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Ghost slot editor: stacks placed in a ghost slot are never taken; clicking one with a stack adds an item rule, clicking it empty removes the rule. */
public final class FilterMenu extends AbstractContainerMenu {
    public static final int GHOSTS = 9;

    private final Player player;
    private final InteractionHand hand;
    private final SimpleContainer ghosts = new SimpleContainer(GHOSTS);

    public FilterMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readEnum(InteractionHand.class));
    }

    public FilterMenu(int id, Inventory inv, InteractionHand hand) {
        super(DiaithirFilters.FILTER_MENU.get(), id);
        this.player = inv.player;
        this.hand = hand;
        for (int i = 0; i < GHOSTS; i++) addSlot(new GhostSlot(ghosts, i, 8 + i * 18, 20));
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 51 + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, 109));
        refresh();
    }

    private ItemStack filterStack() {
        return player.getItemInHand(hand);
    }

    private static String idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    /** Shows each top level item rule as a ghost stack. */
    private void refresh() {
        FilterData d = Filters.data(filterStack());
        int slot = 0;
        for (Rule r : d.rules()) {
            if (slot >= GHOSTS) break;
            if (r instanceof Rule.ItemRule ir) {
                ItemStack shown = ItemStack.EMPTY;
                for (Item item : BuiltInRegistries.ITEM) {
                    if (idOf(item).equals(ir.id())) {
                        shown = new ItemStack(item);
                        break;
                    }
                }
                ghosts.setItem(slot++, shown);
            }
        }
        for (; slot < GHOSTS; slot++) ghosts.setItem(slot, ItemStack.EMPTY);
    }

    private void write(FilterData d) {
        if (d.withinLimits(Config.MAX_RULES.get(), Config.MAX_DEPTH.get())) filterStack().set(DiaithirFilters.FILTER_DATA.get(), d);
        refresh();
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput type, Player p) {
        if (slotId >= 0 && slotId < GHOSTS && !p.level().isClientSide()) {
            ItemStack carried = getCarried();
            FilterData d = Filters.data(filterStack());
            if (carried.isEmpty()) {
                ItemStack shown = ghosts.getItem(slotId);
                if (!shown.isEmpty()) {
                    String id = idOf(shown.getItem());
                    for (int i = 0; i < d.rules().size(); i++) {
                        if (d.rules().get(i) instanceof Rule.ItemRule ir && ir.id().equals(id)) {
                            write(d.without(i));
                            break;
                        }
                    }
                }
            } else {
                String id = idOf(carried.getItem());
                boolean present = d.rules().stream().anyMatch(r -> r instanceof Rule.ItemRule ir && ir.id().equals(id));
                if (!present) write(d.with(new Rule.ItemRule(id)));
            }
            broadcastChanges();
            return;
        }
        if (slotId >= GHOSTS && slots.get(slotId).getItem() == filterStack()) return;
        super.clicked(slotId, button, type, p);
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player p) {
        return Filters.isFilter(p.getItemInHand(hand));
    }

    private static final class GhostSlot extends Slot {
        GhostSlot(SimpleContainer c, int i, int x, int y) {
            super(c, i, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack s) {
            return false;
        }

        @Override
        public boolean mayPickup(Player p) {
            return false;
        }
    }
}
