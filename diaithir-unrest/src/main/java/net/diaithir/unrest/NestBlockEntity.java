package net.diaithir.unrest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;

/**
 * A nest. Each step it takes a share of the Unrest in nearby chunks into its store, grows with what it has absorbed in total, and spends
 * store on attack waves aimed at the loudest chunk it can see. Size sets tier; the tier is capped by Config.MAX_TIER (raised by Age).
 */
public final class NestBlockEntity extends BlockEntity {
    /** Total absorbed needed for tier 0, 1, 2, 3, 4. */
    static final double[] TIER_AT = {0, 400, 1500, 5000, 15000};

    private double store;
    private double lifetime;

    public NestBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirUnrest.NEST_BE.get(), pos, state);
    }

    public double store() { return store; }
    public double lifetime() { return lifetime; }

    public static int tierFor(double lifetime) {
        int t = 0;
        for (int i = 0; i < TIER_AT.length; i++) if (lifetime >= TIER_AT[i]) t = i;
        return t;
    }

    public int tier() {
        return Math.min(tierFor(lifetime), Config.MAX_TIER.getAsInt());
    }

    /** One step. Public so the self test can drive it. */
    public void step(ServerLevel level) {
        Field f = Unrest.field(level);
        ChunkPos me = ChunkPos.containing(worldPosition);
        int r = Config.NEST_RADIUS.getAsInt();
        double rate = Config.NEST_RATE.getAsDouble();
        int bx = me.x(), bz = me.z();
        double loud = 0;
        for (int x = me.x() - r; x <= me.x() + r; x++) {
            for (int z = me.z() - r; z <= me.z() + r; z++) {
                double have = f.get(x, z);
                if (have <= 0) continue;
                double got = f.take(x, z, have * rate);
                store += got;
                lifetime += got;
                if (have > loud && (x != me.x() || z != me.z())) {
                    loud = have;
                    bx = x;
                    bz = z;
                }
            }
        }
        double cost = Config.WAVE_COST.getAsDouble();
        while (store >= cost && loud > 0) {
            BlockPos target = new BlockPos(bx * 16 + 8, worldPosition.getY(), bz * 16 + 8);
            int size = 2 + tier() * 2;
            NestWaveEvent ev = NeoForge.EVENT_BUS.post(new NestWaveEvent(level, worldPosition, target, tier(), size));
            if (ev.isCanceled()) break;
            store -= cost;
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putDouble("store", store);
        out.putDouble("lifetime", lifetime);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        store = in.getDoubleOr("store", 0);
        lifetime = in.getDoubleOr("lifetime", 0);
    }
}
