package net.diaithir.portable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** The one menu for storage blocks and backpacks: main grid, upgrade row, player inventory. Buttons: 0 sort, 1 lock toggle. */
public final class PortableMenu extends AbstractContainerMenu {
    public static final int SORT = 0;
    public static final int LOCK = 1;

    private final Tier tier;
    private final Container main;
    private final Container upgrades;
    private final Runnable save;
    private final LockHolder lock;
    private final int mainSlots;
    private final int upgradeSlots;

    /** Anything that can hold a lock flag: the block entity or the backpack stack. */
    public interface LockHolder {
        boolean locked();

        void setLocked(boolean b);
    }

    public PortableMenu(int id, Inventory inv, Tier tier, Container main, Container upgrades, LockHolder lock, Runnable save) {
        super(DiaithirPortable.PORTABLE_MENU.get(), id);
        this.tier = tier;
        this.main = main;
        this.upgrades = upgrades;
        this.save = save;
        this.lock = lock;
        this.mainSlots = tier.slots;
        this.upgradeSlots = tier.upgradeSlots;
        int cols = tier.columns;
        for (int i = 0; i < tier.slots; i++) addSlot(new Slot(main, i, 8 + (i % cols) * 18, 18 + (i / cols) * 18));
        int uy = 18 + tier.rows() * 18 + 6;
        for (int i = 0; i < tier.upgradeSlots; i++) addSlot(new Slot(upgrades, i, 8 + i * 18, uy) {
            @Override
            public boolean mayPlace(ItemStack s) {
                return s.getItem() instanceof UpgradeItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        int py = uy + 30;
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, py + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, py + 58));
    }

    public static PortableMenu client(int id, Inventory inv, FriendlyByteBuf buf) {
        Tier t = Tier.byOrdinal(buf.readVarInt());
        net.diaithir.portable.Contents main = new net.diaithir.portable.Contents(t.slots, () -> {
        });
        net.diaithir.portable.Contents ups = new net.diaithir.portable.Contents(t.upgradeSlots, () -> {
        });
        boolean[] locked = {buf.readBoolean()};
        return new PortableMenu(id, inv, t, main, ups, new LockHolder() {
            public boolean locked() {
                return locked[0];
            }

            public void setLocked(boolean b) {
                locked[0] = b;
            }
        }, () -> {
        });
    }

    public Tier tier() {
        return tier;
    }

    public boolean locked() {
        return lock.locked();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == SORT) {
            sort();
            return true;
        }
        if (button == LOCK) {
            lock.setLocked(!lock.locked());
            save.run();
            return true;
        }
        return false;
    }

    private void sort() {
        List<ItemStack> all = new ArrayList<>();
        for (int i = 0; i < main.getContainerSize(); i++) {
            ItemStack s = main.getItem(i);
            if (!s.isEmpty()) all.add(s.copy());
            main.setItem(i, ItemStack.EMPTY);
        }
        all.sort(Comparator.comparing((ItemStack s) -> s.getItem().getDescriptionId()).thenComparing(s -> -s.getCount()));
        Set<UpgradeKind> kinds = Upgrades.kinds(upgrades);
        int limit = Upgrades.limit(upgrades);
        for (ItemStack s : all) Upgrades.insert(main, s, kinds, limit);
        main.setChanged();
        save.run();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack in = slot.getItem();
        ItemStack copy = in.copy();
        int mainEnd = mainSlots;
        int upEnd = mainSlots + upgradeSlots;
        if (index < upEnd) {
            if (!moveItemStackTo(in, upEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else if (in.getItem() instanceof UpgradeItem && moveItemStackTo(in, mainEnd, upEnd, false)) {
            // moved into upgrade row
        } else if (!lock.locked() || containsSame(in)) {
            if (!moveItemStackTo(in, 0, mainEnd, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (in.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    private boolean containsSame(ItemStack s) {
        for (int i = 0; i < main.getContainerSize(); i++) if (ItemStack.isSameItemSameComponents(main.getItem(i), s)) return true;
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput type, Player player) {
        if (lock.locked() && slotId >= 0 && slotId < mainSlots && type == ContainerInput.PICKUP && !getCarried().isEmpty() && main.getItem(slotId).isEmpty()) return;
        super.clicked(slotId, button, type, player);
        save.run();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        save.run();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static Component title(Tier t) {
        return Component.translatable("container.diaithirportable." + t.id);
    }
}
