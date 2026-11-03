package com.ssaml.advancedoredeposits.miner;

import com.ssaml.advancedoredeposits.block.DepositBlockEntity;
import com.ssaml.advancedoredeposits.registry.ModBlockEntities;
import com.ssaml.advancedoredeposits.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Mines the deposit tiles under it (range and depth like FactoriOres, MIT): picks the tile with the most
 * cycles left, so a patch drains from the middle outward. Slot 0 is the output, slot 1 the fuel (burner).
 */
public class MinerBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int MAX_DEPTH = 10;
    public static final int ENERGY_PER_TICK = 20;
    private static final int[] OUT = {0};
    private static final int[] ANY = {0, 1};

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    /** Accepts power from outside (generators, cables); only the miner itself can draw it down. */
    public final MinerEnergy energy = new MinerEnergy();

    public final class MinerEnergy extends SimpleEnergyHandler {
        MinerEnergy() {
            super(ENERGY_PER_TICK * 400, 1000, 0);
        }

        @Override
        protected void onEnergyChanged(int previous) {
            setChanged();
        }

        boolean drain(int amount) {
            if (energy < amount) {
                return false;
            }
            int before = energy;
            energy -= amount;
            onEnergyChanged(before);
            return true;
        }
    }
    private int progress;
    private int burnTime;

    public MinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MINER.get(), pos, state);
    }

    private MinerKind kind() {
        return ((MinerBlock) getBlockState().getBlock()).kind();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MinerBlockEntity miner) {
        boolean running = miner.tick(level);
        if (state.getValue(MinerBlock.ENABLED) != running) {
            level.setBlock(pos, state.setValue(MinerBlock.ENABLED, running), 3);
        }
    }

    private boolean tick(Level level) {
        MinerKind kind = kind();
        if (burnTime > 0) {
            burnTime--;
        }
        ItemStack out = items.get(0);
        if (!out.isEmpty() && out.getCount() >= out.getMaxStackSize() || level.hasNeighborSignal(worldPosition)) {
            return false;
        }
        if (pickTarget(level, kind) == null) {
            progress = 0;
            return false;
        }
        if (!powered(kind)) {
            progress = Math.max(0, progress - 2);
            return false;
        }
        if (++progress >= kind.cycleTicks) {
            progress = 0;
            mine(level, kind);
        }
        setChanged();
        return true;
    }

    private boolean powered(MinerKind kind) {
        if (kind == MinerKind.ELECTRIC) {
            return energy.drain(ENERGY_PER_TICK);
        }
        if (burnTime <= 0) {
            ItemStack fuel = items.get(1);
            int ticks = fuel.isEmpty() ? 0 : level.fuelValues().burnDuration(fuel);
            if (ticks <= 0) {
                return false;
            }
            burnTime = ticks;
            ItemStack remainder = fuel.getCraftingRemainder() == null ? ItemStack.EMPTY : fuel.getCraftingRemainder().create();
            fuel.shrink(1);
            if (fuel.isEmpty()) {
                items.set(1, remainder);
            }
        }
        return true;
    }

    private @Nullable DepositBlockEntity pickTarget(Level level, MinerKind kind) {
        DepositBlockEntity best = null;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy <= MAX_DEPTH; dy++) {
            for (int dx = -kind.range; dx <= kind.range; dx++) {
                for (int dz = -kind.range; dz <= kind.range; dz++) {
                    cursor.set(worldPosition.getX() + dx, worldPosition.getY() - dy, worldPosition.getZ() + dz);
                    if (level.getBlockEntity(cursor) instanceof DepositBlockEntity d && d.remainingBlocks() > 0
                            && canStack(d)
                            && (best == null || d.remainingBlocks() > best.remainingBlocks())) {
                        best = d;
                    }
                }
            }
        }
        return best;
    }

    private boolean canStack(DepositBlockEntity d) {
        ItemStack out = items.get(0);
        return out.isEmpty() || out.is(ModItems.rawChunk(d.oreType()));
    }

    private void mine(Level level, MinerKind kind) {
        DepositBlockEntity target = pickTarget(level, kind);
        if (target == null) {
            return;
        }
        ItemStack chunk = new ItemStack(ModItems.rawChunk(target.oreType()));
        if (items.get(0).isEmpty()) {
            items.set(0, chunk);
        } else {
            items.get(0).grow(1);
        }
        if (!target.consumeMineCycle()) {
            level.removeBlock(target.getBlockPos(), false);
        }
    }

    public ItemStack takeOutput() {
        ItemStack out = items.get(0);
        items.set(0, ItemStack.EMPTY);
        setChanged();
        return out;
    }

    public boolean insertFuel(Player player, ItemStack held) {
        if (level == null || held.isEmpty() || level.fuelValues().burnDuration(held) <= 0) {
            return false;
        }
        ItemStack slot = items.get(1);
        if (slot.isEmpty()) {
            items.set(1, held.copyWithCount(held.getCount()));
            held.setCount(0);
        } else if (ItemStack.isSameItemSameComponents(slot, held)) {
            int moved = Math.min(held.getCount(), slot.getMaxStackSize() - slot.getCount());
            slot.grow(moved);
            held.shrink(moved);
        } else {
            return false;
        }
        setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, items);
        out.putInt("Progress", progress);
        out.putInt("BurnTime", burnTime);
        energy.serialize(out.child("Energy"));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items.clear();
        ContainerHelper.loadAllItems(in, items);
        progress = in.getIntOr("Progress", 0);
        burnTime = in.getIntOr("BurnTime", 0);
        in.child("Energy").ifPresent(energy::deserialize);
    }

    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() { return items.get(0).isEmpty() && items.get(1).isEmpty(); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack r = ContainerHelper.removeItem(items, slot, amount);
        if (!r.isEmpty()) setChanged();
        return r;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); setChanged(); }
    @Override public boolean stillValid(Player player) { return Container_stillValid(player); }
    @Override public void clearContent() { items.clear(); }
    @Override public int[] getSlotsForFace(Direction side) { return kind() == MinerKind.BURNER ? ANY : OUT; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot == 1 && kind() == MinerKind.BURNER && level != null && level.fuelValues().burnDuration(stack) > 0;
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return slot == 0; }

    private boolean Container_stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64;
    }
}
