package net.diaithir.elevators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Holds the whole elevator. The shaft is the column of cells under the controller. Floors are FloorBlocks in the four columns
 * beside the shaft (at most 96 blocks below). The cabin is virtual: parked it is a PlateBlock in the shaft cell at a floor's height,
 * moving it is a block display that is moved every tick while riders are carried with it.
 */
public final class ControllerEntity extends BlockEntity {
    static final int NONE = 0, PARKED = 1, MOVING = 2;
    static final String TAG = "diaithirelevators_cabin";
    static final int REACH = 96;

    private final Handler energy = new Handler(100_000, 2_000, 0);
    int phase = NONE;
    int parkedY;
    int startY;
    int targetY;
    double y;
    private UUID display;
    private boolean resume;

    ControllerEntity(BlockPos pos, BlockState state) {
        super(ElevatorsMod.CONTROLLER_ENTITY.get(), pos, state);
    }

    private final class Handler extends SimpleEnergyHandler {
        Handler(int cap, int in, int out) { super(cap, in, out); }
        @Override protected void onEnergyChanged(int previous) { setChanged(); }
        void drain(int n) { set(Math.max(0, energy - n)); }
    }

    EnergyHandler energy() { return energy; }

    int cx() { return worldPosition.getX(); }
    int cz() { return worldPosition.getZ(); }

    // ---- finding the elevator from a block

    static ControllerEntity findInColumn(Level level, BlockPos from) {
        for (int y = from.getY() + 1; y <= Math.min(level.getMaxY(), from.getY() + REACH); y++) {
            if (level.getBlockEntity(new BlockPos(from.getX(), y, from.getZ())) instanceof ControllerEntity c) return c;
        }
        return null;
    }

