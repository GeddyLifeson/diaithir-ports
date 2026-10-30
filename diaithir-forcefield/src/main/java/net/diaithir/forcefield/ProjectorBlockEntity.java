package net.diaithir.forcefield;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The projector. Between passes it does nothing. Every checkIntervalTicks it draws its upkeep, grows or drops the shell in batches
 * and, when the shell stands, looks once at the living things inside the shell's bounding box and holds out those not authorised.
 */
public final class ProjectorBlockEntity extends BlockEntity {
    private enum Phase { IDLE, BUILDING, UP, TEARDOWN }

    /** FE buffer. Only the projector itself can draw from it. */
    public final class Buffer extends SimpleEnergyHandler {
        Buffer() {
            super(1, 1, 0);
        }

        void refit() {
            capacity = Config.BASE_CAPACITY.get() + Config.CAPACITOR_BONUS.get() * modules[Module.CAPACITOR.ordinal()];
            maxInsert = Config.MAX_RECEIVE.get();
            if (energy > capacity) energy = capacity;
        }

        void drain(int amount) {
            if (amount > 0) set(Math.max(0, energy - amount));
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    }

    public final Buffer energy = new Buffer();
    private final int[] modules = new int[Module.values().length];
    private final Map<UUID, String> guests = new LinkedHashMap<>();
    private UUID owner;
    private String ownerName = "";
    private Geometry.Shape shape = Geometry.Shape.CUBE;
    private boolean enabled = true;
    private boolean teamAccess = true;

    // runtime only
    private Phase phase = Phase.IDLE;
    private boolean planDirty = true;
    private int radius;
    private int thick;
    private int[] offsets = new int[0];
    private int cursor;
    private int tickCount;

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirForcefield.PROJECTOR_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        energy.refit();
    }

    // ---- settings -------------------------------------------------------------------------------------------------------------

    public void setOwner(Player p) {
        owner = p.getUUID();
        ownerName = p.getGameProfile().name();
        setChanged();
    }

    public UUID owner() {
        return owner;
    }

