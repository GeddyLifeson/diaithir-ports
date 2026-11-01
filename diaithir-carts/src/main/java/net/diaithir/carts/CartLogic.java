package net.diaithir.carts;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-cart rules run before the cart's own tick: the speed cap of our rails, the boost and stop of our powered rails, and the station hold. */
public final class CartLogic {
    private CartLogic() {}

    /** A cart waiting (or being let through) at a station. */
    private static final class Hold {
        final BlockPos at;
        final Vec3 dir;
        int waited;
        boolean passing;

        Hold(BlockPos at, Vec3 dir) {
            this.at = at;
            this.dir = dir;
        }
    }

    private static final Map<UUID, Hold> HOLDS = new HashMap<>();

    /** The cap for a cart: vanilla's, or the rail's if it is one of ours and faster. Called from the mixin. */
    public static double cap(AbstractMinecart cart, double vanilla) {
        BlockState state = cart.level().getBlockState(cart.getCurrentBlockPosOrRailBelow());
        if (state.getBlock() instanceof RailSpeed r) return Math.max(vanilla, r.maxSpeed() * (cart.isInWater() ? 0.5 : 1.0));
        return vanilla;
    }

    public static void forget(UUID cart) {
        HOLDS.remove(cart);
    }

    public static int heldCount() {
        return HOLDS.size();
    }

    /** True while a cart is stopped at a station and waiting. */
    public static boolean isHeld(AbstractMinecart cart) {
        Hold h = HOLDS.get(cart.getUUID());
        return h != null && !h.passing;
    }

    public static void tick(AbstractMinecart cart, ServerLevel level) {
        Coupling.pull(cart, level);
        BlockPos pos = cart.getCurrentBlockPosOrRailBelow();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof StationRail) {
            station(cart, level, pos, state);
            return;
        }
        if (!HOLDS.isEmpty()) HOLDS.remove(cart.getUUID());
        if (state.getBlock() instanceof TierPoweredRail rail) {
            if (state.getValue(PoweredRailBlock.POWERED)) boost(cart, level, pos, state, rail.tier().boost);
            else halt(cart);
        }
    }

    private static void halt(AbstractMinecart cart) {
        Vec3 d = cart.getDeltaMovement();
        if (d.horizontalDistance() < 0.03) cart.setDeltaMovement(0, d.y, 0);
        else cart.setDeltaMovement(d.x * 0.5, d.y, d.z * 0.5);
    }

    private static void boost(AbstractMinecart cart, ServerLevel level, BlockPos pos, BlockState state, double amount) {
        Vec3 d = cart.getDeltaMovement();
        double sp = d.horizontalDistance();
        if (sp > 0.01) {
            cart.setDeltaMovement(d.x + d.x / sp * amount, d.y, d.z + d.z / sp * amount);
            return;
        }
        // standing still: push away from a solid block at one end of the rail, like a vanilla powered rail
        RailShape shape = state.getValue(PoweredRailBlock.SHAPE);
        double dx = d.x, dz = d.z;
        if (shape == RailShape.EAST_WEST || shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST) {
            if (solid(level, pos.west())) dx = 0.02;
            else if (solid(level, pos.east())) dx = -0.02;
        } else {
            if (solid(level, pos.north())) dz = 0.02;
            else if (solid(level, pos.south())) dz = -0.02;
        }
        cart.setDeltaMovement(dx, d.y, dz);
    }

    private static boolean solid(ServerLevel level, BlockPos p) {
        return level.getBlockState(p).isRedstoneConductor(level, p);
    }

    private static void station(AbstractMinecart cart, ServerLevel level, BlockPos pos, BlockState state) {
        if (Coupling.hasLeader(cart)) return; // a coupled follower goes where its leader goes
        boolean powered = state.getValue(PoweredRailBlock.POWERED);
        Hold h = HOLDS.get(cart.getUUID());
        if (h != null && !h.at.equals(pos)) {
            HOLDS.remove(cart.getUUID());
            h = null;
        }
        Vec3 d = cart.getDeltaMovement();
        double sp = d.horizontalDistance();
        if (powered) {
            if (h != null) {
                if (sp < 0.03) cart.setDeltaMovement(h.dir.x * 0.2, d.y, h.dir.z * 0.2);
                HOLDS.remove(cart.getUUID());
            }
            boost(cart, level, pos, state, Tier.STEEL.boost);
            return;
        }
        if (h == null) {
            Vec3 dir = sp > 0.01 ? new Vec3(d.x / sp, 0, d.z / sp) : axis(state);
            h = new Hold(pos, dir);
            HOLDS.put(cart.getUUID(), h);
        }
        if (h.passing) return;
        if (sp < 0.03) {
            cart.setDeltaMovement(0, d.y, 0);
            h.waited++;
            int seconds = StationRail.SECONDS[state.getValue(StationRail.DELAY)];
            if (seconds > 0 && h.waited >= seconds * 20) {
                h.passing = true;
                cart.setDeltaMovement(h.dir.x * 0.3, d.y, h.dir.z * 0.3);
            }
        } else {
            cart.setDeltaMovement(d.x * 0.5, d.y, d.z * 0.5);
        }
    }

    private static Vec3 axis(BlockState state) {
        RailShape s = state.getValue(PoweredRailBlock.SHAPE);
        return (s == RailShape.EAST_WEST || s == RailShape.ASCENDING_EAST || s == RailShape.ASCENDING_WEST) ? new Vec3(1, 0, 0) : new Vec3(0, 0, 1);
    }
}
