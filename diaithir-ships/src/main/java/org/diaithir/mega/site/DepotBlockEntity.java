package org.diaithir.mega.site;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Converts delivered items to tonnes per material tag. An item counts for the first material tag it carries: 1 t per item (a pallet),
 * or 20 t when it also carries c:container/bulk (a bulk container). The pallet and container items come from the containers module;
 * until its data component lands this is the stand-in rule, and the numbers match plans/MEGASTRUCTURES.md.
 */
public class DepotBlockEntity extends BlockEntity {
    public static final double PALLET_T = 1.0, CONTAINER_T = 20.0;
    private static final TagKey<Item> BULK = TagKey.create(Registries.ITEM, Identifier.parse("c:container/bulk"));

    public final Ledger ledger = new Ledger();
    public final Intake intake = new Intake();

    public DepotBlockEntity(BlockPos pos, BlockState state) { super(org.diaithir.mega.MegaMod.DEPOT_BE.get(), pos, state); }

    /** Tonnes one item of this resource is worth for tag, or 0 when it is not that material. */
    static double tonnesPer(Item item, String tag) {
        if (!item.builtInRegistryHolder().is(TagKey.create(Registries.ITEM, Identifier.parse(tag)))) return 0;
        return item.builtInRegistryHolder().is(BULK) ? CONTAINER_T : PALLET_T;
    }

    /** Insert-only item handler; rolls back with the transaction. */
    public final class Intake extends SnapshotJournal<Map<String, Double>> implements ResourceHandler<ItemResource> {
        @Override protected Map<String, Double> createSnapshot() { return new HashMap<>(ledger.view()); }
        @Override protected void revertToSnapshot(Map<String, Double> s) { ledger.set(s); }
        @Override protected void onRootCommit(Map<String, Double> original) { setChanged(); }

        @Override public int size() { return 1; }
        @Override public ItemResource getResource(int index) { return ItemResource.EMPTY; }
        @Override public long getAmountAsLong(int index) { return 0; }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return Integer.MAX_VALUE; }
        @Override public boolean isValid(int index, ItemResource resource) { return index == 0 && material(resource) != null; }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext tx) { return 0; }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
            String tag = material(resource);
            if (index != 0 || amount <= 0 || tag == null) return 0;
            updateSnapshots(tx);
            ledger.add(tag, amount * tonnesPer(resource.getItem(), tag));
            return amount;
        }

        private String material(ItemResource r) {
            for (String id : Blueprints.ids()) for (var st : Blueprints.get(id).stages()) for (String tag : st.materials().keySet())
                if (tonnesPer(r.getItem(), tag) > 0) return tag;
            return null;
        }
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        Map<String, Double> m = new HashMap<>();
        in.child("ledger").ifPresent(c -> { for (String tag : Blueprints.allTags()) m.put(tag, c.getDoubleOr(tag, 0)); });
        ledger.set(m);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ValueOutput c = out.child("ledger");
        for (var e : ledger.view().entrySet()) c.putDouble(e.getKey(), e.getValue());
    }
}
