package net.diaithir.upgrades;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Module slots of one machine plus the player's inventory. Changes in the slots are saved to the machine at once. */
public final class UpgradeMenu extends AbstractContainerMenu {
    public static final int SLOT_X = 8, SLOT_Y = 20, PER_ROW = 8;

    private final Container modules;
    private final BlockPos pos;
    private final int moduleSlots;

    /** Client side. */
    public UpgradeMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos(), buf.readVarInt(), null);
    }

    /** Server side: pass the machine, slots are filled from it. */
    public UpgradeMenu(int id, Inventory inv, BlockPos pos, int slots, BlockEntity be) {
        super(Upgrades.MENU.get(), id);
        this.pos = pos;
        this.moduleSlots = slots;
        this.modules = be == null ? new SimpleContainer(slots) : new SimpleContainer(slots) {
            @Override
            public void setChanged() {
                super.setChanged();
                save(be);
            }
        };
        if (be != null) {
            var host = Upgrades.host(be);
            if (host.isPresent()) {
                int i = 0;
                for (var m : host.get().slots().modules()) {
                    final int at = i++;
                    Modules.ALL.stream().filter(d -> d.getId().getPath().equals(m.spec().id())).findFirst()
                            .ifPresent(d -> modules.setItem(at, new ItemStack(d.get())));
                }
            }
        }
        for (int i = 0; i < slots; i++) {
            addSlot(new Slot(modules, i, SLOT_X + (i % PER_ROW) * 18, SLOT_Y + (i / PER_ROW) * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof ModuleItem mi && allowed(be, mi.spec());
                }

                @Override
                public int getMaxStackSize() { return 1; }
            });
        }
        int top = 52 + 18 * ((slots - 1) / PER_ROW);
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, top + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, top + 58));
    }

    /** Client has no block entity and lets the server decide; the server checks the machine's rule. */
    private static boolean allowed(BlockEntity be, ModuleSpec spec) {
        if (be == null) return true;
        return Upgrades.targetOf(be).map(t -> t.accepts(spec)).orElse(false);
    }

    private void save(BlockEntity be) {
        Upgrades.targetOf(be).ifPresent(t -> {
            Slotted s = new Slotted(t);
            for (int i = 0; i < modules.getContainerSize(); i++) {
                if (modules.getItem(i).getItem() instanceof ModuleItem mi) s.install(mi.spec(), Quality.NORMAL);
            }
            be.setData(Upgrades.SLOTS, SlotData.of(s));
            be.setChanged();
        });
    }

    public int moduleSlots() { return moduleSlots; }

    public BlockPos pos() { return pos; }

    /** Sum of the slotted modules' own effects (beacons are added by the machine, not shown here). */
    public Effects effects() {
        Effects e = Effects.NONE;
        for (int i = 0; i < modules.getContainerSize(); i++) {
            if (modules.getItem(i).getItem() instanceof ModuleItem mi) e = e.plus(mi.spec().effects(Quality.NORMAL));
        }
        return e;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), copy = stack.copy();
        boolean moved = index < moduleSlots
                ? moveItemStackTo(stack, moduleSlots, slots.size(), true)
                : moveItemStackTo(stack, 0, moduleSlots, false);
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
}
