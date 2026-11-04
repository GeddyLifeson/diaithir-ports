package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.blockentity.custom.SiloBlockEntity;
import com.misterd.agritechevolved.gui.ATEMenuTypes;
import com.misterd.agritechevolved.util.ATETags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class SiloMenu extends AbstractContainerMenu {

    private static final int PLAYER_SLOTS = 36;
    private static final int STORAGE_SLOTS = 63;
    private static final int MODULE_SLOT = 63;
    private static final int TE_SLOT_COUNT = 64;
    private static final int TE_FIRST_SLOT = PLAYER_SLOTS;
    private static final int TE_LAST_SLOT = TE_FIRST_SLOT + TE_SLOT_COUNT;

    public final SiloBlockEntity blockEntity;
    private final Level level;

    private int lastEnergyStored = 0;

    public SiloMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public SiloMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ATEMenuTypes.SILO_MENU.get(), containerId);
        this.blockEntity = (SiloBlockEntity) blockEntity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addBlockEntitySlots();
        addDataSlots();
    }

    private void addBlockEntitySlots() {
        int idx = 0;
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new SiloSlot(blockEntity, idx++, 8 + col * 18, 19 + row * 18));

        addSlot(new SiloSlot(blockEntity, MODULE_SLOT, 176, 19));
    }

    private void addDataSlots() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() { return blockEntity.getEnergyStored(); }

            @Override
            public void set(int value) { lastEnergyStored = value; }
        });
    }

    public int getEnergyStored() {
        return level.isClientSide() ? lastEnergyStored : blockEntity.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return blockEntity.getMaxEnergyStored();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (source == null || !source.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = source.getItem();
        ItemStack copy = stack.copy();

        if (index < PLAYER_SLOTS) {
            if (!moveToBlockEntity(stack)) return ItemStack.EMPTY;
        } else {
            if (index >= TE_LAST_SLOT) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, 0, PLAYER_SLOTS, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();

        source.onTake(player, stack);
        return copy;
    }

    private boolean moveToBlockEntity(ItemStack stack) {
        if (stack.is(ATETags.Items.ATE_RANGE_MODULES) && blockEntity.getStack(MODULE_SLOT).isEmpty()) {
            insertSingle(stack, MODULE_SLOT);
            return true;
        }
        return false;
    }

    private void insertSingle(ItemStack stack, int slot) {
        int actual;
        try (Transaction tx = Transaction.openRoot()) {
            actual = blockEntity.inventory.insert(slot, ItemResource.of(stack), 1, tx);
            if (actual > 0) tx.commit();
        }
        if (actual > 0) stack.shrink(actual);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ATEBlocks.SILO.get());
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 160 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, 8 + i * 18, 218));
    }

    private static class SiloSlot extends Slot {
        private final SiloBlockEntity be;
        private final int index;

        SiloSlot(SiloBlockEntity be, int index, int x, int y) {
            super(new SimpleContainer(be.inventory.size()), index, x, y);
            this.be = be;
            this.index = index;
            container.setItem(index, be.getStack(index));
        }

        @Override
        public ItemStack getItem() {
            Level lvl = be.getLevel();
            if (lvl != null && lvl.isClientSide()) return container.getItem(index);
            return be.getStack(index);
        }

        @Override
        public void set(ItemStack stack) {
            container.setItem(index, stack.copy());
            Level lvl = be.getLevel();
            if (lvl == null || lvl.isClientSide()) {
                setChanged();
                return;
            }
            try (Transaction tx = Transaction.openRoot()) {
                ItemStack existing = be.getStack(index);
                if (!existing.isEmpty())
                    be.inventory.extract(index, ItemResource.of(existing), existing.getCount(), tx);
                if (!stack.isEmpty()) {
                    long cap = be.inventory.getCapacityAsLong(index, ItemResource.of(stack));
                    be.inventory.insert(index, ItemResource.of(stack), (int) Math.min(stack.getCount(), cap), tx);
                }
                tx.commit();
            }
            setChanged();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (index == MODULE_SLOT) return be.inventory.isValid(index, ItemResource.of(stack));
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return index == MODULE_SLOT ? 1 : 64;
        }

        @Override
        public ItemStack remove(int amount) {
            ItemStack existing = be.getStack(index);
            if (existing.isEmpty()) return ItemStack.EMPTY;
            int toExtract = Math.min(amount, existing.getCount());
            try (Transaction tx = Transaction.openRoot()) {
                int extracted = be.inventory.extract(index, ItemResource.of(existing), toExtract, tx);
                tx.commit();
                return existing.copyWithCount(extracted);
            }
        }
    }
}