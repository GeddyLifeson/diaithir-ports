package net.diaithir.freight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

/** The loads standing in a container, as a data component. Cell positions are the container's own (0..2 wide, 0..2 high, 0..length-1). */
public record BayData(List<Entry> loads) {
    public static final BayData EMPTY = new BayData(List.of());

    public record Entry(String type, int x, int y, int z, boolean turned, ItemContainerContents items, List<Entry> inside) {
        public Entry(String type, int x, int y, int z, boolean turned, ItemContainerContents items) { this(type, x, y, z, turned, items, List.of()); }

        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("type").forGetter(Entry::type),
                Codec.INT.fieldOf("x").forGetter(Entry::x), Codec.INT.fieldOf("y").forGetter(Entry::y), Codec.INT.fieldOf("z").forGetter(Entry::z),
                Codec.BOOL.optionalFieldOf("turned", false).forGetter(Entry::turned),
                ItemContainerContents.CODEC.optionalFieldOf("items", ItemContainerContents.EMPTY).forGetter(Entry::items),
                // a container's own loads; recursive, so a container on a vehicle keeps what is inside it
                Codec.lazyInitialized(() -> Entry.CODEC).listOf().optionalFieldOf("inside", List.of()).forGetter(Entry::inside)).apply(i, Entry::new));
    }

    public static final Codec<BayData> CODEC = Entry.CODEC.listOf().xmap(BayData::new, BayData::loads);

    /** Rebuilds the bay of a container of the given type; a load that no longer fits or is unknown is skipped. */
    public CargoBay toBay(LoadType container) {
        CargoBay bay = new CargoBay(container.inner()[0], container.inner()[1], container.inner()[2], container.payloadKg());
        fill(bay, loads);
        return bay;
    }

    /** Rebuilds a vehicle's bay of the given size: a deck, bed or trunk rather than a container. */
    public CargoBay toBay(int w, int h, int l, double maxPayloadKg) {
        CargoBay bay = new CargoBay(w, h, l, maxPayloadKg);
        fill(bay);
        return bay;
    }

    private void fill(CargoBay bay) { fill(bay, loads); }

    private static void fill(CargoBay bay, List<Entry> entries) {
        for (Entry e : entries) {
            LoadType t = LoadType.byId(e.type);
            if (t == null) continue;
            CargoBay.Placed p = bay.restore(t, e.turned, e.x, e.y, e.z, 0);
            if (p == null) continue;
            if (p.inside != null) { fill(p.inside, e.inside); continue; }
            NonNullList<ItemStack> items = NonNullList.withSize(t.itemSlots(), ItemStack.EMPTY);
            e.items.copyInto(items);
            p.data = items;
            p.payloadKg = Loads.kg(items);
        }
    }

    public static BayData of(CargoBay bay) { return new BayData(entries(bay)); }

    private static List<Entry> entries(CargoBay bay) {
        List<Entry> out = new ArrayList<>();
        for (CargoBay.Placed p : bay.loads()) {
            ItemContainerContents items = p.data instanceof List<?> l ? ItemContainerContents.fromItems(castItems(l)) : ItemContainerContents.EMPTY;
            out.add(new Entry(p.type.id(), p.x, p.y, p.z, p.turned, items, p.inside != null ? entries(p.inside) : List.of()));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<ItemStack> castItems(List<?> l) { return (List<ItemStack>) l; }
}
