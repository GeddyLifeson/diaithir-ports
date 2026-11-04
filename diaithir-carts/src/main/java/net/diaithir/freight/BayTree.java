package net.diaithir.freight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

/**
 * A whole {@link CargoBay} as data, containers and their interiors included, for a carrier that must keep its loads across a save (a wagon, a truck bed).
 * {@link BayData} holds one container's flat interior; this is the recursive form a vehicle bay needs, because a container standing on a flatcar still holds
 * its own pallets.
 */
public record BayTree(List<Node> loads) {
    public static final BayTree EMPTY = new BayTree(List.of());

    public record Node(String type, int x, int y, int z, boolean turned, ItemContainerContents items, List<Node> inside) {}

    private static final Codec<Node> NODE = Codec.recursive("BayTreeNode", self -> RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("type").forGetter(Node::type),
            Codec.INT.fieldOf("x").forGetter(Node::x), Codec.INT.fieldOf("y").forGetter(Node::y), Codec.INT.fieldOf("z").forGetter(Node::z),
            Codec.BOOL.optionalFieldOf("turned", false).forGetter(Node::turned),
            ItemContainerContents.CODEC.optionalFieldOf("items", ItemContainerContents.EMPTY).forGetter(Node::items),
            self.listOf().optionalFieldOf("inside", List.of()).forGetter(Node::inside)).apply(i, Node::new)));

    public static final Codec<BayTree> CODEC = NODE.listOf().xmap(BayTree::new, BayTree::loads);

    public static BayTree of(CargoBay bay) {
        List<Node> out = new ArrayList<>();
        for (CargoBay.Placed p : bay.loads()) {
            ItemContainerContents items = ItemContainerContents.EMPTY;
            if (p.data instanceof List<?> l) {
                @SuppressWarnings("unchecked") List<ItemStack> stacks = (List<ItemStack>) l;
                items = ItemContainerContents.fromItems(stacks);
            }
            out.add(new Node(p.type.id(), p.x, p.y, p.z, p.turned, items, p.inside == null ? List.of() : of(p.inside).loads));
        }
        return new BayTree(out);
    }

    /** Rebuilds a bay of the given size; a load that is unknown or no longer fits is skipped. */
    public CargoBay toBay(int w, int h, int l, double maxPayloadKg) {
        CargoBay bay = new CargoBay(w, h, l, maxPayloadKg);
        fill(bay, loads);
        return bay;
    }

    private static void fill(CargoBay bay, List<Node> nodes) {
        for (Node n : nodes) {
            LoadType t = LoadType.byId(n.type);
            if (t == null) continue;
            CargoBay.Placed p = bay.restore(t, n.turned, n.x, n.y, n.z, 0);
            if (p == null) continue;
            if (p.inside != null) {
                fill(p.inside, n.inside);
            } else {
                NonNullList<ItemStack> items = NonNullList.withSize(t.itemSlots(), ItemStack.EMPTY);
                n.items.copyInto(items);
                p.data = items;
                p.payloadKg = Loads.kg(items);
            }
        }
    }
}
