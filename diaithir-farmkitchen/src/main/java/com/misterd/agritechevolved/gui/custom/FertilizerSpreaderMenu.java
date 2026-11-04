package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.blockentity.custom.FertilizerSpreaderBlockEntity;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
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

public class FertilizerSpreaderMenu extends AbstractContainerMenu {

    private static final int PLAYER_SLOTS = 36;
    private static final int STORAGE_SLOTS = 63;
    private static final int MODULE_SLOT = 63;
    private static final int TE_SLOT_COUNT = 64;
    private static final int TE_FIRST_SLOT = PLAYER_SLOTS;
    private static final int TE_LAST_SLOT = TE_FIRST_SLOT + TE_SLOT_COUNT;

    public final FertilizerSpreaderBlockEntity blockEntity;
    private final Level level;

    private int lastEnergyStored = 0;

    public FertilizerSpreaderMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public FertilizerSpreaderMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ATEMenuTypes.FERTILIZER_SPREADER_MENU.get(), containerId);
        this.blockEntity = (FertilizerSpreaderBlockEntity) blockEntity;
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
                addSlot(new SpreaderSlot(blockEntity, idx++, 8 + col * 18, 19 + row * 18));

        addSlot(new SpreaderSlot(blockEntity, MODULE_SLOT, 176, 19));
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
        if (isFertilizer(stack)) {
            return insertIntoBlockEntity(stack, 0, STORAGE_SLOTS);
        }
        if (stack.is(ATETags.Items.ATE_RANGE_MODULES) && blockEntity.getStack(MODULE_SLOT).isEmpty()) {
            insertSingle(stack, MODULE_SLOT);
            return true;
        }
        return false;
    }

    private static boolean isFertilizer(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem().builtInRegistryHolder().getData(ATEDataMaps.FERTILIZERS) != null;
    }

    private boolean insertIntoBlockEntity(ItemStack stack, int startSlot, int endSlot) {
        if (stack.isEmpty()) return false;
        int inserted = 0;

        for (int i = startSlot; i < endSlot && !stack.isEmpty(); i++) {
            ItemStack existing = blockEntity.getStack(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) continue;
            int space = stack.getMaxStackSize() - existing.getCount();
            if (space <= 0) continue;
            int toInsert = Math.min(space, stack.getCount());
            try (Transaction tx = Transaction.openRoot()) {
                int actual = blockEntity.inventory.insert(i, ItemResource.of(stack), toInsert, tx);
                tx.commit();
                stack.shrink(actual);
                inserted += actual;
            }
        }

        for (int i = startSlot; i < endSlot && !stack.isEmpty(); i++) {
            if (!blockEntity.getStack(i).isEmpty()) continue;
            if (!blockEntity.inventory.isValid(i, ItemResource.of(stack))) continue;
            int toInsert = Math.min(stack.getMaxStackSize(), stack.getCount());
            try (Transaction tx = Transaction.openRoot()) {
                int actual = blockEntity.inventory.insert(i, ItemResource.of(stack), toInsert, tx);
                tx.commit();
                stack.shrink(actual);
                inserted += actual;
            }
        }

        return inserted > 0;
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
                player, ATEBlocks.FERT_SPREADER.get());
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

    private static class SpreaderSlot extends Slot {
        private final FertilizerSpreaderBlockEntity be;
        private final int index;

        SpreaderSlot(FertilizerSpreaderBlockEntity be, int index, int x, int y) {
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
            return be.inventory.isValid(index, ItemResource.of(stack));
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