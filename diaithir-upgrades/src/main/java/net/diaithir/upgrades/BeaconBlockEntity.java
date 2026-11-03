package net.diaithir.upgrades;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Holds modules (saved as the standard slot attachment) and shares them with machines within RANGE blocks. */
public final class BeaconBlockEntity extends BlockEntity {
    public static final int RANGE = 4;

    private static final Map<Level, Set<BlockPos>> LOADED = new ConcurrentHashMap<>();

    public BeaconBlockEntity(BlockPos pos, BlockState state) {
        super(Registry.BEACON_ENTITY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        track();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        track();
    }

    private void track() {
        if (level != null) LOADED.computeIfAbsent(level, l -> ConcurrentHashMap.newKeySet()).add(worldPosition.immutable());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            Set<BlockPos> set = LOADED.get(level);
            if (set != null) set.remove(worldPosition);
        }
    }

    /** Beacons that cover a block at pos. */
    static List<Beacon> covering(Level level, BlockPos pos) {
        List<Beacon> out = new ArrayList<>();
        Set<BlockPos> set = LOADED.get(level);
        if (set == null) return out;
        for (BlockPos p : set) {
            if (p.equals(pos) || Math.abs(p.getX() - pos.getX()) > RANGE || Math.abs(p.getY() - pos.getY()) > RANGE
                    || Math.abs(p.getZ() - pos.getZ()) > RANGE) continue;
            if (level.getBlockEntity(p) instanceof BeaconBlockEntity be) {
                Upgrades.targetOf(be).ifPresent(t -> out.add(Beacon.wrap(be.slots(t), RANGE)));
            }
        }
        return out;
    }

    Slotted slots(Target t) { return getData(Upgrades.SLOTS).toSlotted(t); }
}
