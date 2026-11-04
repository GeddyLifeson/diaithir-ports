package net.diaithir.chunkloaders;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The optional cart tier: a Cartseal on a minecart keeps the chunks around the cart loaded, following it, until the seal's minutes run out. */
final class Carts {
    static final String LEFT = "diaithirchunkloaders_cart_ticks", OWNER = "diaithirchunkloaders_cart_owner";
    private static final int PERIOD = 10;

    /** A cart that is keeping chunks right now. */
    static final class Active {
        final String dim;
        final String owner;
        ChunkPos at;
        Active(String dim, String owner) { this.dim = dim; this.owner = owner; }
    }

    static final Map<UUID, Active> ACTIVE = new HashMap<>();

    private Carts() {}

    static void onInteract(PlayerInteractEvent.EntityInteract e) {
        if (e.getHand() != InteractionHand.MAIN_HAND || !(e.getTarget() instanceof AbstractMinecart cart) || !e.getItemStack().is(Registry.CART_SEAL.get())
                || !(e.getEntity() instanceof ServerPlayer player)) return;
        e.setCanceled(true);
        if (!Config.CARTS_ENABLED.get()) {
            player.sendOverlayMessage(Component.literal("Cartseals are switched off on this server."));
            return;
        }
        CompoundTag tag = cart.getPersistentData();
        boolean fresh = tag.getLongOr(LEFT, 0) <= 0;
        if (fresh && countOwned(player.getStringUUID()) >= Config.CART_MAX_PER_PLAYER.get()
                && !(Config.OPS_BYPASS_CAP.get() && player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))) {
            player.sendOverlayMessage(Component.literal("You already have " + Config.CART_MAX_PER_PLAYER.get() + " sealed carts."));
            return;
        }
        long add = Config.CART_MINUTES_PER_SEAL.get() * 1200L, max = Config.CART_MAX_MINUTES.get() * 1200L;
        long left = Math.min(max, tag.getLongOr(LEFT, 0) + add);
        if (left == tag.getLongOr(LEFT, 0)) {
            player.sendOverlayMessage(Component.literal("This cart already holds the most time."));
            return;
        }
        if (fresh) tag.putString(OWNER, player.getStringUUID());
        tag.putLong(LEFT, left);
        if (!player.hasInfiniteMaterials()) e.getItemStack().shrink(1);
        register((ServerLevel) cart.level(), cart);
        player.sendOverlayMessage(Component.literal("Cart keeps " + side() + "x" + side() + " chunks loaded, " + LoaderBlock.duration(left / 20) + " left."));
    }

    static int side() { return 2 * Config.CART_RADIUS.get() + 1; }

    static int countOwned(String owner) { return (int) ACTIVE.values().stream().filter(a -> a.owner.equals(owner)).count(); }

    static void register(ServerLevel level, AbstractMinecart cart) {
        ACTIVE.putIfAbsent(cart.getUUID(), new Active(level.dimension().identifier().toString(), cart.getPersistentData().getStringOr(OWNER, "")));
    }

    static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel() instanceof ServerLevel level && e.getEntity() instanceof AbstractMinecart cart && cart.getPersistentData().getLongOr(LEFT, 0) > 0 && Config.CARTS_ENABLED.get()) {
            register(level, cart);
        }
    }

    static void onLeave(EntityLeaveLevelEvent e) {
        if (e.getLevel() instanceof ServerLevel level && ACTIVE.containsKey(e.getEntity().getUUID())) release(level, e.getEntity().getUUID(), ACTIVE.remove(e.getEntity().getUUID()));
    }

    static void onTick(ServerTickEvent.Post e) {
        if (!ACTIVE.isEmpty() && e.getServer().getTickCount() % PERIOD == 0) step(e.getServer());
    }

    /** One bookkeeping pass: burn PERIOD ticks off each cart's seal, move its chunk square, release it when the time is out. */
    static void step(MinecraftServer server) {
        for (Iterator<Map.Entry<UUID, Active>> it = ACTIVE.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Active> en = it.next();
            Active a = en.getValue();
            ServerLevel level = levelOf(server, a.dim);
            Entity cart = level == null ? null : level.getEntity(en.getKey());
            if (level == null || cart == null || cart.isRemoved() || !Config.CARTS_ENABLED.get()) {
                if (level != null) release(level, en.getKey(), a);
                it.remove();
                continue;
            }
            CompoundTag tag = cart.getPersistentData();
            long left = tag.getLongOr(LEFT, 0) - PERIOD;
            if (left <= 0) {
                tag.putLong(LEFT, 0);
                release(level, en.getKey(), a);
                it.remove();
                continue;
            }
            tag.putLong(LEFT, left);
            ChunkPos now = ChunkPos.containing(cart.blockPosition());
            if (!now.equals(a.at)) {
                hold(level, en.getKey(), now, a);
            }
        }
    }

    /** Moves the held square to a new centre: add the new chunks first, then drop the ones no longer in range. */
    static void hold(ServerLevel level, UUID id, ChunkPos now, Active a) {
        int r = Config.CART_RADIUS.get();
        boolean ticking = Config.ENTITY_TICKING.get();
        List<ChunkPos> want = Tickets.around(now, r);
        for (ChunkPos p : want) Tickets.CARTS.forceChunk(level, id, p.x(), p.z(), true, ticking);
        if (a.at != null) {
            for (ChunkPos p : Tickets.around(a.at, r)) if (!want.contains(p)) Tickets.CARTS.forceChunk(level, id, p.x(), p.z(), false, ticking);
        }
        a.at = now;
    }

    static void release(ServerLevel level, UUID id, Active a) {
        if (a.at == null) return;
        for (ChunkPos p : Tickets.around(a.at, Config.CART_RADIUS.get())) {
            Tickets.CARTS.forceChunk(level, id, p.x(), p.z(), false, true);
            Tickets.CARTS.forceChunk(level, id, p.x(), p.z(), false, false);
        }
        a.at = null;
    }

    static ServerLevel levelOf(MinecraftServer server, String dim) {
        for (ServerLevel l : server.getAllLevels()) if (l.dimension().identifier().toString().equals(dim)) return l;
        return null;
    }
}
