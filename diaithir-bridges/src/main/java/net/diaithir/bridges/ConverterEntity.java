package net.diaithir.bridges;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.LimitingEnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/**
 * Lossy converter. The buffer holds the OUTPUT unit. Input faces (all but the front) take the input unit and store input * pct / 100 of it;
 * the front face gives the buffer away. EU kind: both sides are the plain Forge Energy capability (Modern Industrialization, Mekanism and
 * others expose it), so the loss is the point. AIR kind: one side is PneumaticCraft compressed air through {@link PneumaticAir}.
 */
public final class ConverterEntity extends BlockEntity {
    enum Kind { EU, AIR }

    private final Kind kind;
    private final Buf buf = new Buf();
    private final EnergyHandler input = new Input();

    ConverterEntity(BlockPos pos, BlockState state) {
        super(DiaithirBridges.CONVERTER.get(), pos, state);
        this.kind = state.getBlock() instanceof ConverterBlock b ? b.kind() : Kind.EU;
    }

    private final class Buf extends SimpleEnergyHandler {
        Buf() { super(Config.get(Config.BUFFER), Integer.MAX_VALUE, Integer.MAX_VALUE); }
        @Override protected void onEnergyChanged(int previous) { setChanged(); }
        void drain(int n) { set(Math.max(0, energy - n)); }
        void fill(int n) { set(Math.min(capacity, energy + n)); }
        void resize() { capacity = Config.get(Config.BUFFER); }
    }

    /** Takes input units from the neighbour and stores the converted amount. */
    private final class Input implements EnergyHandler {
        @Override public long getAmountAsLong() { return buf.getAmountAsLong(); }
        @Override public long getCapacityAsLong() { return buf.getCapacityAsLong(); }
        @Override public int extract(int amount, TransactionContext tx) { return 0; }
        @Override public int insert(int amount, TransactionContext tx) {
            int pct = pct(getBlockState().getValue(ConverterBlock.REVERSE));
            int in = Math.min(amount, Config.get(Config.RATE));
            int out = (int) Math.min(Integer.MAX_VALUE, (long) in * pct / 100);
            if (out <= 0) return 0;
            int stored = buf.insert(out, tx);
            return (int) Math.min(in, (stored * 100L + pct - 1) / pct);
        }
    }

    int pct(boolean reverse) {
        return switch (kind) {
            case EU -> Config.get(reverse ? Config.FE_TO_EU : Config.EU_TO_FE);
            case AIR -> Config.get(reverse ? Config.AIR_TO_FE : Config.FE_TO_AIR);
        };
    }

    String describe(boolean reverse) {
        String dir = kind == Kind.EU ? (reverse ? "FE to EU" : "EU to FE") : (reverse ? "air to FE" : "FE to air");
        String unit = kind == Kind.AIR && !reverse ? " mL" : " FE";
        String extra = kind == Kind.AIR && !PneumaticAir.available() ? ", PneumaticCraft not loaded" : "";
        return dir + ": " + pct(reverse) + " out per 100 in, " + buf.getAmountAsInt() + " / " + buf.getCapacityAsInt() + unit + extra;
    }

    /** Capability for a face, or null when that face has none in the current direction. */
    @Nullable EnergyHandler energyFor(@Nullable Direction side) {
        boolean reverse = getBlockState().getValue(ConverterBlock.REVERSE);
        Direction front = getBlockState().getValue(ConverterBlock.FACING);
        boolean feOut = kind == Kind.EU || reverse;
        boolean feIn = kind == Kind.EU || !reverse;
        if (side == front) return feOut ? new LimitingEnergyHandler(buf, 0, Config.get(Config.RATE)) : null;
        return feIn ? input : null;
    }

    void modeChanged() {
        buf.set(0);
    }

    int stored() { return buf.getAmountAsInt(); }

    void serverTick() {
        if (level == null) return;
        buf.resize();
        BlockState st = getBlockState();
        boolean reverse = st.getValue(ConverterBlock.REVERSE);
        Direction front = st.getValue(ConverterBlock.FACING);
        int rate = Config.get(Config.RATE);
        if (kind == Kind.AIR && reverse) {
            // air in: pull from neighbours behind and beside
            int pct = pct(true);
            for (Direction d : Direction.values()) {
                if (d == front) continue;
                Object h = PneumaticAir.find(level, worldPosition.relative(d), d.getOpposite());
                if (h == null) continue;
                int room = (int) ((buf.getCapacityAsLong() - buf.getAmountAsLong()) * 100L / pct);
                int take = Math.min(Math.min(rate, PneumaticAir.air(h)), room);
                if (take > 0) {
                    PneumaticAir.add(h, -take);
                    buf.fill((int) ((long) take * pct / 100));
                }
            }
        }
        if (buf.getAmountAsInt() <= 0) return;
        BlockPos target = worldPosition.relative(front);
        if (kind == Kind.AIR && !reverse) {
            Object h = PneumaticAir.find(level, target, front.getOpposite());
            if (h != null && PneumaticAir.pressure(h) < Config.MAX_BAR.get().floatValue()) {
                int n = Math.min(rate, buf.getAmountAsInt());
                PneumaticAir.add(h, n);
                buf.drain(n);
            }
        } else {
            EnergyHandlerUtil.move(buf, level.getCapability(Capabilities.Energy.BLOCK, target, front.getOpposite()), rate, null);
        }
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        buf.serialize(out.child("buffer"));
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("buffer").ifPresent(buf::deserialize);
    }
}
