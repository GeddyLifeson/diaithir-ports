package net.diaithir.computing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * What the Reckoner and the Turtle share: a program, the redstone it gives out, sleeping, wireless messages and writing to signs.
 * A program runs in slices (see Session). Anything that touches the world is a yield answered here on the server thread, and a
 * yield can also make the program wait: sleep, a turtle action, or a message.
 */
public abstract class ComputerEntity extends BlockEntity {
    static final int MAX_PROGRAM = 16_000, MAX_LOG = 20, MAX_LINE = 200, MAX_SLEEP = 1200, MAX_STEPS = 256;
    static final String[] SIDES = {"bottom", "top", "north", "south", "west", "east"};   // indexes match Direction.get3DDataValue()

    public String program = "";
    public final int[] output = new int[6];
    public List<String> log = new ArrayList<>();
    public String error = "";
    public int runs;

    Session session;
    int wait;
    boolean waitingNet;
    Varargs resumeWith = LuaValue.NONE;
    final Deque<Net.Msg> inbox = new ArrayDeque<>();
    final Set<Integer> listening = new HashSet<>();
    private final int[] inputs = new int[6];
    private long time;
    private boolean raining;
    /** Set when this entity has handed its running program to a new block entity (a turtle that moved). */
    boolean moved;
    /** What the running program's api talks to; a turtle that moves hands it to its new block entity. */
    static final class Cell { ComputerEntity e; Cell(ComputerEntity e) { this.e = e; } }
    Cell cell = new Cell(this);

