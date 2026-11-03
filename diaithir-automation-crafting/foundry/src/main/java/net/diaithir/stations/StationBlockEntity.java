package net.diaithir.stations;

import net.diaithir.crafting.foundry.Operators;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * A station holds up to nine stacks. Use it with items to load it, empty-handed to start the first recipe its contents satisfy,
 * sneak empty-handed to take the contents back. The job runs for the recipe's seconds, then the result drops on top of the block.
 */
public final class StationBlockEntity extends BlockEntity {
    public static final int SLOTS = 9;
    public final NonNullList<ItemStack> inv = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private ItemStack pending = ItemStack.EMPTY;
    private int left;
    /** Last finished result, kept for tests and the overlay. */
    public ItemStack lastOutput = ItemStack.EMPTY;

    public StationBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirStations.STATION_BE.get(), pos, state);
    }

    String stationKey() { return ((StationBlock) getBlockState().getBlock()).station(); }

    public boolean busy() { return left > 0; }

    /** Puts as much of the stack as fits into the slots. Returns how many items went in. */
    public int load(ItemStack s) {
        int moved = 0;
        for (int i = 0; i < SLOTS && !s.isEmpty(); i++) {
            ItemStack t = inv.get(i);
            if (t.isEmpty()) {
                inv.set(i, s.copyWithCount(s.getCount()));
                moved += s.getCount();
                s.setCount(0);
            } else if (ItemStack.isSameItemSameComponents(t, s) && t.getCount() < t.getMaxStackSize()) {
                int n = Math.min(s.getCount(), t.getMaxStackSize() - t.getCount());
                t.grow(n);
                s.shrink(n);
                moved += n;
            }
        }
        if (moved > 0) setChanged();
        return moved;
    }

    /** Starts the first satisfied recipe. Returns its name for the overlay, or null when none starts. */
    public @Nullable String start() {
        if (level == null || level.isClientSide() || busy()) return null;
        for (StationRecipe r : StationRecipes.forStation(stationKey())) {
            if (!r.satisfiedBy(inv)) continue;
            if (r.operator() && !Operators.work(level, worldPosition, Math.max(1, r.timeS()))) continue;
            r.consume(inv);
            pending = r.result().copy();
            left = Math.max(1, r.timeS() * 20);
            setChanged();
            return pending.getHoverName().getString();
        }
        return null;
    }

    public void tick() {
        if (left <= 0 || level == null || level.isClientSide()) return;
        if (--left == 0) {
            lastOutput = pending;
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, pending.copy());
            pending = ItemStack.EMPTY;
        }
        setChanged();
    }

    public void takeAll(Player p) {
        for (int i = 0; i < SLOTS; i++) {
            if (!inv.get(i).isEmpty()) p.getInventory().placeItemBackInInventory(inv.get(i));
            inv.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    public Component describe() {
        int n = 0;
        for (ItemStack s : inv) n += s.getCount();
        return Component.literal(busy() ? "Working: " + (left + 19) / 20 + " s left" : n + " items loaded");
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            for (ItemStack s : inv) if (!s.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), s);
            if (!pending.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), pending);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ValueOutput.TypedOutputList<ItemStack> list = out.list("inv", ItemStack.OPTIONAL_CODEC);
        for (ItemStack s : inv) list.add(s);
        out.store("pending", ItemStack.OPTIONAL_CODEC, pending);
        out.putInt("left", left);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        inv.clear();
        int i = 0;
        for (ItemStack s : in.listOrEmpty("inv", ItemStack.OPTIONAL_CODEC)) if (i < SLOTS) inv.set(i++, s);
        pending = in.read("pending", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        left = in.getIntOr("left", 0);
    }
}
