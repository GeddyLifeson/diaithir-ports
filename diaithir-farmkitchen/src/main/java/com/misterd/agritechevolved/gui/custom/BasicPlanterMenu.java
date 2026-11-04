package com.misterd.agritechevolved.gui.custom;

import com.misterd.agritechevolved.block.custom.PlanterBlock;
import com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.gui.ATEMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class BasicPlanterMenu extends AbstractContainerMenu {

    public final PlanterBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private static final int PLAYER_SLOTS = 36;
    private static final int TE_SLOT_PLANT = PLAYER_SLOTS;
    private static final int TE_SLOT_SOIL = PLAYER_SLOTS + 1;
    private static final int TE_SLOT_FERT = PLAYER_SLOTS + 2;
    private static final int TE_OUTPUT_START = PLAYER_SLOTS + 3;
    private static final int TE_OUTPUT_END = PLAYER_SLOTS + 15;
    private static final int TE_LAST_SLOT = TE_OUTPUT_END;

    public BasicPlanterMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BasicPlanterMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ATEMenuTypes.PLANTER_BLOCK_MENU.get(), containerId);
        this.blockEntity = (PlanterBlockEntity) blockEntity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addSlot(new PlanterSlot(this.blockEntity, 0, 8, 18));
        addSlot(new PlanterSlot(this.blockEntity, 1, 8, 54));
        addSlot(new FertilizerSlot(this.blockEntity, 2, 152, 18));

        int slotIndex = 3;
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 4; col++)
                addSlot(new PlanterSlot(this.blockEntity, slotIndex++, 62 + col * 18, 18 + row * 18));

        PlanterBlockEntity be = this.blockEntity;
        if (be != null) {
            this.data = new ContainerData() {
                @Override public int get(int index) { return index == 0 ? be.growthProgress : 0; }
                @Override public void set(int index, int value) { if (index == 0) be.growthProgress = value; }
                @Override public int getCount() { return 1; }
            };
        } else {
            this.data = new SimpleContainerData(1);
        }
        addDataSlots(this.data);
    }

    public int getGrowthProgress() {
        return data.get(0);
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
            if (!moveItemStackTo(stack, 0, PLAYER_SLOTS, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();

        source.onTake(player, stack);
        return copy;
    }

    private boolean moveToBlockEntity(ItemStack stack) {
        if (blockEntity.isValidPlant(stack)) {
            if (!blockEntity.getStack(0).isEmpty()) return false;
            ItemStack soil = blockEntity.getStack(1);
            if (!soil.isEmpty() && !blockEntity.isValidPlantSoilCombination(stack, soil)) return false;
            insertSingle(stack, 0);
            return true;
        }

        if (blockEntity.isValidSoilForAnyRecipe(stack)) {
            if (!blockEntity.getStack(1).isEmpty()) return false;
            ItemStack plant = blockEntity.getStack(0);
            if (!plant.isEmpty() && !blockEntity.isValidPlantSoilCombination(plant, stack)) return false;
            insertSingle(stack, 1);
            return true;
        }

        if (isFertilizer(stack)) {
            return insertIntoBlockEntity(stack, 2, 3);
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
        try (Transaction tx = Transaction.openRoot()) {
            blockEntity.inventory.insert(slot, ItemResource.of(stack), 1, tx);
            tx.commit();
        }
        stack.shrink(1);
    }

    @Override
    public boolean stillValid(Player player) {
        Block block = blockEntity.getBlockState().getBlock();
        return block instanceof PlanterBlock
                && stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, block);
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 88 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, 8 + i * 18, 146));
    }

    private static class PlanterSlot extends Slot {
        private final PlanterBlockEntity be;
        private final int index;

        PlanterSlot(PlanterBlockEntity be, int index, int x, int y) {
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
            return (index == 0 || index == 1) ? 1 : 64;
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

    private static class FertilizerSlot extends PlanterSlot {
        FertilizerSlot(PlanterBlockEntity be, int index, int x, int y) {
            super(be, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !stack.isEmpty() && stack.getItem().builtInRegistryHolder().getData(ATEDataMaps.FERTILIZERS) != null;
        }
    }
}