    static ControllerEntity findAbove(Level level, BlockPos floor) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (findInColumn(level, floor.relative(d)) instanceof ControllerEntity c) return c;
        }
        return null;
    }

    // ---- shaft

    /** Floor heights of this shaft, lowest first. */
    List<Integer> floors() {
        List<Integer> out = new ArrayList<>();
        if (level == null) return out;
        int low = Math.max(level.getMinY(), worldPosition.getY() - REACH);
        for (int fy = low; fy <= worldPosition.getY() - 3; fy++) {
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(new BlockPos(cx() + d.getStepX(), fy, cz() + d.getStepZ())).getBlock() instanceof FloorBlock) {
                    out.add(fy);
                    break;
                }
            }
        }
        return out;
    }

    private boolean isPlate(BlockPos p) { return level.getBlockState(p).is(ElevatorsMod.PLATE.get()); }

    /** A cell the cabin or its riders may pass: empty collision, or the parked deck itself. */
    private boolean passable(int cy) {
        BlockPos p = new BlockPos(cx(), cy, cz());
        BlockState s = level.getBlockState(p);
        return s.is(ElevatorsMod.PLATE.get()) || s.getCollisionShape(level, p).isEmpty();
    }

    private boolean shaftClear(int fromY, int toY) {
        for (int cy = Math.min(fromY, toY); cy <= Math.max(fromY, toY) + 2; cy++) if (!passable(cy)) return false;
        return true;
    }

    boolean isParkedAt(int floorY) { return phase == PARKED && parkedY == floorY; }

    String status() {
        List<Integer> f = floors();
        String where = phase == MOVING ? "moving" : phase == PARKED ? "parked at floor " + (f.indexOf(parkedY) + 1) : "not yet placed";
        return f.size() + " floors, cabin " + where + (Config.ENERGY_PER_BLOCK > 0
                ? ", " + energy.getAmountAsInt() + " / " + energy.getCapacityAsInt() + " FE, " + Config.ENERGY_PER_BLOCK + " FE per block" : ", no power needed");
    }

    // ---- requests; each returns null on success or a message for the player

    private String ensureCabin(List<Integer> floors) {
        if (phase == NONE) {
            parkedY = floors.get(0);
            phase = PARKED;
        }
        BlockPos cell = new BlockPos(cx(), parkedY, cz());
        if (!isPlate(cell)) {
            if (!passable(parkedY)) return "Something blocks the cabin.";
            level.setBlock(cell, ElevatorsMod.PLATE.get().defaultBlockState(), 3);
        }
        setChanged();
        return null;
    }

    /** Goes to the next floor up (or down when sneaking), wrapping round at the end. */
    String cycle(boolean down) {
        List<Integer> f = floors();
        if (f.size() < 2) return "A shaft needs at least two floors.";
        if (phase == MOVING) return "The cabin is moving.";
        int cur = phase == NONE ? f.get(0) : parkedY;
        int to = down ? f.get(f.size() - 1) : f.get(0);
        for (int fy : f) {
            if (!down && fy > cur) { to = fy; break; }
            if (down && fy < cur) to = fy;
        }
        if (down && f.stream().noneMatch(fy -> fy < cur)) to = f.get(f.size() - 1);
        return request(to);
    }

    String request(int floorY) {
        if (!(level instanceof ServerLevel sl)) return "Not on a server.";
        List<Integer> f = floors();
        if (!f.contains(floorY)) return "That is not a floor of this shaft.";
        if (f.size() < 2) return "A shaft needs at least two floors.";
        if (phase == MOVING) return "The cabin is moving.";
        String err = ensureCabin(f);
        if (err != null) return err;
        if (parkedY == floorY) return "The cabin is already here.";
        if (!shaftClear(Math.min(parkedY, floorY), Math.max(parkedY, floorY))) return "The shaft is blocked.";
        int cost = Config.ENERGY_PER_BLOCK * Math.abs(floorY - parkedY);
        if (cost > energy.getAmountAsInt()) return "The controller needs " + cost + " FE for this trip.";
        energy.drain(cost);
        startY = parkedY;
        targetY = floorY;
        y = parkedY;
        phase = MOVING;
        BlockPos cell = new BlockPos(cx(), parkedY, cz());
        if (isPlate(cell)) level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
        spawnDisplay(sl);
        setChanged();
        return null;
    }

    // ---- motion

    void serverTick() {
        if (!(level instanceof ServerLevel sl)) return;
        if (phase != MOVING) return;
        if (resume) {
            resume = false;
            arrive(sl);
            return;
        }
        double remaining = Math.abs(targetY - y), travelled = Math.abs(y - startY);
        double dir = Math.signum(targetY - y);
        double v = Math.min(remaining, Math.min(Config.SPEED, 0.05 + 0.1 * Math.min(travelled, remaining)));
        double ny = y + dir * v;
        if (!passable((int) Math.floor(ny)) || !passable((int) Math.floor(ny + 2.8))) {
            strand(sl);
            return;
        }
        carry(sl, y, ny, false);
        y = ny;
        moveDisplay(sl);
        if (Math.abs(targetY - y) < 1e-6) arrive(sl);
    }

    private void arrive(ServerLevel sl) {
        y = targetY;
        carry(sl, targetY, targetY, true);
        park(sl, targetY);
    }

    /** Something blocked the way: park where the cabin is. */
    private void strand(ServerLevel sl) {
        int at = (int) Math.floor(y);
        carry(sl, y, y, true);
        park(sl, at);
    }

    private void park(ServerLevel sl, int cellY) {
        removeDisplays(sl);
        parkedY = cellY;
        phase = PARKED;
        BlockPos cell = new BlockPos(cx(), cellY, cz());
        if (passable(cellY)) level.setBlock(cell, ElevatorsMod.PLATE.get().defaultBlockState(), 3);
        setChanged();
    }

    /** Moves everything standing on the cabin from old to new height. */
    private void carry(ServerLevel sl, double oldY, double newY, boolean stop) {
        double dy = newY - oldY;
        AABB box = new AABB(cx() - 0.1, oldY + 0.5, cz() - 0.1, cx() + 1.1, oldY + 3.2, cz() + 1.1);
        for (Entity e : sl.getEntitiesOfClass(Entity.class, box, x -> !(x instanceof Display) && !x.isSpectator() && !x.isPassenger())) {
            e.resetFallDistance();
            if (e instanceof ServerPlayer p) {
                p.setDeltaMovement(0, stop ? 0 : dy, 0);
                p.hurtMarked = true;
                if (Math.abs(p.getY() - (newY + 1)) > 0.4) p.teleportTo(p.getX(), newY + 1, p.getZ());
            } else {
                if (dy != 0) e.teleportRelative(0, dy, 0);
                e.setDeltaMovement(0, 0, 0);
                e.setOnGround(true);
            }
        }
    }

    // ---- the moving display

    private void spawnDisplay(ServerLevel sl) {
        removeDisplays(sl);
        display = UUID.randomUUID();
        long m = display.getMostSignificantBits(), l = display.getLeastSignificantBits();
        String block = BuiltInRegistries.BLOCK.getKey(ElevatorsMod.PLATE.get()).toString();
        String cmd = "summon minecraft:block_display " + cx() + " " + y + " " + cz() + " {UUID:[I;" + (int) (m >> 32) + "," + (int) m + ","
                + (int) (l >> 32) + "," + (int) l + "],block_state:{Name:\"" + block + "\"},teleport_duration:3,Tags:[\"" + TAG + "\"]}";
        sl.getServer().getCommands().performPrefixedCommand(sl.getServer().createCommandSourceStack().withLevel(sl).withSuppressedOutput(), cmd);
    }

    private void moveDisplay(ServerLevel sl) {
        Entity e = display == null ? null : sl.getEntity(display);
        if (e == null) spawnDisplay(sl);
        else e.setPos(cx(), y, cz());
    }

    /** Removes the display, and any orphan left by an unload, from the whole shaft. */
    private void removeDisplays(ServerLevel sl) {
        AABB col = new AABB(cx() - 0.5, sl.getMinY(), cz() - 0.5, cx() + 1.5, worldPosition.getY(), cz() + 1.5);
        for (Display.BlockDisplay d : sl.getEntitiesOfClass(Display.BlockDisplay.class, col, x -> x.entityTags().contains(TAG))) d.discard();
        display = null;
    }

    /** The controller is gone: take the cabin with it. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel sl) {
            removeDisplays(sl);
            BlockPos cell = new BlockPos(cx(), parkedY, cz());
            if (phase == PARKED && isPlate(cell)) level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("phase", phase);
        out.putInt("parked", parkedY);
        out.putInt("target", targetY);
        energy.serialize(out.child("energy"));
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        phase = in.getIntOr("phase", NONE);
        parkedY = in.getIntOr("parked", 0);
        targetY = in.getIntOr("target", parkedY);
        in.child("energy").ifPresent(energy::deserialize);
        // a move cut short by a stop or unload is finished at once on the next tick
        if (phase == MOVING) resume = true;
    }
}
