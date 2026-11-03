package net.diaithir.computing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.ArrayList;
import java.util.List;

/** Holds the program, the last run's output and the signal the Reckoner drives out of each side. */
public final class ReckonerEntity extends BlockEntity {
    static final int MAX_PROGRAM = 16_000, MAX_LOG = 20, MAX_LINE = 200;
    static final String[] SIDES = {"bottom", "top", "north", "south", "west", "east"};   // indexes match Direction.get3DDataValue()

    public String program = "";
    public final int[] output = new int[6];
    public final List<String> log = new ArrayList<>();
    public String error = "";
    public int runs;

    public ReckonerEntity(BlockPos pos, BlockState state) {
        super(Registry.RECKONER_ENTITY.get(), pos, state);
    }

    static int side(LuaValue v) {
        String s = v.checkjstring().toLowerCase();
        for (int i = 0; i < 6; i++) if (SIDES[i].equals(s)) return i;
        throw new org.luaj.vm2.LuaError("unknown side '" + s + "' (bottom, top, north, south, west, east)");
    }

    public boolean setProgram(String text) {
        if (text.length() > MAX_PROGRAM) return false;
        program = text;
        setChanged();
        return true;
    }

    /** Runs the program once. Returns the error, or null. */
    public String run() {
        if (level == null || level.isClientSide()) return "not on a server";
        log.clear();
        int[] before = output.clone();
        runs++;
        error = orEmpty(Sandbox.run(program, this::install, line -> {
            if (log.size() < MAX_LOG) log.add(line.length() > MAX_LINE ? line.substring(0, MAX_LINE) : line);
        }));
        setChanged();
        if (!java.util.Arrays.equals(before, output)) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        return error.isEmpty() ? null : error;
    }

    private static String orEmpty(String s) { return s == null ? "" : s; }

    /** Lua runs on its own coroutine thread, where touching the world would deadlock, so everything a program can read is copied on the server thread first. */
    private void install(Globals g) {
        final int[] in = new int[6];
        @SuppressWarnings("unchecked") final List<Inventories.Entry>[] inv = new List[6];
        for (Direction d : Direction.values()) {
            in[d.get3DDataValue()] = level.getSignal(worldPosition.relative(d), d);
            inv[d.get3DDataValue()] = Inventories.list(level, worldPosition.relative(d), d.getOpposite());
        }
        final long time = level.getGameTime();
        final boolean raining = level.isRaining();
        final BlockPos pos = worldPosition;

        LuaTable redstone = new LuaTable();
        redstone.set("get", new OneArgFunction() {
            @Override public LuaValue call(LuaValue side) { return LuaValue.valueOf(in[side(side)]); }
        });
        redstone.set("set", new TwoArgFunction() {
            @Override public LuaValue call(LuaValue side, LuaValue power) {
                int p = power.isboolean() ? (power.toboolean() ? 15 : 0) : Math.max(0, Math.min(15, power.checkint()));
                output[side(side)] = p;
                return LuaValue.NIL;
            }
        });
        g.set("redstone", redstone);

        LuaTable world = new LuaTable();
        world.set("time", new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(time); } });
        world.set("day", new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(time / 24000L); } });
        world.set("raining", new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(raining); } });
        world.set("pos", new ZeroArgFunction() {
            @Override public Varargs invoke() { return LuaValue.varargsOf(LuaValue.valueOf(pos.getX()), LuaValue.valueOf(pos.getY()), LuaValue.valueOf(pos.getZ())); }
            @Override public LuaValue call() { return LuaValue.valueOf(pos.getX()); }
        });
        g.set("world", world);

        LuaTable inventory = new LuaTable();
        inventory.set("list", new OneArgFunction() {
            @Override public LuaValue call(LuaValue side) {
                LuaTable out = new LuaTable();
                int n = 0;
                for (Inventories.Entry e : inv[side(side)]) {
                    LuaTable row = new LuaTable();
                    row.set("name", e.name());
                    row.set("count", e.count());
                    out.set(++n, row);
                }
                return out;
            }
        });
        g.set("inventory", inventory);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("program", program);
        out.putIntArray("output", output);
        out.putString("error", error);
        out.putInt("runs", runs);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        program = in.getStringOr("program", "");
        error = in.getStringOr("error", "");
        runs = in.getIntOr("runs", 0);
        in.getIntArray("output").ifPresent(a -> System.arraycopy(a, 0, output, 0, Math.min(6, a.length)));
    }
}
