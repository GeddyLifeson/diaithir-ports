package net.diaithir.computing;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.List;

/**
 * The Wayfarer's brain. Moving swaps the block: the block entity at the new position takes over the program, fuel and sixteen slots,
 * and the old block goes. A move costs a unit of fuel (one unit = one second of burn time of a fuel item) and eight ticks; dig and place five or ten.
 * Directions in the api: forward, back, left, right, up, down.
 */
public final class TurtleEntity extends ComputerEntity {
    static final int SLOTS = 16, MAX_FUEL = 20_000, MOVE_TICKS = 8, DIG_TICKS = 10, PLACE_TICKS = 5;
    final ItemStack[] slots = new ItemStack[SLOTS];
    int selected = 1;
    int fuel = 20;

    public TurtleEntity(BlockPos pos, BlockState state) {
        super(Registry.TURTLE_ENTITY.get(), pos, state);
        java.util.Arrays.fill(slots, ItemStack.EMPTY);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, TurtleEntity be) {
        if (!level.isClientSide()) be.tickProgram();
    }

    Direction facing() { return getBlockState().getValue(TurtleBlock.FACING); }

    Direction dir(String name) {
        Direction f = facing();
        return switch (name) {
            case "back" -> f.getOpposite();
            case "left" -> f.getCounterClockWise();
            case "right" -> f.getClockWise();
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            default -> f;
        };
    }