    protected ComputerEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    static int side(LuaValue v) {
        String s = v.checkjstring().toLowerCase();
        for (int i = 0; i < 6; i++) if (SIDES[i].equals(s)) return i;
        throw new LuaError("unknown side '" + s + "' (bottom, top, north, south, west, east)");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) Net.add(this);
    }

    @Override
    public void setRemoved() {
        Net.remove(this);
        super.setRemoved();
    }

    public boolean setProgram(String text) {
        if (text.length() > MAX_PROGRAM) return false;
        program = text;
        setChanged();
        return true;
    }

    public boolean running() { return session != null; }

    public void stop() {
        session = null;
        wait = 0;
        waitingNet = false;
        setChanged();
    }

    /** Starts the program and runs it until it finishes or has to wait. Returns the error so far, or null. */
    public String run() {
        if (level == null || level.isClientSide()) return "not on a server";
        log.clear();
        error = "";
        runs++;
        wait = 0;
        waitingNet = false;
        resumeWith = LuaValue.NONE;
        cell = new Cell(this);
        Net.add(this);
        List<String> sink = log;
        session = Session.start(program, this::install, line -> {
            if (sink.size() < MAX_LOG) sink.add(line.length() > MAX_LINE ? line.substring(0, MAX_LINE) : line);
        });
        advance();
        return error.isEmpty() ? null : error;
    }

    /** Called every game tick by the block's ticker. */
    public void tickProgram() {
        if (session == null || level == null) return;
        if (waitingNet) {
            if (inbox.isEmpty()) return;
            waitingNet = false;
            resumeWith = message(inbox.poll());
        }
        if (wait > 0) { wait--; return; }
        advance();
    }

    private static Varargs message(Net.Msg m) {
        return LuaValue.varargsOf(LuaValue.valueOf(m.channel()), LuaValue.valueOf(m.text()), LuaValue.valueOf(m.distance()));
    }

    private void advance() {
        int[] before = output.clone();
        for (int step = 0; step < MAX_STEPS && session != null && wait == 0 && !waitingNet; step++) {
            refresh();
            Varargs y = session.resume(resumeWith);
            if (session.finished) {
                if (!session.error.isEmpty()) error = session.error;
                session = null;
                break;
            }
            resumeWith = handle(y);
            if (moved) return;   // the program now belongs to the block entity at the new position
        }
        if (session != null && wait == 0 && !waitingNet && error.isEmpty()) {
            // MAX_STEPS yields in one tick: let the server breathe, carry on next tick
        }
        setChanged();
        if (!Arrays.equals(before, output)) level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    private void refresh() {
        for (Direction d : Direction.values()) inputs[d.get3DDataValue()] = level.getSignal(worldPosition.relative(d), d);
        time = level.getGameTime();
        raining = level.isRaining();
    }

    private Varargs handle(Varargs y) {
        String act = y.arg1().tojstring();
        switch (act) {
            case "sleep": wait = Math.max(1, Math.min(MAX_SLEEP, y.arg(2).optint(1))); return LuaValue.NONE;
            case "net_wait":
                if (inbox.isEmpty()) { waitingNet = true; return LuaValue.NONE; }
                return message(inbox.poll());
            case "net_send": return LuaValue.valueOf(Net.send(this, y.arg(2).checkint(), y.arg(3).tojstring()));
            case "inv_list": return inventoryList(Direction.from3DDataValue(y.arg(2).checkint()));
            case "monitor": return writeSign(Direction.from3DDataValue(y.arg(2).checkint()), y.arg(3).checkint(), y.arg(4).tojstring());
            default: return action(act, y);
        }
    }

    /** Turtle actions; a Reckoner has none. */
    protected Varargs action(String act, Varargs y) { return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf("unknown action " + act)); }

    private Varargs inventoryList(Direction d) {
        LuaTable out = new LuaTable();
        int n = 0;
        for (Inventories.Entry e : Inventories.list(level, worldPosition.relative(d), d.getOpposite())) {
            LuaTable row = new LuaTable();
            row.set("name", e.name());
            row.set("count", e.count());
            out.set(++n, row);
        }
        return out;
    }

    private Varargs writeSign(Direction d, int line, String text) {
        if (line < 1 || line > 4) return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf("line 1 to 4"));
        if (!(level.getBlockEntity(worldPosition.relative(d)) instanceof SignBlockEntity sign)) return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf("no sign on that side"));
        String t = text.length() > 40 ? text.substring(0, 40) : text;
        sign.updateText(st -> st.setMessage(line - 1, Component.literal(t)), true);
        sign.setChanged();
        level.sendBlockUpdated(sign.getBlockPos(), sign.getBlockState(), sign.getBlockState(), 3);
        return LuaValue.TRUE;
    }

    /** The api every program gets. Reads come from values copied before the slice; anything that touches the world yields. */
    protected void install(Globals g) {
        final Cell c = cell;
        LuaTable redstone = new LuaTable();
        redstone.set("get", new OneArgFunction() {
            @Override public LuaValue call(LuaValue side) { return LuaValue.valueOf(c.e.inputs[side(side)]); }
        });
        redstone.set("set", new TwoArgFunction() {
            @Override public LuaValue call(LuaValue side, LuaValue power) {
                c.e.output[side(side)] = power.isboolean() ? (power.toboolean() ? 15 : 0) : Math.max(0, Math.min(15, power.checkint()));
                return LuaValue.NIL;
            }
        });
        g.set("redstone", redstone);

        LuaTable world = new LuaTable();
        world.set("time", new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(c.e.time); } });
        world.set("day", new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(c.e.time / 24000L); } });
        world.set("raining", new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(c.e.raining); } });
        world.set("pos", new Fn0() {
            @Override Varargs run() { BlockPos p = c.e.worldPosition; return LuaValue.varargsOf(LuaValue.valueOf(p.getX()), LuaValue.valueOf(p.getY()), LuaValue.valueOf(p.getZ())); }
        });
        g.set("world", world);

        LuaTable inventory = new LuaTable();
        inventory.set("list", new OneArgFunction() {
            @Override public LuaValue call(LuaValue side) { return g.yield(LuaValue.varargsOf(LuaValue.valueOf("inv_list"), LuaValue.valueOf(side(side)))).arg1(); }
        });
        g.set("inventory", inventory);

        g.set("sleep", new OneArgFunction() {
            @Override public LuaValue call(LuaValue ticks) { g.yield(LuaValue.varargsOf(LuaValue.valueOf("sleep"), ticks)); return LuaValue.NIL; }
        });

        LuaTable monitor = new LuaTable();   // writes to a sign on that side: monitor.write(side, line 1-4, text)
        monitor.set("write", new VarArgFunction() {
            @Override public Varargs invoke(Varargs a) {
                return g.yield(LuaValue.varargsOf(new LuaValue[]{LuaValue.valueOf("monitor"), LuaValue.valueOf(side(a.arg(1))), a.arg(2), LuaValue.valueOf(a.arg(3).tojstring())}));
            }
        });
        g.set("monitor", monitor);

        LuaTable net = new LuaTable();
        net.set("listen", new OneArgFunction() {
            @Override public LuaValue call(LuaValue ch) { c.e.listening.add(ch.checkint()); return LuaValue.NIL; }
        });
        net.set("unlisten", new OneArgFunction() {
            @Override public LuaValue call(LuaValue ch) { c.e.listening.remove(ch.checkint()); return LuaValue.NIL; }
        });
        net.set("send", new TwoArgFunction() {
            @Override public LuaValue call(LuaValue ch, LuaValue msg) { return g.yield(LuaValue.varargsOf(LuaValue.valueOf("net_send"), ch, LuaValue.valueOf(msg.tojstring()))).arg1(); }
        });
        net.set("receive", new Fn0() {
            @Override Varargs run() { Net.Msg m = c.e.inbox.poll(); return m == null ? LuaValue.NIL : message(m); }
        });
        net.set("wait", new Fn0() {
            @Override Varargs run() { return g.yield(LuaValue.valueOf("net_wait")); }
        });
        g.set("net", net);
        installExtra(g);
    }

    protected void installExtra(Globals g) {}

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("program", program);
        out.putIntArray("output", output);
        out.putString("error", error);
        out.putInt("runs", runs);
        out.putIntArray("listening", listening.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        program = in.getStringOr("program", "");
        error = in.getStringOr("error", "");
        runs = in.getIntOr("runs", 0);
        in.getIntArray("output").ifPresent(a -> System.arraycopy(a, 0, output, 0, Math.min(6, a.length)));
        listening.clear();
        in.getIntArray("listening").ifPresent(a -> { for (int c : a) listening.add(c); });
    }
}
