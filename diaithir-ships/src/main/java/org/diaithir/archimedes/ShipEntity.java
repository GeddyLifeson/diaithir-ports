package org.diaithir.archimedes;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

/**
 * A ship: a block structure that floats, flies and is steered from the helm. Server authoritative.
 * Rotation uses entity yRot with the convention x' = x cos - z sin, z' = x sin + z cos (90 degrees = Rotation.CLOCKWISE_90).
 */
public class ShipEntity extends Entity implements IEntityWithComplexSpawn {
    private static final double GRAVITY = 0.05, BASE_FORWARD = 0.025, BASE_LIFT = 0.04, BASE_TURN = 1.2;

    private ShipChunk chunk = new ShipChunk();
    private final Map<BlockPos, Integer> burn = new HashMap<>();
    private double motionYaw;
    private boolean flying;
    private boolean hadRider;
    /** Test hook: when set, replaces the rider's input. */
    public Input testInput;

    public ShipEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public ShipChunk chunk() { return chunk; }

    public void setChunk(ShipChunk c) {
        chunk = c;
        setBoundingBox(makeBoundingBox());
    }

    public boolean isFlying() { return flying; }

    private static double rx(double x, double z, double yawDeg) {
        double r = Math.toRadians(yawDeg);
        return x * Math.cos(r) - z * Math.sin(r);
    }

    private static double rz(double x, double z, double yawDeg) {
        double r = Math.toRadians(yawDeg);
        return x * Math.sin(r) + z * Math.cos(r);
    }

    @Override
    protected AABB makeBoundingBox(Vec3 p) {
        if (chunk == null || chunk.blocks.isEmpty()) return super.makeBoundingBox(p);
        double minx = Double.MAX_VALUE, maxx = -Double.MAX_VALUE, minz = Double.MAX_VALUE, maxz = -Double.MAX_VALUE;
        double[][] corners = {{-chunk.centerX(), -chunk.centerZ()}, {chunk.sizeX - chunk.centerX(), -chunk.centerZ()},
                {-chunk.centerX(), chunk.sizeZ - chunk.centerZ()}, {chunk.sizeX - chunk.centerX(), chunk.sizeZ - chunk.centerZ()}};
        for (double[] c : corners) {
            double x = rx(c[0], c[1], getYRot()), z = rz(c[0], c[1], getYRot());
            minx = Math.min(minx, x); maxx = Math.max(maxx, x);
            minz = Math.min(minz, z); maxz = Math.max(maxz, z);
        }
        return new AABB(p.x + minx, p.y, p.z + minz, p.x + maxx, p.y + chunk.sizeY, p.z + maxz);
    }

    /** World position of the centre of a local block. */
    public Vec3 toWorld(BlockPos local) {
        double lx = local.getX() + 0.5 - chunk.centerX(), lz = local.getZ() + 0.5 - chunk.centerZ();
        return new Vec3(getX() + rx(lx, lz, getYRot()), getY() + local.getY() + 0.5, getZ() + rz(lx, lz, getYRot()));
    }

    /** Ship-local point to world space (local y is measured from the ship's feet). */
    public Vec3 toWorldPoint(Vec3 local) {
        double lx = local.x - chunk.centerX(), lz = local.z - chunk.centerZ();
        return new Vec3(getX() + rx(lx, lz, getYRot()), getY() + local.y, getZ() + rz(lx, lz, getYRot()));
    }

    public Vec3 toLocalPoint(Vec3 world) {
        return toLocal(world);
    }

    private Vec3 toLocal(Vec3 w) {
        double dx = w.x - getX(), dz = w.z - getZ();
        double r = Math.toRadians(getYRot());
        double x = dx * Math.cos(r) + dz * Math.sin(r), z = -dx * Math.sin(r) + dz * Math.cos(r);
        return new Vec3(x + chunk.centerX(), w.y - getY(), z + chunk.centerZ());
    }

