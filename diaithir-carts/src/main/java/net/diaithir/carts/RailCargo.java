package net.diaithir.carts;

import com.mojang.serialization.Codec;
import net.diaithir.freight.BayTree;
import net.diaithir.freight.CargoBay;
import net.diaithir.freight.CargoCarrier;
import net.diaithir.freight.Freight;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * Rail wagons as cargo carriers of the freight layer. A freight wagon spawned from the yards carries a bay (cells and rated payload, set when it is built):
 * flat, well and container wagons take a 20 ft container, every other wagon takes pallets, barrels, crates and chests, and narrow-gauge wagons are too
 * small for a container. The loads stay in the wagon across saves, docks serve them through the usual item handler, and the cart draws them (client).
 * Nothing here opens a storage screen.
 */
public final class RailCargo {
    private RailCargo() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirCarts.ID);
    /** "w,h,l,kg": the bay of this wagon; empty for a cart that carries no freight bay. */
    public static final Supplier<AttachmentType<String>> SPEC = ATTACHMENTS.register("cargo_bay", () -> AttachmentType.builder(() -> "").serialize(Codec.STRING.fieldOf("spec")).build());
    /** The loads standing in the wagon. */
    public static final Supplier<AttachmentType<BayTree>> LOADS = ATTACHMENTS.register("cargo_loads", () -> AttachmentType.builder(() -> BayTree.EMPTY).serialize(BayTree.CODEC.fieldOf("loads")).build());
    /** What the client draws: "w,h,l|type,x,y,z,turned;..." (synced to everyone tracking the cart). */
    public static final Supplier<AttachmentType<String>> VIEW = ATTACHMENTS.register("cargo_view", () -> AttachmentType.builder(() -> "").sync(ByteBufCodecs.STRING_UTF8).build());

    /** Width of the cart's hitbox, which the aimed point is mapped across. */
    private static final double SPAN = 0.98;

    private static final Map<AbstractMinecart, Carrier> CARRIERS = Collections.synchronizedMap(new WeakHashMap<>());

    /** Gives a freshly built wagon its bay: cells {w,h,l} and the rated payload in kg. */
    public static void fit(AbstractMinecart cart, int[] cells, int ratedKg) {
        cart.setData(SPEC, cells[0] + "," + cells[1] + "," + cells[2] + "," + Math.max(ratedKg, 1000));
        cart.setData(VIEW, view(new CargoBay(cells[0], cells[1], cells[2], ratedKg)));
    }

    public static void register(RegisterCapabilitiesEvent e) {
        for (EntityType<? extends AbstractMinecart> t : java.util.List.of(EntityType.CHEST_MINECART, EntityType.HOPPER_MINECART, EntityType.MINECART))
            e.registerEntity(Freight.CARRIER, t, (cart, ctx) -> carrier(cart));
    }

    /** The carrier of a wagon, or null when the cart has no bay. */
    public static @Nullable CargoCarrier carrier(AbstractMinecart cart) {
        String spec = cart.getData(SPEC);
        if (spec.isEmpty()) return null;
        synchronized (CARRIERS) {
            Carrier c = CARRIERS.get(cart);
            if (c == null) {
                String[] s = spec.split(",");
                int w = Integer.parseInt(s[0]), h = Integer.parseInt(s[1]), l = Integer.parseInt(s[2]);
                c = new Carrier(cart, cart.getData(LOADS).toBay(w, h, l, Double.parseDouble(s[3])));
                CARRIERS.put(cart, c);
            }
            return c;
        }
    }

    /** The client's view of a bay: its size and each load's id and cell. Containers are drawn whole; their interiors are not sent. */
    public static String view(CargoBay bay) {
        StringBuilder b = new StringBuilder().append(bay.w).append(',').append(bay.h).append(',').append(bay.l).append('|');
        for (CargoBay.Placed p : bay.loads()) b.append(p.type.id()).append(',').append(p.x).append(',').append(p.y).append(',').append(p.z).append(',').append(p.turned ? 1 : 0).append(';');
        return b.toString();
    }

    /** Column {x, z} under a point given in the wagon's own frame (along the rail, across it), each in blocks from the middle; null when off the deck. */
    public static @Nullable int[] column(double along, double across, int w, int l) {
        if (Math.abs(along) > SPAN * 0.6 || Math.abs(across) > SPAN * 0.6) return null;
        int z = (int) Math.floor((along / SPAN + 0.5) * l), x = (int) Math.floor((across / SPAN + 0.5) * w);
        return new int[]{Math.max(0, Math.min(w - 1, x)), Math.max(0, Math.min(l - 1, z))};
    }

    private static final class Carrier implements CargoCarrier {
        private final AbstractMinecart cart;
        private final CargoBay bay;

        Carrier(AbstractMinecart cart, CargoBay bay) {
            this.cart = cart;
            this.bay = bay;
        }

        @Override
        public CargoBay bay() { return bay; }

        @Override
        public @Nullable int[] columnAt(Vec3 world) {
            double yaw = Math.toRadians(cart.getYRot());
            double dx = world.x - cart.getX(), dz = world.z - cart.getZ();
            // the cart's long axis: improved minecarts turn by 180 - atan2(dz, dx), the classic ones by atan2(dz, dx); the renderer draws along the same axis
            boolean improved = cart.getBehavior() instanceof net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
            double ax = Math.cos(yaw), az = improved ? -Math.sin(yaw) : Math.sin(yaw);
            double along = dx * ax + dz * az, across = -dx * az + dz * ax;
            return column(along, across, bay.w, bay.l);
        }

        @Override
        public void changed() {
            cart.setData(LOADS, BayTree.of(bay));
            cart.setData(VIEW, view(bay));
        }
    }
}
