package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Trains. A coupled cart has a leader; a cart has at most one leader and one follower. The follower is set moving toward its leader every tick
 * at the leader's speed corrected by how far the gap is from the spacing, so it trails at the spacing and stops with it. The data lives on the carts as attachments.
 */
public final class Coupling {
    private Coupling() {}

    public static final double SPACING = 1.5;
    public static final double MAX_LINK = 6.0;
    public static final double BREAK_DISTANCE = 8.0;
    private static final UUID NIL = new UUID(0, 0);

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirCarts.ID);
    private static final MapCodec<UUID> ID_CODEC = UUIDUtil.CODEC.fieldOf("id");
    public static final Supplier<AttachmentType<UUID>> LEADER = ATTACHMENTS.register("leader", () -> AttachmentType.builder(() -> NIL).serialize(ID_CODEC).build());
    public static final Supplier<AttachmentType<UUID>> FOLLOWER = ATTACHMENTS.register("follower", () -> AttachmentType.builder(() -> NIL).serialize(ID_CODEC).build());

    private record Pending(UUID cart, long time) {}

    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final Map<UUID, Integer> MISSING = new HashMap<>();

    public static boolean hasLeader(AbstractMinecart c) {
        return c.hasData(LEADER);
    }

    public static boolean hasFollower(AbstractMinecart c) {
        return c.hasData(FOLLOWER);
    }

    private static AbstractMinecart find(ServerLevel level, UUID id) {
        return level.getEntity(id) instanceof AbstractMinecart c && !c.isRemoved() ? c : null;
    }

    /** True if making {@code follower} follow {@code leader} is allowed. */
    public static boolean canLink(ServerLevel level, AbstractMinecart leader, AbstractMinecart follower) {
        if (leader == follower || hasFollower(leader) || hasLeader(follower)) return false;
        if (leader.distanceTo(follower) > MAX_LINK) return false;
        AbstractMinecart walk = leader;
        for (int i = 0; i < 256 && walk != null && hasLeader(walk); i++) {
            walk = find(level, walk.getData(LEADER));
            if (walk == follower) return false; // would close a loop
        }
        return true;
    }

    public static boolean link(ServerLevel level, AbstractMinecart leader, AbstractMinecart follower) {
        if (!canLink(level, leader, follower)) return false;
        leader.setData(FOLLOWER, follower.getUUID());
        follower.setData(LEADER, leader.getUUID());
        return true;
    }

    private static void dropChain(ServerLevel level, Vec3 at) {
        ItemEntity e = new ItemEntity(level, at.x, at.y + 0.3, at.z, new ItemStack(DiaithirCarts.COUPLING_CHAIN.get()));
        level.addFreshEntity(e);
    }

    /** Breaks the link between {@code follower} and its leader and drops the chain. */
    public static void unlink(ServerLevel level, AbstractMinecart follower) {
        if (!hasLeader(follower)) return;
        AbstractMinecart leader = find(level, follower.getData(LEADER));
        follower.removeData(LEADER);
        MISSING.remove(follower.getUUID());
        if (leader != null && leader.hasData(FOLLOWER) && leader.getData(FOLLOWER).equals(follower.getUUID())) leader.removeData(FOLLOWER);
        dropChain(level, follower.position());
    }

    /** The chain was used on a cart: the first use remembers it, the second links the two (first is the leader). Returns a message for the player. */
    public static String useChain(ServerLevel level, Player player, ItemStack stack, AbstractMinecart cart) {
        Pending p = PENDING.get(player.getUUID());
        long now = level.getGameTime();
        if (p == null || now - p.time > 600 || find(level, p.cart) == null) {
            PENDING.put(player.getUUID(), new Pending(cart.getUUID(), now));
            return "Chain fixed to this cart. Use it on the cart that should follow.";
        }
        if (p.cart.equals(cart.getUUID())) {
            PENDING.remove(player.getUUID());
            return "Chain taken off.";
        }
        AbstractMinecart leader = find(level, p.cart);
        PENDING.remove(player.getUUID());
        if (!link(level, leader, cart)) return "Cannot couple those carts (too far, already coupled, or a loop).";
        if (!player.hasInfiniteMaterials()) stack.shrink(1);
        return "Carts coupled.";
    }

    /** Cutters used on a cart: cuts its link to its leader, else to its follower. Returns true if a link was cut. */
    public static boolean cut(ServerLevel level, AbstractMinecart cart) {
        if (hasLeader(cart)) {
            unlink(level, cart);
            return true;
        }
        if (hasFollower(cart)) {
            AbstractMinecart f = find(level, cart.getData(FOLLOWER));
            if (f != null) unlink(level, f);
            else {
                cart.removeData(FOLLOWER);
                dropChain(level, cart.position());
            }
            return true;
        }
        return false;
    }

    /** A cart that is destroyed frees both its links. */
    public static void onRemoved(ServerLevel level, AbstractMinecart cart) {
        MISSING.remove(cart.getUUID());
        CartLogic.forget(cart.getUUID());
        if (hasFollower(cart)) {
            AbstractMinecart f = find(level, cart.getData(FOLLOWER));
            if (f != null) {
                f.removeData(LEADER);
                dropChain(level, cart.position());
            }
        }
        if (hasLeader(cart)) {
            AbstractMinecart l = find(level, cart.getData(LEADER));
            if (l != null) {
                l.removeData(FOLLOWER);
                dropChain(level, cart.position());
            }
        }
    }

    /** Called every tick for each cart: if it has a leader, set it moving toward it. */
    static void pull(AbstractMinecart cart, ServerLevel level) {
        if (!cart.hasData(LEADER)) return;
        UUID lid = cart.getData(LEADER);
        Entity e = level.getEntity(lid);
        if (!(e instanceof AbstractMinecart leader) || leader.isRemoved()) {
            int n = MISSING.merge(cart.getUUID(), 1, Integer::sum);
            if (n > 200 || (e != null && e.isRemoved())) {
                cart.removeData(LEADER);
                MISSING.remove(cart.getUUID());
                dropChain(level, cart.position());
            }
            return;
        }
        MISSING.remove(cart.getUUID());
        double dx = leader.getX() - cart.getX();
        double dz = leader.getZ() - cart.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > BREAK_DISTANCE) {
            unlink(level, cart);
            return;
        }
        Vec3 d = cart.getDeltaMovement();
        if (dist < 0.2) {
            cart.setDeltaMovement(0, d.y, 0);
            return;
        }
        double lsp = leader.getDeltaMovement().horizontalDistance();
        double sp = Math.max(0, lsp + (dist - SPACING) * 0.5);
        cart.setDeltaMovement(dx / dist * sp, d.y, dz / dist * sp);
    }
}
