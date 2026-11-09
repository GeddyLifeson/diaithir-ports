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
        boolean wasPowered;

        Hold(BlockPos at, Vec3 dir) {
            this.at = at;
            this.dir = dir;
        }
    }

    private static final Map<UUID, Hold> HOLDS = new HashMap<>();

    /** The cap for a cart: vanilla's, or the rail's if it is one of ours and faster. Called from the mixin. */
    public static double cap(AbstractMinecart cart, double vanilla) {
        BlockState state = cart.level().getBlockState(cart.getCurrentBlockPosOrRailBelow());
        if (!(state.getBlock() instanceof RailSpeed r)) return vanilla;
        Gauge own = GaugeData.of(cart);
        if (!Gauge.runs(own, r.gauge())) return 0;   // wrong gauge: the vehicle cannot move on this track
        double cap = Math.max(vanilla, r.maxSpeed() * (cart.isInWater() ? 0.5 : 1.0));
        if (own != Gauge.ANY && curved(state)) cap *= Math.min(own.curveSpeed, r.gauge().curveSpeed);
        return cap;
    }

    private static RailShape shape(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.BaseRailBlock b ? state.getValue(b.getShapeProperty()) : null;
    }

    private static boolean curved(BlockState state) {
        RailShape s = shape(state);
        return s != null && s != RailShape.NORTH_SOUTH && s != RailShape.EAST_WEST && !s.isSlope();
    }

    /** Slots of a freight cart that take cargo: the vehicle's gauge payload share of the container (narrow 40 %), at least one. */
    public static int slotLimit(AbstractMinecart cart) {
        int size = cart instanceof net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer c ? c.getContainerSize() : 0;
        return Math.max(1, (int) Math.floor(size * GaugeData.of(cart).payload));
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
        MagicFuel.tick(cart);
        if (Autonomy.crewRequired(cart) > 1 && !Coupling.hasLeader(cart) && !Autonomy.mayRun(cart)) {   // a multi-crew loco (steam: driver and fireman) stands until fully crewed
            Vec3 v = cart.getDeltaMovement();
            cart.setDeltaMovement(0, v.y, 0);
        }
        BlockPos pos = cart.getCurrentBlockPosOrRailBelow();
        BlockState state = level.getBlockState(pos);
        Gauge own = GaugeData.of(cart);
        if (own != Gauge.ANY) {
            RailShape sh = shape(state);
            if (sh != null && sh.isSlope()) {   // grades: narrow keeps its speed, standard loses it
                Vec3 v = cart.getDeltaMovement();
                double keep = state.getBlock() instanceof RailSpeed rs ? Math.min(own.gradeKeep, rs.gauge().gradeKeep) : own.gradeKeep;
                cart.setDeltaMovement(v.x * keep, v.y, v.z * keep);
            }
        }
        if (state.getBlock() instanceof StationRail) {
            station(cart, level, pos, state);
            return;
        }
        if (state.getBlock() instanceof LockingRail) {
            locking(cart, level, pos, state);
            return;
        }
        if (!HOLDS.isEmpty()) HOLDS.remove(cart.getUUID());
        if (state.getBlock() instanceof OneWayRail) {
            Vec3 ok = OneWayRail.allowed(state);
            Vec3 d = cart.getDeltaMovement();
            if (ok != null && d.x * ok.x + d.z * ok.z < -0.02) cart.setDeltaMovement(-d.x * 0.5, d.y, -d.z * 0.5);   // turned back
        }
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
        Schedule sch = Schedule.get(cart);
        boolean crewed = Autonomy.mayRun(cart);
        boolean powered = state.getValue(PoweredRailBlock.POWERED) && (sch == null || crewed);
        Hold h = HOLDS.get(cart.getUUID());
        if (h != null && !h.at.equals(pos)) {
            HOLDS.remove(cart.getUUID());
            h = null;
        }
        Vec3 d = cart.getDeltaMovement();
        double sp = d.horizontalDistance();
        boolean target = sch != null && sch.target().equals(pos);
        if (powered) {
            if (target) Schedule.set(cart, sch.advance());
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
        if (sch != null && crewed && !target) {   // a scheduled, crewed cart does not stop at stations that are not its next stop
            h.passing = true;
            if (sp < 0.03) cart.setDeltaMovement(h.dir.x * 0.3, d.y, h.dir.z * 0.3);
            return;
        }
        if (sp < 0.03) {
            cart.setDeltaMovement(0, d.y, 0);
            if (sch != null && !crewed) return;   // no crew: wait here until someone climbs aboard (or the Age brings autonomy)
            h.waited++;
            int seconds = StationRail.SECONDS[state.getValue(StationRail.DELAY)];
            if (seconds > 0 && h.waited >= seconds * 20) {
                if (target) Schedule.set(cart, sch.advance());
                h.passing = true;
                cart.setDeltaMovement(h.dir.x * 0.3, d.y, h.dir.z * 0.3);
            }
        } else {
            cart.setDeltaMovement(d.x * 0.5, d.y, d.z * 0.5);
        }
    }

    private static void locking(AbstractMinecart cart, ServerLevel level, BlockPos pos, BlockState state) {
        if (Coupling.hasLeader(cart)) return;
        Hold h = HOLDS.get(cart.getUUID());
        if (h != null && !h.at.equals(pos)) {
            HOLDS.remove(cart.getUUID());
            h = null;
        }
        boolean powered = state.getValue(PoweredRailBlock.POWERED);
        Vec3 d = cart.getDeltaMovement();
        double sp = d.horizontalDistance();
        if (h == null) {
            Vec3 dir = sp > 0.01 ? new Vec3(d.x / sp, 0, d.z / sp) : axis(state);
            h = new Hold(pos, dir);
            h.wasPowered = powered;
            HOLDS.put(cart.getUUID(), h);
        }
        if (h.passing) return;
        if (sp >= 0.03) {
            cart.setDeltaMovement(d.x * 0.5, d.y, d.z * 0.5);
            return;
        }
        cart.setDeltaMovement(0, d.y, 0);
        boolean release;
        if (state.getValue(LockingRail.MODE) == 0) release = powered && !h.wasPowered;
        else release = ahead(level, pos, h.dir);
        h.wasPowered = powered;
        if (release) {
            h.passing = true;
            cart.setDeltaMovement(h.dir.x * 0.3, d.y, h.dir.z * 0.3);
        }
    }

    /** True when no other cart is on the rails ahead of pos in direction dir (up to LockingRail.AHEAD of them). */
    private static boolean ahead(ServerLevel level, BlockPos pos, Vec3 dir) {
        net.minecraft.core.Direction face = net.minecraft.core.Direction.getApproximateNearest(dir.x, 0, dir.z);
        for (BlockPos n : new BlockPos[]{pos.relative(face), pos.relative(face).above(), pos.relative(face).below()}) {
            if (RailTrace.isRail(level, n)) {
                java.util.List<BlockPos> rails = RailTrace.trace(level, n, LockingRail.AHEAD, pos);
                return !RailTrace.occupied(level, rails);
            }
        }
        return true;
    }

    private static Vec3 axis(BlockState state) {
        RailShape s = state.getValue(PoweredRailBlock.SHAPE);
        return (s == RailShape.EAST_WEST || s == RailShape.ASCENDING_EAST || s == RailShape.ASCENDING_WEST) ? new Vec3(1, 0, 0) : new Vec3(0, 0, 1);
    }
}
