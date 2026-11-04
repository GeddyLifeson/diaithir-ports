package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

/**
 * Loading and unloading a {@link CargoCarrier} with items: a hand, a forklift, a crane or a depot worker calls the same two methods. A load goes on top of the
 * column it is aimed at and stays there; the first column spot that passes every {@link CargoBay} rule is used.
 */
public final class Carriers {
    /** Finders for carriers that are not entities: (level, search box) to a carrier or null. */
    public static final List<BiFunction<Level, AABB, CargoCarrier>> SOURCES = new CopyOnWriteArrayList<>();
    /** The Age used for placement checks; the items themselves are already Age gated. */
    private static final int ANY_AGE = 8;

    private Carriers() {}

    /** The carrier within {@code reach} blocks of {@code from} along {@code dir}: what a dock faces. */
    public static @Nullable CargoCarrier near(Level level, BlockPos from, Direction dir, int reach) {
        AABB box = new AABB(from.relative(dir, 1)).minmax(new AABB(from.relative(dir, reach)));
        for (BiFunction<Level, AABB, CargoCarrier> f : SOURCES) {
            CargoCarrier c = f.apply(level, box);
            if (c != null) return c;
        }
        for (Entity e : level.getEntities((Entity) null, box, x -> true)) {
            CargoCarrier c = e.getCapability(Freight.CARRIER, null);
            if (c != null) return c;
        }
        return null;
    }

    /** Whether this stack is something a carrier can take: a load block item or a container item. */
    public static boolean loadable(ItemStack stack) {
        return stack.getItem() instanceof ContainerItem || stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LoadBlock;
    }

    private static LoadType typeOf(ItemStack stack) {
        if (stack.getItem() instanceof ContainerItem ci) return ci.type;
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LoadBlock lb ? lb.type : null;
    }

    /** Puts one load (one item of the stack) on top of column (x,z). Shrinks the stack by one on OK. */
    public static CargoBay.Result put(CargoCarrier carrier, ItemStack stack, int x, int z) {
        LoadType t = typeOf(stack);
        if (t == null) return CargoBay.Result.UNKNOWN_TYPE;
        CargoBay bay = carrier.bay();
        // a load wider than one cell is set down so it fits: aiming at a column near the edge slides it in
        x = Math.max(0, Math.min(x, bay.w - t.w()));
        z = Math.max(0, Math.min(z, bay.l - t.l()));
        NonNullList<ItemStack> items = NonNullList.withSize(t.itemSlots(), ItemStack.EMPTY);
        CargoBay inner = null;
        double kg;
        if (t.container()) {
            inner = Containers.bayOf(stack);
            kg = inner.payloadKg();
        } else {
            stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
            kg = Loads.kg(items);
        }
        CargoBay.Result last = CargoBay.Result.OUT_OF_BAY;
        for (int y = 0; y < bay.h; y++) {
            last = bay.check(t, false, x, y, z, kg, ANY_AGE);
            if (last != CargoBay.Result.OK) continue;
            CargoBay.Placed p = bay.place(t, false, x, y, z, kg, ANY_AGE) == CargoBay.Result.OK ? bay.at(x, y, z) : null;
            if (p == null) break;
            if (inner != null) copyInto(p.inside, inner); else p.data = items;
            stack.shrink(1);
            carrier.changed();
            return CargoBay.Result.OK;
        }
        return last;
    }

    private static void copyInto(CargoBay to, CargoBay from) {
        for (CargoBay.Placed q : from.loads()) {
            CargoBay.Placed r = to.restore(q.type, q.turned, q.x, q.y, q.z, q.payloadKg);
            if (r != null) r.data = q.data;
        }
    }

    /** Takes the highest free load off column (x,z) as an item (a container keeps its cargo); empty when there is none. */
    public static ItemStack takeTop(CargoCarrier carrier, int x, int z) {
        CargoBay bay = carrier.bay();
        for (int y = bay.h - 1; y >= 0; y--) {
            CargoBay.Placed p = bay.at(x, y, z);
            if (p == null) continue;
            if (!bay.canTake(p)) return ItemStack.EMPTY;
            ItemStack out;
            if (p.type.container()) {
                out = new ItemStack(Freight.CONTAINER_ITEMS.get(p.type.id()).get());
                out.set(Freight.BAY.get(), BayData.of(p.inside));
            } else {
                out = new ItemStack(Freight.LOAD_ITEMS.get(p.type.id()).get());
                @SuppressWarnings("unchecked") List<ItemStack> items = (List<ItemStack>) items(p);
                out.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
            }
            bay.take(p);
            carrier.changed();
            return out;
        }
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("unchecked")
    private static NonNullList<ItemStack> items(CargoBay.Placed p) {
        if (!(p.data instanceof NonNullList<?>)) p.data = NonNullList.withSize(p.type.itemSlots(), ItemStack.EMPTY);
        return (NonNullList<ItemStack>) p.data;
    }

    /** Every loose load's items on the carrier, container interiors included, as one Container for docks. */
    public static Container view(CargoCarrier carrier) {
        List<Container> parts = new ArrayList<>();
        collect(carrier.bay(), carrier::changed, parts);
        return new ContainerView(parts);
    }

    private static void collect(CargoBay bay, Runnable changed, List<Container> parts) {
        for (CargoBay.Placed p : bay.loads()) {
            if (p.type.looseItems()) parts.add(new ListContainer(items(p), changed));
            else if (p.inside != null) collect(p.inside, changed, parts);
        }
    }
}
