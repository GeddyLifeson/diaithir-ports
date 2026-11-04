package net.diaithir.linkedstorage;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/** Capability handlers over the shared stores. Each reads and writes LinkedData directly so every block on a channel sees one state. */
public final class SharedHandlers {
    private SharedHandlers() {}

    public static final class Items implements IItemHandlerModifiable {
        private final LinkedBlockEntity be;

        Items(LinkedBlockEntity be) {
            this.be = be;
        }

        private NonNullList<ItemStack> list() {
            return be.shared().chest(be.key());
        }

        @Override
        public int getSlots() {
            return LinkedData.CHEST_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return list().get(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            list().set(slot, stack);
            be.shared().setDirty();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            ItemStack cur = list().get(slot);
            int cap = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            if (!cur.isEmpty() && !ItemStack.isSameItemSameComponents(cur, stack)) return stack;
            int room = cap - cur.getCount();
            if (room <= 0) return stack;
            int move = Math.min(room, Math.min(stack.getCount(), Config.ITEMS_PER_OPERATION.get()));
            if (!simulate) {
                if (cur.isEmpty()) list().set(slot, stack.copyWithCount(move));
                else cur.grow(move);
                be.shared().setDirty();
            }
            return move >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - move);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack cur = list().get(slot);
            if (cur.isEmpty() || amount <= 0) return ItemStack.EMPTY;
            int take = Math.min(Math.min(amount, cur.getCount()), Config.ITEMS_PER_OPERATION.get());
            ItemStack out = cur.copyWithCount(take);
            if (!simulate) {
                cur.shrink(take);
                if (cur.isEmpty()) list().set(slot, ItemStack.EMPTY);
                be.shared().setDirty();
            }
            return out;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }

    public static final class Fluids implements IFluidHandler {
        private final LinkedBlockEntity be;

        Fluids(LinkedBlockEntity be) {
            this.be = be;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return be.shared().tank(be.key());
        }

        @Override
        public int getTankCapacity(int tank) {
            return Config.TANK_CAPACITY.get();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;
            FluidStack cur = be.shared().tank(be.key());
            if (!cur.isEmpty() && !FluidStack.isSameFluidSameComponents(cur, resource)) return 0;
            int room = Config.TANK_CAPACITY.get() - cur.getAmount();
            int move = Math.min(Math.min(room, resource.getAmount()), Config.FLUID_PER_OPERATION.get());
            if (move <= 0) return 0;
            if (action.execute()) be.shared().setTank(be.key(), resource.copyWithAmount(cur.getAmount() + move));
            return move;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack cur = be.shared().tank(be.key());
            if (cur.isEmpty() || !FluidStack.isSameFluidSameComponents(cur, resource)) return FluidStack.EMPTY;
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack cur = be.shared().tank(be.key());
            if (cur.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
            int take = Math.min(Math.min(maxDrain, cur.getAmount()), Config.FLUID_PER_OPERATION.get());
            FluidStack out = cur.copyWithAmount(take);
            if (action.execute()) {
                int left = cur.getAmount() - take;
                be.shared().setTank(be.key(), left <= 0 ? FluidStack.EMPTY : cur.copyWithAmount(left));
            }
            return out;
        }
    }

    public static final class Energy implements IEnergyStorage {
        private final LinkedBlockEntity be;

        Energy(LinkedBlockEntity be) {
            this.be = be;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int cur = be.shared().cell(be.key());
            int move = Math.min(Math.min(maxReceive, Config.CELL_CAPACITY.get() - cur), Config.ENERGY_PER_OPERATION.get());
            if (move <= 0) return 0;
            if (!simulate) be.shared().setCell(be.key(), cur + move);
            return move;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int cur = be.shared().cell(be.key());
            int move = Math.min(Math.min(maxExtract, cur), Config.ENERGY_PER_OPERATION.get());
            if (move <= 0) return 0;
            if (!simulate) be.shared().setCell(be.key(), cur - move);
            return move;
        }

        @Override
        public int getEnergyStored() {
            return be.shared().cell(be.key());
        }

        @Override
        public int getMaxEnergyStored() {
            return Config.CELL_CAPACITY.get();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