    private static Varargs fail(String why) { return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf(why)); }

    /** Puts a stack into the slots (selected first); returns what did not fit. */
    ItemStack store(ItemStack in) {
        ItemStack rest = in.copy();
        for (int pass = 0; pass < 2 && !rest.isEmpty(); pass++) {
            for (int k = 0; k < SLOTS && !rest.isEmpty(); k++) {
                int i = (selected - 1 + k) % SLOTS;
                ItemStack s = slots[i];
                if (pass == 0 && !s.isEmpty() && ItemStack.isSameItemSameComponents(s, rest)) {
                    int add = Math.min(rest.getCount(), s.getMaxStackSize() - s.getCount());
                    s.grow(add);
                    rest.shrink(add);
                } else if (pass == 1 && s.isEmpty()) {
                    slots[i] = rest.copy();
                    rest.setCount(0);
                }
            }
        }
        return rest;
    }

    @Override
    protected Varargs action(String act, Varargs y) {
        String d = y.arg(2).optjstring("forward");
        switch (act) {
            case "turn": {
                boolean left = d.equals("left");
                level.setBlock(worldPosition, getBlockState().setValue(TurtleBlock.FACING, left ? facing().getCounterClockWise() : facing().getClockWise()), 3);
                wait = 4;
                return LuaValue.TRUE;
            }
            case "move": return move(dir(d));
            case "dig": return dig(dir(d));
            case "place": return place(dir(d));
            case "detect": return LuaValue.valueOf(!level.getBlockState(worldPosition.relative(dir(d))).isAir());
            case "inspect": {
                BlockState s = level.getBlockState(worldPosition.relative(dir(d)));
                return s.isAir() ? fail("no block") : LuaValue.valueOf(BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString());
            }
            case "drop": return drop(dir(d), y.arg(3).optint(64));
            case "suck": return suck(dir(d));
            case "refuel": return refuel(y.arg(2).optint(64));
            default: return super.action(act, y);
        }
    }

    private Varargs move(Direction d) {
        if (fuel <= 0) return fail("out of fuel");
        BlockPos to = worldPosition.relative(d);
        if (!level.isLoaded(to) || !level.isInWorldBounds(to)) return fail("edge of the world");
        if (!level.getBlockState(to).canBeReplaced() || !level.getFluidState(to).isEmpty()) return fail("blocked");
        wait = MOVE_TICKS;
        fuel--;
        BlockPos from = worldPosition;
        level.setBlock(to, getBlockState(), 3);
        if (!(level.getBlockEntity(to) instanceof TurtleEntity ne)) { level.setBlock(to, Blocks.AIR.defaultBlockState(), 3); fuel++; return fail("could not move"); }
        ne.adopt(this);
        moved = true;
        level.setBlock(from, Blocks.AIR.defaultBlockState(), 3);
        return LuaValue.TRUE;
    }

    /** Takes over everything of a turtle that has just moved here. */
    void adopt(TurtleEntity o) {
        program = o.program;
        log = o.log;
        cell = o.cell;
        cell.e = this;
        Net.add(this);
        System.arraycopy(o.output, 0, output, 0, 6);
        error = o.error;
        runs = o.runs;
        listening.addAll(o.listening);
        inbox.addAll(o.inbox);
        System.arraycopy(o.slots, 0, slots, 0, SLOTS);
        java.util.Arrays.fill(o.slots, ItemStack.EMPTY);
        selected = o.selected;
        fuel = o.fuel;
        session = o.session;
        wait = o.wait;
        waitingNet = o.waitingNet;
        resumeWith = LuaValue.TRUE;
        o.session = null;
        setChanged();
    }

    private Varargs dig(Direction d) {
        BlockPos at = worldPosition.relative(d);
        BlockState s = level.getBlockState(at);
        if (s.isAir() || !level.getFluidState(at).isEmpty() && s.getCollisionShape(level, at).isEmpty()) return fail("nothing to dig");
        if (s.getDestroySpeed(level, at) < 0 || s.is(Registry.TURTLE.get()) || s.is(Registry.RECKONER.get())) return fail("cannot dig that");
        List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(s, (ServerLevel) level, at, level.getBlockEntity(at));
        level.destroyBlock(at, false);
        for (ItemStack st : drops) {
            ItemStack rest = store(st);
            if (!rest.isEmpty()) net.minecraft.world.level.block.Block.popResource(level, worldPosition, rest);
        }
        wait = DIG_TICKS;
        setChanged();
        return LuaValue.TRUE;
    }

    private Varargs place(Direction d) {
        ItemStack in = slots[selected - 1];
        if (in.isEmpty() || !(in.getItem() instanceof BlockItem bi)) return fail("select a block");
        BlockPos at = worldPosition.relative(d);
        if (!level.getBlockState(at).canBeReplaced() || !level.getFluidState(at).isEmpty() && !level.getBlockState(at).isAir()) return fail("something is in the way");
        BlockState st = bi.getBlock().defaultBlockState();
        if (!st.canSurvive(level, at)) return fail("cannot place there");
        level.setBlock(at, st, 3);
        in.shrink(1);
        wait = PLACE_TICKS;
        setChanged();
        return LuaValue.TRUE;
    }

    private Varargs drop(Direction d, int count) {
        ItemStack in = slots[selected - 1];
        if (in.isEmpty()) return fail("slot is empty");
        ItemStack out = in.split(Math.min(count, in.getCount()));
        BlockPos at = worldPosition.relative(d);
        ItemEntity e = new ItemEntity(level, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, out, 0, 0, 0);
        e.setDefaultPickUpDelay();
        level.addFreshEntity(e);
        wait = PLACE_TICKS;
        setChanged();
        return LuaValue.TRUE;
    }

    private Varargs suck(Direction d) {
        BlockPos at = worldPosition.relative(d);
        for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(0.4))) {
            ItemStack rest = store(e.getItem());
            if (rest.getCount() == e.getItem().getCount()) continue;
            if (rest.isEmpty()) e.discard(); else e.setItem(rest);
            wait = PLACE_TICKS;
            setChanged();
            return LuaValue.TRUE;
        }
        return fail("nothing to take");
    }

    private Varargs refuel(int count) {
        ItemStack in = slots[selected - 1];
        int burn = in.isEmpty() ? 0 : level.fuelValues().burnDuration(in) / 20;
        if (burn <= 0) return fail("not a fuel");
        int n = 0;
        while (n < count && !in.isEmpty() && fuel + burn <= MAX_FUEL) { in.shrink(1); fuel += burn; n++; }
        setChanged();
        return n == 0 ? fail("fuel is full") : LuaValue.valueOf(fuel);
    }

    @Override
    protected void installExtra(Globals g) {
        final Cell c = cell;
        LuaTable t = new LuaTable();
        String[] moves = {"forward", "back", "up", "down"};
        for (String m : moves) t.set(m, new Fn0() {
            @Override Varargs run() { return g.yield(LuaValue.varargsOf(LuaValue.valueOf("move"), LuaValue.valueOf(m))); }
        });
        t.set("turnLeft", new Fn0() { @Override Varargs run() { return g.yield(LuaValue.varargsOf(LuaValue.valueOf("turn"), LuaValue.valueOf("left"))); } });
        t.set("turnRight", new Fn0() { @Override Varargs run() { return g.yield(LuaValue.varargsOf(LuaValue.valueOf("turn"), LuaValue.valueOf("right"))); } });
        for (String a : new String[]{"dig", "place", "detect", "inspect", "suck", "drop"}) t.set(a, new VarArgFunction() {
            @Override public Varargs invoke(Varargs args) {
                return g.yield(LuaValue.varargsOf(new LuaValue[]{LuaValue.valueOf(a), LuaValue.valueOf(args.arg1().optjstring("forward")), args.arg(2)}));
            }
        });
        t.set("refuel", new VarArgFunction() {
            @Override public Varargs invoke(Varargs args) { return g.yield(LuaValue.varargsOf(LuaValue.valueOf("refuel"), args.arg1().isnil() ? LuaValue.valueOf(64) : args.arg1())); }
        });
        t.set("select", new OneArgFunction() {
            @Override public LuaValue call(LuaValue n) { int i = n.checkint(); if (i < 1 || i > SLOTS) return LuaValue.FALSE; ((TurtleEntity) c.e).selected = i; return LuaValue.TRUE; }
        });
        t.set("getCount", new VarArgFunction() {
            @Override public Varargs invoke(Varargs a) { TurtleEntity me = (TurtleEntity) c.e; int i = a.arg1().optint(me.selected); return LuaValue.valueOf(i < 1 || i > SLOTS ? 0 : me.slots[i - 1].getCount()); }
        });
        t.set("getItem", new VarArgFunction() {
            @Override public Varargs invoke(Varargs a) {
                TurtleEntity me = (TurtleEntity) c.e;
                int i = a.arg1().optint(me.selected);
                if (i < 1 || i > SLOTS || me.slots[i - 1].isEmpty()) return LuaValue.NIL;
                return LuaValue.valueOf(BuiltInRegistries.ITEM.getKey(me.slots[i - 1].getItem()).toString());
            }
        });
        t.set("getFuel", new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(((TurtleEntity) c.e).fuel); } });
        g.set("turtle", t);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (!moved && level != null && !level.isClientSide()) {
            for (ItemStack s : slots) if (!s.isEmpty()) net.minecraft.world.level.block.Block.popResource(level, pos, s);
            java.util.Arrays.fill(slots, ItemStack.EMPTY);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("fuel", fuel);
        out.putInt("selected", selected);
        for (int i = 0; i < SLOTS; i++) if (!slots[i].isEmpty()) out.store("slot" + i, ItemStack.OPTIONAL_CODEC, slots[i]);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        fuel = in.getIntOr("fuel", 20);
        selected = Math.max(1, Math.min(SLOTS, in.getIntOr("selected", 1)));
        for (int i = 0; i < SLOTS; i++) slots[i] = in.read("slot" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
