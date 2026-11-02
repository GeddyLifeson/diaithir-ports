package net.diaithir.routing.foundry;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** One kind of item, up to a configured number of stacks. Transaction safe. */
public final class BarrelStore {
    private record Snap(ItemResource resource, int count) {}

    private ItemResource resource = ItemResource.EMPTY;
    private int count;
    private final Runnable changed;
    private final SnapshotJournal<Snap> journal = new SnapshotJournal<>() {
        @Override protected Snap createSnapshot() { return new Snap(resource, count); }
        @Override protected void revertToSnapshot(Snap s) { resource = s.resource; count = s.count; }
        @Override protected void onRootCommit(Snap original) { changed.run(); }
    };

    public BarrelStore(Runnable changed) {
        this.changed = changed;
    }

    public ItemResource resource() { return resource; }
    public int count() { return count; }

    public static long capacity(ItemResource r) {
        return (long) Config.BARREL_STACKS.get() * Math.max(1, r.getMaxStackSize());
    }

    public void save(ValueOutput out) {
        out.store("res", ItemResource.OPTIONAL_CODEC, resource);
        out.store("count", Codec.INT, count);
    }

    public void load(ValueInput in) {
        resource = in.read("res", ItemResource.OPTIONAL_CODEC).orElse(ItemResource.EMPTY);
        count = resource.isEmpty() ? 0 : in.read("count", Codec.INT).orElse(0);
    }

    /** A view of the barrel. Insert is allowed from the top and sides, extraction from the bottom only (the original rule). */
    public ResourceHandler<ItemResource> view(boolean canInsert, boolean canExtract) {
        return new ResourceHandler<>() {
            @Override public int size() { return 1; }
            @Override public ItemResource getResource(int index) { return resource; }
            @Override public long getAmountAsLong(int index) { return count; }
            @Override public long getCapacityAsLong(int index, ItemResource r) { return r.isEmpty() ? 0 : (resource.isEmpty() || resource.equals(r)) ? capacity(r) : 0; }
            @Override public boolean isValid(int index, ItemResource r) { return canInsert && (resource.isEmpty() || resource.equals(r)); }

            @Override
            public int insert(int index, ItemResource r, int amount, TransactionContext tx) {
                if (!canInsert || index != 0 || amount <= 0 || !isValid(0, r)) return 0;
                int n = (int) Math.min(amount, capacity(r) - count);
                if (n <= 0) return 0;
                journal.updateSnapshots(tx);
                resource = r;
                count += n;
                return n;
            }

            @Override
            public int extract(int index, ItemResource r, int amount, TransactionContext tx) {
                if (!canExtract || index != 0 || amount <= 0 || resource.isEmpty() || !resource.equals(r)) return 0;
                int n = Math.min(amount, count);
                journal.updateSnapshots(tx);
                count -= n;
                if (count == 0) resource = ItemResource.EMPTY;
                return n;
            }
        };
    }
}