    /** First ship block on the line of sight, or null. */
    public BlockPos pick(Player p) {
        Vec3 eye = p.getEyePosition(), dir = p.getLookAngle();
        for (double t = 0; t < 7; t += 0.05) {
            Vec3 l = toLocal(eye.add(dir.scale(t)));
            BlockPos b = BlockPos.containing(l);
            if (chunk.blocks.containsKey(b)) return b;
        }
        return null;
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b) {}
    @Override public boolean isPickable() { return !isRemoved(); }
    @Override public boolean canBeCollidedWith(Entity other) { return true; }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
    @Override public boolean isIgnoringBlockTriggers() { return true; }
    @Override public net.minecraft.world.entity.LivingEntity getControllingPassenger() { return null; }
    @Override protected boolean canAddPassenger(Entity e) { return getPassengers().isEmpty(); }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dims, float scale) {
        BlockPos h = chunk.helm;
        double lx = h.getX() + 0.5 - chunk.centerX(), lz = h.getZ() + 0.5 - chunk.centerZ();
        return new Vec3(rx(lx, lz, getYRot()), h.getY() + 1.0, rz(lx, lz, getYRot()));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        BlockPos hit = pick(player);
        if (hit == null) return InteractionResult.PASS;
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        BlockState s = chunk.blocks.get(hit);
        if (s.is(ArchimedesMod.MARKER.get()) || s.is(ArchimedesMod.SEAT.get())) {
            if (!player.isPassenger()) player.startRiding(this);
            return InteractionResult.CONSUME;
        }
        if (s.is(ArchimedesMod.ENGINE.get())) {
            ItemStack held = player.getItemInHand(hand);
            int ticks = held.isEmpty() ? 0 : level().fuelValues().burnDuration(held);
            if (ticks > 0 && burn.getOrDefault(hit, 0) < 20 * 60 * 20) {
                burn.merge(hit, ticks, Integer::sum);
                held.shrink(1);
                player.sendOverlayMessage(Component.translatable("archimedes.engine.fueled", burn.get(hit) / 20));
                return InteractionResult.CONSUME;
            }
            player.sendOverlayMessage(Component.translatable("archimedes.engine.status", burn.getOrDefault(hit, 0) / 20));
            return InteractionResult.CONSUME;
        }
        if (s.is(ArchimedesMod.CRATE.get()) && player instanceof ServerPlayer sp) {
            NonNullList<ItemStack> items = chunk.crates.computeIfAbsent(hit, k -> NonNullList.withSize(ShipChunk.CRATE_SLOTS, ItemStack.EMPTY));
            SimpleContainer c = new SimpleContainer(items.toArray(new ItemStack[0])) {
                @Override public void setChanged() {
                    super.setChanged();
                    for (int i = 0; i < items.size(); i++) items.set(i, getItem(i));
                }
            };
            sp.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, pl) -> ChestMenu.threeRows(id, inv, c), Component.translatable("block.archimedes.crate")));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    // ---- simulation ----

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || chunk.blocks.isEmpty()) return;
        serverTick((ServerLevel) level());
    }

    private double waterline() {
        AABB bb = getBoundingBox();
        double best = Double.NEGATIVE_INFINITY;
        int y0 = Mth.floor(bb.minY);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(bb.minX); x <= Mth.floor(bb.maxX); x++) {
            for (int z = Mth.floor(bb.minZ); z <= Mth.floor(bb.maxZ); z++) {
                for (int y = y0 - 1; y <= Mth.floor(bb.maxY); y++) {
                    m.set(x, y, z);
                    var f = level().getFluidState(m);
                    if (f.is(FluidTags.WATER)) {
                        best = Math.max(best, y + f.getHeight(level(), m));
                    }
                }
            }
        }
        return best;
    }

    private Input input() {
        if (testInput != null) return testInput;
        for (Entity e : getPassengers()) if (e instanceof ServerPlayer sp) return sp.getLastClientInput();
        return Input.EMPTY;
    }

    private void serverTick(ServerLevel level) {
        boolean rider = !getPassengers().isEmpty() || testInput != null;
        if (hadRider && !rider && ArchimedesConfig.DISASSEMBLE_ON_DISMOUNT.get()) {
            disassemble(level);
            return;
        }
        hadRider = rider;

        Vec3 dm = getDeltaMovement();
        double mx = dm.x, my = dm.y, mz = dm.z;

        // engines
        float power = 0;
        if (rider) {
            for (var it = burn.entrySet().iterator(); it.hasNext(); ) {
                var e = it.next();
                if (e.getValue() <= 0) { it.remove(); continue; }
                e.setValue(e.getValue() - 1);
                power += 1f;
            }
        }
        boolean canFly = ArchimedesConfig.ENABLE_AIRSHIPS.get() && chunk.balloons >= chunk.blocks.size() * ArchimedesConfig.FLY_BALLOON_RATIO.get();

        // buoyancy
        double surface = waterline();
        double depth = Math.min(chunk.sizeY, Math.max(0, surface - getY()));
        double volume = 0;
        for (int y = 0; y < chunk.layerCount.length; y++) {
            volume += chunk.layerCount[y] * Mth.clamp(depth - y, 0, 1);
        }
        if (volume > 0) {
            flying = false;
            my += ShipDensity.WATER * volume * GRAVITY / chunk.mass;
        }
        if (onGround()) flying = false;
        if (!flying) my -= GRAVITY;

        // pilot
        Input in = input();
        boolean braking = in.sprint();
        double speedMult = 1 + power * 0.5, turnMult = 1 + power * 0.25, liftMult = 1 + power * 0.5;
        double throttle = (in.forward() ? 1 : 0) - (in.backward() ? 1 : 0);
        double steer = (in.left() ? 1 : 0) - (in.right() ? 1 : 0);
        if (flying) throttle *= 0.5;
        Direction front = Direction.from2DDataValue(chunk.front);
        double fx = rx(front.getStepX(), front.getStepZ(), getYRot()), fz = rz(front.getStepX(), front.getStepZ(), getYRot());
        mx *= 0.9; mz *= 0.9;
        mx += fx * throttle * BASE_FORWARD * speedMult;
        mz += fz * throttle * BASE_FORWARD * speedMult;
        motionYaw -= steer * BASE_TURN * turnMult * ArchimedesConfig.TURN_SPEED.get();
        if (braking) { mx *= 0.9; mz *= 0.9; if (flying) my *= 0.9; }
        if (canFly && (in.jump() || (flying && in.shift()))) {
            if (in.jump()) { flying = true; my += BASE_LIFT * liftMult; }
            else my -= BASE_LIFT * liftMult;
        }
        if (!rider && flying) my -= BASE_LIFT * 0.2;

        // limits and friction
        double lim = ArchimedesConfig.SPEED_LIMIT.get();
        double hv = Math.sqrt(mx * mx + mz * mz);
        if (hv > lim) { mx *= lim / hv; mz *= lim / hv; }
        my = Mth.clamp(my, -lim, lim);
        if (onGround()) { mx *= 0.8; mz *= 0.8; }

        java.util.Map<Entity, Vec3> deck = DeckWalk.collect(this);
        float yawBefore = getYRot();
        setYRot((float) (getYRot() + motionYaw));
        motionYaw *= 0.7;
        setBoundingBox(makeBoundingBox());
        setDeltaMovement(mx, my, mz);
        move(MoverType.SELF, getDeltaMovement());
        DeckWalk.carry(this, deck, getYRot() - yawBefore);
        Vec3 d = getDeltaMovement();
        setDeltaMovement(d.x * 0.98, d.y * (volume > 0 ? 0.9 : 0.98), d.z * 0.98);
    }

    // ---- disassembly ----

    public void disassemble(ServerLevel level) {
        int q = Math.floorMod(Math.round(getYRot() / 90f), 4);
        Rotation rot = switch (q) { case 1 -> Rotation.CLOCKWISE_90; case 2 -> Rotation.CLOCKWISE_180; case 3 -> Rotation.COUNTERCLOCKWISE_90; default -> Rotation.NONE; };
        float yaw = q * 90f;
        // snap the pivot so the first block lands on a block centre
        double lx = 0.5 - chunk.centerX(), lz = 0.5 - chunk.centerZ();
        double cx = getX() + rx(lx, lz, yaw), cz = getZ() + rz(lx, lz, yaw);
        double px = getX() + (Math.floor(cx) + 0.5 - cx), pz = getZ() + (Math.floor(cz) + 0.5 - cz);
        int py = (int) Math.round(getY());

        int bestDy = 0, bestConf = Integer.MAX_VALUE;
        for (int dy = 0; dy <= 3 && bestConf > 0; dy++) {
            int conf = 0;
            for (BlockPos l : chunk.blocks.keySet()) {
                if (!level.getBlockState(worldPos(l, px, py + dy, pz, yaw)).canBeReplaced()) conf++;
            }
            if (conf < bestConf) { bestConf = conf; bestDy = dy; }
        }
        for (var e : chunk.blocks.entrySet()) {
            BlockPos wp = worldPos(e.getKey(), px, py + bestDy, pz, yaw);
            BlockState s = e.getValue().rotate(rot);
            if (!level.getBlockState(wp).canBeReplaced()) {
                Block.popResource(level, wp.above(), new ItemStack(s.getBlock()));
                continue;
            }
            level.setBlock(wp, s, Block.UPDATE_ALL);
            var items = chunk.crates.get(e.getKey());
            if (items != null && level.getBlockEntity(wp) instanceof org.diaithir.archimedes.block.CrateBlock.Entity be) {
                be.items = NonNullList.copyOf(items);
                be.setChanged();
            }
        }
        Vec3 up = Vec3.atBottomCenterOf(worldPos(chunk.helm, px, py + bestDy, pz, yaw).above());
        for (Entity p : java.util.List.copyOf(getPassengers())) {
            p.stopRiding();
            p.teleportTo(up.x, up.y, up.z);
        }
        discard();
    }

    private BlockPos worldPos(BlockPos l, double px, int py, double pz, float yaw) {
        double x = l.getX() + 0.5 - chunk.centerX(), z = l.getZ() + 0.5 - chunk.centerZ();
        return BlockPos.containing(px + rx(x, z, yaw), py + l.getY() + 0.5, pz + rz(x, z, yaw));
    }

    // ---- persistence and sync ----

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        out.store("ship", CompoundTag.CODEC, chunk.save(level().registryAccess(), true));
        CompoundTag b = new CompoundTag();
        burn.forEach((p, t) -> b.putInt(p.getX() + "," + p.getY() + "," + p.getZ(), t));
        out.store("burn", CompoundTag.CODEC, b);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        in.read("ship", CompoundTag.CODEC).ifPresent(t -> setChunk(ShipChunk.load(t, level().registryAccess())));
        in.read("burn", CompoundTag.CODEC).ifPresent(b -> {
            for (String k : b.keySet()) {
                String[] s = k.split(",");
                burn.put(new BlockPos(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2])), b.getIntOr(k, 0));
            }
        });
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        buf.writeNbt(chunk.save(level().registryAccess(), false));
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        CompoundTag t = (CompoundTag) buf.readNbt();
        if (t != null) setChunk(ShipChunk.load(t, level().registryAccess()));
    }
}
