package net.diaithir.pipes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A menu with nine ghost slots for the filter of one pipe. A click with a held stack sets the slot; an empty click clears it. */
public final class FilterMenu extends AbstractContainerMenu {
    private final PipeBlockEntity pipe;

    public FilterMenu(int id, Inventory inv, PipeBlockEntity pipe) {
        super(DiaithirPipes.FILTER_MENU.get(), id);
        this.pipe = pipe;
        for (int i = 0; i < ItemFilter.SLOTS; i++) addSlot(new Slot(new SimpleContainer(1), 0, 8 + i * 18, 20) {
            @Override
            public boolean mayPickup(Player p) {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack s) {
                return false;
            }
        });
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 51 + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, 109));
        for (int i = 0; i < ItemFilter.SLOTS; i++) slots.get(i).set(pipe.filter().ghost(i));
    }

    public Component title() {
        return Component.literal("Pipe filter (" + (pipe.filter().blacklist() ? "blacklist" : "whitelist") + ")");
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput type, Player player) {
        if (slotId >= 0 && slotId < ItemFilter.SLOTS) {
            ItemStack held = getCarried();
            pipe.filter().setSlot(slotId, held.isEmpty() ? ItemStack.EMPTY : held.copyWithCount(1));
            slots.get(slotId).set(pipe.filter().ghost(slotId));
            pipe.setChanged();
            return;
        }
        super.clicked(slotId, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos p = pipe.getBlockPos();
        return !pipe.isRemoved() && player.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) <= 64;
    }
}
