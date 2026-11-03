package net.diaithir.crafting.foundry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** One kind of food, insert only from outside, drained by the seat as the worker eats. Transaction safe. */
public final class FoodStore {
    private record Snap(ItemResource resource, int count) {}

    private ItemResource resource = ItemResource.EMPTY;
    private int count;
    private final Runnable changed;
    private final SnapshotJournal<Snap> journal = new SnapshotJournal<>() {
        @Override protected Snap createSnapshot() { return new Snap(resource, count); }
        @Override protected void revertToSnapshot(Snap s) { resource = s.resource; count = s.count; }
        @Override protected void onRootCommit(Snap original) { changed.run(); }
    };

    public FoodStore(Runnable changed) {
        this.changed = changed;
    }

    public ItemResource resource() { return resource; }
    public int count() { return count; }

    /** Nutrition of one item, 0 when the item is not food. */
    public static int nutrition(ItemResource r) {
        if (r.isEmpty()) return 0;
        FoodProperties f = r.toStack().get(DataComponents.FOOD);
        return f == null ? 0 : Math.max(0, f.nutrition());
    }

    /** Takes one item for the worker. Returns its nutrition, 0 when the store is empty. Not transaction aware: only the seat calls it. */
    int eatOne() {
        if (count <= 0) return 0;
        int n = nutrition(resource);
        count--;
        if (count == 0) resource = ItemResource.EMPTY;
        changed.run();
        return n;
    }

    public void save(ValueOutput out) {
        out.store("res", ItemResource.OPTIONAL_CODEC, resource);
        out.store("count", Codec.INT, count);
    }

    public void load(ValueInput in) {
        resource = in.read("res", ItemResource.OPTIONAL_CODEC).orElse(ItemResource.EMPTY);
        count = resource.isEmpty() ? 0 : in.read("count", Codec.INT).orElse(0);
    }

    /** Pipes and inserters see an insert-only handler that accepts food of the kind already inside. */
    public ResourceHandler<ItemResource> view() {
        return new ResourceHandler<>() {
            @Override public int size() { return 1; }
            @Override public ItemResource getResource(int index) { return resource; }
            @Override public long getAmountAsLong(int index) { return count; }
            @Override public long getCapacityAsLong(int index, ItemResource r) { return accepts(r) ? Config.FOOD_ITEMS.get() : 0; }
            @Override public boolean isValid(int index, ItemResource r) { return accepts(r); }

            private boolean accepts(ItemResource r) {
                return nutrition(r) > 0 && (resource.isEmpty() || resource.equals(r));
            }

            @Override
            public int insert(int index, ItemResource r, int amount, TransactionContext tx) {
                if (index != 0 || amount <= 0 || !accepts(r)) return 0;
                int n = Math.min(amount, Config.FOOD_ITEMS.get() - count);
                if (n <= 0) return 0;
                journal.updateSnapshots(tx);
                resource = r;
                count += n;
                return n;
            }

            @Override public int extract(int index, ItemResource r, int amount, TransactionContext tx) { return 0; }
        };
    }
}
