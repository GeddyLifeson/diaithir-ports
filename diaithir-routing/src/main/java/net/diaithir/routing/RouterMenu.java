package net.diaithir.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Router menu. Slots 0-8: modules (backed by the router), slot 9: range-card sink,
 * slots 10-18: ghost filter, then the player inventory.
 * Buttons: 0 blacklist, 1 match tag, 2 match durability, 3 ignore data.
 */
public final class RouterMenu extends AbstractContainerMenu {
    public static final int MODULE_START = 0;
    public static final int RANGE_SLOT = 9;
    public static final int GHOST_START = 10;
    public static final int PLAYER_START = 19;

    private final RouterBlockEntity router;
    private final Container clientRange = new SimpleContainer(1);
    private int clientRangeValue;

    /** Server constructor. */
    public RouterMenu(int id, Inventory inv, RouterBlockEntity router) {
        super(DiaithirRouting.ROUTER_MENU.get(), id);
        this.router = router;
        build(inv);
    }

    /** Client constructor: no block entity is reachable, so a detached one carries mirrored state. */
    public static RouterMenu client(int id, Inventory inv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        RouterBlockEntity be = inv.player.level().getBlockEntity(pos) instanceof RouterBlockEntity r ? r
                : new RouterBlockEntity(pos, DiaithirRouting.ROUTER.get().defaultBlockState());
        return new RouterMenu(id, inv, be);
    }

    private void build(Inventory inv) {
        for (int i = 0; i < RouterBlockEntity.MAX_MODULES; i++) {
            final int idx = i;
            addSlot(new Slot(new SimpleContainer(0), idx, 8 + idx * 18, 18) {
                @Override
                public ItemStack getItem() {
                    return router.moduleStack(idx);
                }

                @Override
                public void set(ItemStack stack) {
                    router.setModuleStack(idx, stack);
                }

                @Override
                public void setByPlayer(ItemStack stack) {
                    set(stack);
                }

                @Override
                public boolean hasItem() {
                    return !getItem().isEmpty();
                }

                @Override
                public boolean mayPlace(ItemStack s) {
                    return s.getItem() instanceof ModuleItem && !hasItem() && idx == router.moduleCount();
                }

                @Override
                public boolean mayPickup(Player p) {
                    return hasItem();
                }

                @Override
                public ItemStack remove(int amount) {
                    ItemStack s = getItem();
                    router.setModuleStack(idx, ItemStack.EMPTY);
                    return s;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public void setChanged() {
                    router.setChanged();
                }
            });
        }
        addSlot(new Slot(clientRange, 0, 8, 46) {
            @Override
            public boolean mayPlace(ItemStack s) {
                return RoutingRefs.isRangeCard(s);
            }

            @Override
            public boolean mayPickup(Player p) {
                return false;
            }

            @Override
            public void setByPlayer(ItemStack stack) {
                absorb(stack);
            }

            @Override
            public void set(ItemStack stack) {
                absorb(stack);
            }

            private void absorb(ItemStack stack) {
                if (RoutingRefs.isRangeCard(stack)) {
                    for (int n = stack.getCount(); n > 0; n--) router.addRangeCard();
                    clientRangeValue = router.range();
                }
            }
        });
        for (int i = 0; i < FilterData.MAX_ENTRIES; i++) {
            final int idx = i;
            addSlot(new Slot(new SimpleContainer(0), idx, 8 + idx * 18, 76) {
                @Override
                public ItemStack getItem() {
                    List<ItemStack> e = router.filter().entries();
                    return idx < e.size() ? e.get(idx) : ItemStack.EMPTY;
                }

                @Override
                public boolean hasItem() {
                    return !getItem().isEmpty();
                }

                @Override
                public boolean mayPlace(ItemStack s) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player p) {
                    return false;
                }

                @Override
                public void set(ItemStack stack) {
                }
            });
        }
        int py = 108;
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, py + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, py + 58));
    }

    public RouterBlockEntity router() {
        return router;
    }

    public int rangeValue() {
        return router.range();
    }

    public FilterData filter() {
        return router.filter();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button < 0 || button > 3) return false;
        router.toggleFlag(button);
        return true;
    }

    private void setGhost(int idx, ItemStack carried) {
        List<ItemStack> next = new ArrayList<>(router.filter().entries());
        if (carried.isEmpty()) {
            if (idx < next.size()) next.remove(idx);
        } else if (idx < next.size()) {
            next.set(idx, carried.copyWithCount(1));
        } else if (next.size() < FilterData.MAX_ENTRIES) {
            next.add(carried.copyWithCount(1));
        }
        FilterData f = router.filter();
        router.setFilter(new FilterData(List.copyOf(next), f.blacklist(), f.matchTag(), f.matchDurability(), f.ignoreData()));
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput type, Player player) {
        if (slotId >= GHOST_START && slotId < PLAYER_START) {
            if (type == ContainerInput.PICKUP || type == ContainerInput.QUICK_MOVE) setGhost(slotId - GHOST_START, type == ContainerInput.PICKUP ? getCarried() : ItemStack.EMPTY);
            return;
        }
        super.clicked(slotId, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        if (index >= GHOST_START && index < PLAYER_START) return ItemStack.EMPTY;
        ItemStack in = slot.getItem();
        ItemStack copy = in.copy();
        if (index < RANGE_SLOT) {
            if (!moveItemStackTo(in, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
            router.setModuleStack(index, ItemStack.EMPTY);
            return copy;
        }
        if (index >= PLAYER_START) {
            if (in.getItem() instanceof ModuleItem && router.moduleCount() < RouterBlockEntity.MAX_MODULES) {
                router.setModuleStack(router.moduleCount(), in.copyWithCount(1));
                in.shrink(1);
            } else if (RoutingRefs.isRangeCard(in)) {
                for (int n = in.getCount(); n > 0; n--) router.addRangeCard();
                in.setCount(0);
            } else {
                return ItemStack.EMPTY;
            }
            if (in.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
            return copy;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        // owns nothing in player-side containers: the range slot consumes its input, ghosts hold copies, modules live in the router
        super.removed(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return router.getLevel() != null && router.getLevel().getBlockEntity(router.getBlockPos()) == router
                && player.distanceToSqr(router.getBlockPos().getX() + 0.5, router.getBlockPos().getY() + 0.5, router.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}
