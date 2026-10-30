package net.diaithir.forgeworks;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Container menu shared by all four machines. Button ids: 0 redstone, 1 to 6 item mode of a face, 7 to 12 energy of a face. */
public final class MachineMenu extends AbstractContainerMenu {
    public final Kind kind;
    private final Container container;
    private final ContainerData data;
    private final int machineSlots;
    private final int upgradeIndex;

    /** Client side. */
    public MachineMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, Kind.values()[buf.readVarInt()], new SimpleContainer(Kind.SIZE), new SimpleContainerData(MachineBlockEntity.DATA_COUNT));
    }

    public MachineMenu(int id, Inventory inv, Kind kind, Container container, ContainerData data) {
        super(Machines.MENU.get(), id);
        this.kind = kind;
        this.container = container;
        this.data = data;
        int n = 0;
        if (kind.inputs == 1) {
            addSlot(new Slot(container, Kind.IN0, 30, 36));
            n++;
        } else {
            addSlot(new Slot(container, Kind.IN0, 30, 26));
            addSlot(new Slot(container, Kind.IN1, 30, 46));
            n += 2;
        }
        if (!kind.fluid) {
            int oy = kind.bonus ? 26 : 36;
            addSlot(new OutSlot(container, Kind.OUT, 86, oy));
            n++;
            if (kind.bonus) {
                addSlot(new OutSlot(container, Kind.BONUS, 86, 46));
                n++;
            }
        }
        upgradeIndex = n;
        addSlot(new Slot(container, Kind.UPGRADE, 150, 55) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof AugmentItem;
            }
        });
        n++;
        machineSlots = n;
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, 142));
        addDataSlots(data);
    }

    private static final class OutSlot extends Slot {
        OutSlot(Container c, int i, int x, int y) {
            super(c, i, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    // ---- values for the screen -----------------------------------------------------------------------------------------------------

    public int progressPermille() {
        return data.get(0);
    }

    public int energy() {
        return ((data.get(2) & 0xFFFF) << 16) | (data.get(1) & 0xFFFF);
    }

    public int capacity() {
        return ((data.get(4) & 0xFFFF) << 16) | (data.get(3) & 0xFFFF);
    }

    public int redstone() {
        return data.get(5);
    }

    public int itemMode(int face) {
        return (data.get(6) >> (2 * face)) & 3;
    }

    public boolean energyOn(int face) {
        return (data.get(7) & (1 << face)) != 0;
    }

    public int fluidAmount() {
        return ((data.get(9) & 0xFFFF) << 16) | (data.get(8) & 0xFFFF);
    }

    public int fluidCapacity() {
        return (data.get(10) & 0xFFFF) * 100;
    }

    public int fluidId() {
        return data.get(11);
    }

    public int draw() {
        return data.get(12) & 0xFFFF;
    }

    public int speedPercent() {
        return data.get(13) & 0xFFFF;
    }

    // ---- behaviour -----------------------------------------------------------------------------------------------------------------

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(container instanceof MachineBlockEntity be)) return false;
        if (id == 0) be.cycleRedstone();
        else if (id >= 1 && id <= 6) be.cycleItemMode(id - 1);
        else if (id >= 7 && id <= 12) be.toggleEnergy(id - 7);
        else return false;
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int end = slots.size();
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, end, true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof AugmentItem) {
            if (!moveItemStackTo(stack, upgradeIndex, upgradeIndex + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, kind.inputs, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