    public Geometry.Shape shape() {
        return shape;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean teamAccess() {
        return teamAccess;
    }

    public int moduleCount(Module m) {
        return modules[m.ordinal()];
    }

    public boolean built() {
        return phase == Phase.UP;
    }

    public int radius() {
        return radius;
    }

    public int thickness() {
        return thick;
    }

    public int shellBlocks() {
        return offsets.length / 3;
    }

    public Map<UUID, String> guests() {
        return guests;
    }

    public void setEnabled(boolean on) {
        enabled = on;
        setChanged();
    }

    public void setTeamAccess(boolean on) {
        teamAccess = on;
        setChanged();
    }

    public void setShape(Geometry.Shape s) {
        shape = s;
        planDirty = true;
        setChanged();
    }

    public void cycleShape() {
        setShape(shape.next());
    }

    public boolean install(Module m) {
        int i = m.ordinal();
        if (modules[i] >= Config.MAX_PER_MODULE.get()) return false;
        modules[i]++;
        if (m == Module.RANGE || m == Module.STRENGTH) planDirty = true;
        energy.refit();
        setChanged();
        return true;
    }

    public void addGuest(UUID id, String name) {
        guests.put(id, name);
        setChanged();
    }

    public boolean removeGuest(UUID id) {
        boolean had = guests.remove(id) != null;
        if (had) setChanged();
        return had;
    }

    public boolean canManage(Player p) {
        return owner == null || owner.equals(p.getUUID()) || p.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
    }

    public int range() {
        return Math.min(Config.MAX_RANGE.get(), Config.BASE_RANGE.get() + Config.RANGE_STEP.get() * modules[Module.RANGE.ordinal()]);
    }

    public int layers() {
        return Math.min(Config.MAX_THICKNESS.get(), 1 + modules[Module.STRENGTH.ordinal()]);
    }

    private int normalX() {
        Direction d = getBlockState().getValue(ProjectorBlock.FACING);
        return d.getStepX();
    }

    private int normalZ() {
        Direction d = getBlockState().getValue(ProjectorBlock.FACING);
        return d.getStepZ();
    }

    public String summary() {
        return "Shape " + shape.name().toLowerCase() + ", radius " + (radius > 0 ? radius : range()) + ", layers " + layers() + ", "
                + energy.getAmountAsInt() + "/" + energy.getCapacityAsInt() + " FE, field " + (built() ? "up" : "down");
    }

    // ---- passes ---------------------------------------------------------------------------------------------------------------

    public void serverTick(ServerLevel level) {
        if (++tickCount % Config.CHECK_INTERVAL.get() != 0) return;
        pass(level);
    }

    /** FE drawn for one pass. */
    private int passCost() {
        int perTick = Config.UPKEEP.get() == 0 ? 0 : Math.max(1, (int) ((long) shellBlocks() * Config.UPKEEP.get() / 1000L));
        return perTick * Config.CHECK_INTERVAL.get();
    }

    /** One pass. Public so the self test can step the projector without waiting for ticks. */
    public void pass(ServerLevel level) {
        pass(level, List.of());
    }

    /** As {@link #pass(ServerLevel)}, also checking the given entities (the self test uses this for fake players, which no level lists). */
    public void pass(ServerLevel level, List<? extends LivingEntity> extra) {
        energy.refit();
        if (phase == Phase.TEARDOWN) {
            if (removeBatch(level)) phase = Phase.IDLE;
            return;
        }
        if (planDirty) {
            if (phase != Phase.IDLE) {
                phase = Phase.TEARDOWN;
                cursor = 0;
                return;
            }
            plan();
        }
        int cost = passCost();
        boolean powered = enabled && energy.getAmountAsInt() >= Math.max(cost, 1);
        if (!powered) {
            setActive(level, false);
            if (phase != Phase.IDLE) {
                phase = Phase.TEARDOWN;
                cursor = 0;
            }
            return;
        }
        energy.drain(cost);
        setActive(level, true);
        if (phase == Phase.IDLE) {
            phase = Phase.BUILDING;
            cursor = 0;
        }
        if (phase == Phase.BUILDING && buildBatch(level)) phase = Phase.UP;
        enforce(level, extra);
    }

    private void plan() {
        int t = layers();
        int nx = normalX(), nz = normalZ();
        radius = Geometry.fit(shape, range(), t, nx, nz, Config.MAX_FIELD_BLOCKS.get());
        thick = t;
        offsets = Geometry.shell(shape, radius, t, nx, nz);
        planDirty = false;
    }

    private void setActive(ServerLevel level, boolean on) {
        BlockState s = getBlockState();
        if (s.getValue(ProjectorBlock.ACTIVE) != on) level.setBlock(worldPosition, s.setValue(ProjectorBlock.ACTIVE, on), Block.UPDATE_CLIENTS);
    }

    private boolean buildBatch(ServerLevel level) {
        BlockState field = DiaithirForcefield.FIELD.get().defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int end = Math.min(offsets.length, cursor + Config.BUILD_PER_PASS.get() * 3);
        for (; cursor < end; cursor += 3) {
            m.set(worldPosition.getX() + offsets[cursor], worldPosition.getY() + offsets[cursor + 1], worldPosition.getZ() + offsets[cursor + 2]);
            if (!level.isInsideBuildHeight(m.getY()) || !level.hasChunkAt(m)) continue;
            if (level.getBlockState(m).isAir()) level.setBlock(m, field, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        return cursor >= offsets.length;
    }

    /** Removes field blocks in a batch; true when the whole shell has been visited. */
    private boolean removeBatch(ServerLevel level) {
        return removeBatch(level, Config.BUILD_PER_PASS.get());
    }

    private boolean removeBatch(ServerLevel level, int limit) {
        BlockState air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int end = (int) Math.min(offsets.length, (long) cursor + (long) limit * 3);
        for (; cursor < end; cursor += 3) {
            m.set(worldPosition.getX() + offsets[cursor], worldPosition.getY() + offsets[cursor + 1], worldPosition.getZ() + offsets[cursor + 2]);
            if (!level.isInsideBuildHeight(m.getY()) || !level.hasChunkAt(m)) continue;
            if (level.getBlockState(m).is(DiaithirForcefield.FIELD.get())) level.setBlock(m, air, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        return cursor >= offsets.length;
    }

    /** Takes the whole shell down at once. Used when the projector is broken. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel sl && offsets.length > 0) {
            cursor = 0;
            removeBatch(sl, Integer.MAX_VALUE / 4);
            phase = Phase.IDLE;
        }
    }

    // ---- who is held out ------------------------------------------------------------------------------------------------------

    public boolean authorised(Player p) {
        if (Config.CREATIVE_BYPASS.get() && (p.isCreative() || p.isSpectator())) return true;
        UUID id = p.getUUID();
        if (id.equals(owner)) return true;
        if (modules[Module.GUEST_LIST.ordinal()] > 0 && guests.containsKey(id)) return true;
        if (teamAccess && level instanceof ServerLevel sl && !ownerName.isEmpty()) {
            var ownerTeam = sl.getScoreboard().getPlayersTeam(ownerName);
            var team = sl.getScoreboard().getPlayersTeam(p.getGameProfile().name());
            return ownerTeam != null && ownerTeam == team;
        }
        return false;
    }

    /** True when the field must hold this entity out. */
    public boolean blocks(Entity e) {
        if (e instanceof Player p) return modules[Module.MOB_FILTER.ordinal()] == 0 && !authorised(p);
        if (e instanceof OwnableEntity pet && pet.getOwnerReference() != null) {
            UUID ownerId = pet.getOwnerReference().getUUID();
            if (ownerId.equals(owner)) return false;
        }
        if (e instanceof Enemy) return true;
        return Config.BLOCK_NEUTRAL.get() && e instanceof Mob;
    }

    private void enforce(ServerLevel level, List<? extends LivingEntity> extra) {
        int nx = normalX(), nz = normalZ();
        int[] b = Geometry.bounds(shape, radius, thick, nx, nz);
        BlockPos p = worldPosition;
        AABB box = new AABB(p.getX() + b[0], p.getY() + b[1], p.getZ() + b[2], p.getX() + b[3] + 1, p.getY() + b[4] + 1, p.getZ() + b[5] + 1);
        List<LivingEntity> inside = level.getEntitiesOfClass(LivingEntity.class, box);
        if (!extra.isEmpty()) {
            inside = new java.util.ArrayList<>(inside);
            for (LivingEntity x : extra) if (!inside.contains(x)) inside.add(x);
        }
        for (LivingEntity e : inside) {
            if (!e.isAlive() || !blocks(e)) continue;
            double dx = e.getX() - (p.getX() + 0.5), dy = e.getY() + e.getBbHeight() / 2 - (p.getY() + 0.5), dz = e.getZ() - (p.getZ() + 0.5);
            if (!Geometry.inVolume(shape, dx, dy, dz, radius, thick, nx, nz)) continue;
            double[] to = Geometry.eject(shape, dx, dy, dz, radius, thick, nx, nz);
            pushOut(level, e, p.getX() + 0.5 + to[0], e.getY() + (to[1] - dy), p.getZ() + 0.5 + to[2]);
        }
    }

    private void pushOut(ServerLevel level, LivingEntity e, double x, double y, double z) {
        AABB moved = e.getBoundingBox().move(x - e.getX(), y - e.getY(), z - e.getZ());
        boolean found = false;
        for (int up = 0; up <= 8 && !found; up++) {
            if (level.noCollision(e, moved.move(0, up, 0))) {
                y += up;
                found = true;
            }
        }
        if (!found) return;
        e.teleportTo(level, x, y, z, Set.of(), e.getYRot(), e.getXRot(), false);
        if (Math.abs(e.getX() - x) > 0.5 || Math.abs(e.getZ() - z) > 0.5) e.snapTo(x, y, z); // players without a connection (fake players)
        e.setDeltaMovement(0, 0, 0);
        e.hurtMarked = true;
        if (modules[Module.STING.ordinal()] > 0 && Config.ALLOW_STING.get() && Config.STING_DAMAGE.get() > 0) {
            e.hurtServer(level, level.damageSources().magic(), Config.STING_DAMAGE.get().floatValue());
        }
        if (e instanceof ServerPlayer sp) sp.sendOverlayMessage(Component.literal("The field holds you out."));
    }

    // ---- saving ---------------------------------------------------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("energy", energy.getAmountAsInt());
        out.putString("owner", owner == null ? "" : owner.toString());
        out.putString("ownerName", ownerName);
        out.putString("shape", shape.name());
        out.putBoolean("enabled", enabled);
        out.putBoolean("team", teamAccess);
        out.putIntArray("modules", modules);
        StringBuilder sb = new StringBuilder();
        guests.forEach((id, n) -> sb.append(id).append('=').append(n).append(';'));
        out.putString("guests", sb.toString());
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        int[] m = in.getIntArray("modules").orElse(new int[0]);
        for (int i = 0; i < modules.length; i++) modules[i] = i < m.length ? m[i] : 0;
        energy.set(Math.max(0, in.getIntOr("energy", 0)));
        String o = in.getStringOr("owner", "");
        owner = o.isEmpty() ? null : UUID.fromString(o);
        ownerName = in.getStringOr("ownerName", "");
        try {
            shape = Geometry.Shape.valueOf(in.getStringOr("shape", "CUBE"));
        } catch (IllegalArgumentException ex) {
            shape = Geometry.Shape.CUBE;
        }
        enabled = in.getBooleanOr("enabled", true);
        teamAccess = in.getBooleanOr("team", true);
        guests.clear();
        for (String part : in.getStringOr("guests", "").split(";")) {
            int eq = part.indexOf('=');
            if (eq > 0) guests.put(UUID.fromString(part.substring(0, eq)), part.substring(eq + 1));
        }
        planDirty = true;
        phase = Phase.IDLE;
    }
}
