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

    public record Entry(String type, int x, int y, int z, boolean turned, ItemContainerContents items) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("type").forGetter(Entry::type),
                Codec.INT.fieldOf("x").forGetter(Entry::x), Codec.INT.fieldOf("y").forGetter(Entry::y), Codec.INT.fieldOf("z").forGetter(Entry::z),
                Codec.BOOL.optionalFieldOf("turned", false).forGetter(Entry::turned),
                ItemContainerContents.CODEC.optionalFieldOf("items", ItemContainerContents.EMPTY).forGetter(Entry::items)).apply(i, Entry::new));
    }

    public static final Codec<BayData> CODEC = Entry.CODEC.listOf().xmap(BayData::new, BayData::loads);

    /** Rebuilds the bay of a container of the given type; a load that no longer fits or is unknown is skipped. */
    public CargoBay toBay(LoadType container) {
        CargoBay bay = new CargoBay(container.inner()[0], container.inner()[1], container.inner()[2], container.payloadKg());
        for (Entry e : loads) {
            LoadType t = LoadType.byId(e.type);
            if (t == null) continue;
            CargoBay.Placed p = bay.restore(t, e.turned, e.x, e.y, e.z, 0);
            if (p == null) continue;
            NonNullList<ItemStack> items = NonNullList.withSize(t.itemSlots(), ItemStack.EMPTY);
            e.items.copyInto(items);
            p.data = items;
            p.payloadKg = Loads.kg(items);
        }
        return bay;
    }

    public static BayData of(CargoBay bay) {
        List<Entry> out = new ArrayList<>();
        for (CargoBay.Placed p : bay.loads()) {
            if (p.data instanceof List<?> l) {
                @SuppressWarnings("unchecked") List<ItemStack> items = (List<ItemStack>) l;
                out.add(new Entry(p.type.id(), p.x, p.y, p.z, p.turned, ItemContainerContents.fromItems(items)));
            } else {
                out.add(new Entry(p.type.id(), p.x, p.y, p.z, p.turned, ItemContainerContents.EMPTY));
            }
        }
        return new BayData(out);
    }
}
