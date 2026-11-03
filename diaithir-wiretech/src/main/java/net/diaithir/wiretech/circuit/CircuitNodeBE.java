package net.diaithir.wiretech.circuit;

import net.diaithir.wiretech.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** One circuit node: its program, its wires and what it last saw and said. The grid steps every node of a level together. */
public final class CircuitNodeBE extends BlockEntity {
    public static final int MAX_LINKS = 8;

    /** A wire end on this node. selfOut says which port of this node, otherOut which port of the far node. tier is the coil that made it. */
    public record Link(boolean selfOut, Color color, BlockPos other, boolean otherOut, Tier tier) {}

    public final CircuitKind kind;
    public final Tier tier;
    private final List<Link> links = new ArrayList<>();
    private String program;
    private Logic logic;
    Signals out = new Signals();
    In seen = In.NONE;
    private long prevEnergy = -1, prevTick;

    public CircuitNodeBE(BlockPos pos, BlockState state) {
        super(Machines.CIRCUIT_BE.get(), pos, state);
        CircuitBlock b = (CircuitBlock) state.getBlock();
        this.kind = b.kind;
        this.tier = b.tier;
        if (kind.defaultProgram == null) {
            this.logic = new Logic.Constant(new Signals());
            this.program = "const";
        } else {
            setProgramQuiet(kind.defaultProgram);
        }
    }

    public long id() {
        return worldPosition.asLong();
    }

    public List<Link> links() {
        return links;
    }

    public Logic logic() {
        return logic;
    }

    public String program() {
        return program;
    }

    public boolean takesPrograms() {
        return kind != CircuitKind.READER;
    }

    private void setProgramQuiet(String text) {
        Logic l = Logic.parse(text);
        if (!fits(l)) throw new IllegalArgumentException("A " + kind.display + " cannot run '" + text + "'.");
        this.logic = l;
        this.program = l.text();
    }

    private boolean fits(Logic l) {
        return switch (kind) {
            case CONSTANT -> l instanceof Logic.Constant;
            case ARITHMETIC -> l instanceof Logic.Arith;
            case DECIDER -> l instanceof Logic.Decider;
            case SELECTOR -> l instanceof Logic.Selector;
            case GATE, LAMP, SPEAKER -> l instanceof Logic.When;
            case READER -> false;
        };
    }

    /** Sets the program from text; throws IllegalArgumentException with the reason when it does not parse or does not fit this block. */
    public void setProgram(String text) {
        setProgramQuiet(text);
        changed();
    }

    public String describe() {
        return kind.display + " (" + tier.id + ", every " + String.format("%.2f", tier.period / 20.0) + " s, signals up to " + tier.maxValue + ")"
                + (takesPrograms() ? "\nProgram: " + program : "\nReads the block behind it.")
                + "\nSees red " + seen.red() + " green " + seen.green() + "\nSays " + out;
    }

    /** What a screen shows. msg is an error or a short note, resetProgram tells the screen to replace the text in its box. */
    public CircuitPayloads.View view(String msg, boolean resetProgram) {
        return new CircuitPayloads.View(worldPosition, program, seen.red().pretty(), seen.green().pretty(), out.pretty(), msg, resetProgram);
    }

    /** Programs this node for a player. Returns the reply for their screen. */
    public CircuitPayloads.View applyFrom(String text) {
        if (!takesPrograms()) return view("This block has no program.", false);
        try {
            setProgram(text.trim());
            return view("Applied.", true);
        } catch (IllegalArgumentException e) {
            return view(e.getMessage(), false);
        }
    }

    // ---- wiring ------------------------------------------------------------------------------------------------------------------

    /** Joins a port of one node to a port of another. Returns null on success or the reason it failed. */
    public static @Nullable String connect(ServerLevel level, BlockPos a, boolean aOut, BlockPos b, boolean bOut, Color color, Tier coil) {
        if (a.equals(b) && aOut == bOut) return "A wire needs two different terminals.";
        if (!(level.getBlockEntity(a) instanceof CircuitNodeBE na) || !(level.getBlockEntity(b) instanceof CircuitNodeBE nb)) return "That is not a circuit block.";
        if (aOut && !na.kind.hasOutput || !aOut && !na.kind.hasInput) return "The " + na.kind.display + " has no " + (aOut ? "output" : "input") + " terminal.";
        if (bOut && !nb.kind.hasOutput || !bOut && !nb.kind.hasInput) return "The " + nb.kind.display + " has no " + (bOut ? "output" : "input") + " terminal.";
        if (coil.ordinal() < na.tier.ordinal() || coil.ordinal() < nb.tier.ordinal()) return "A " + coil.id + " wire is too coarse for that block; use at least its own tier.";
        if (na.has(color, aOut, b, bOut)) return "Those terminals are already wired together in " + color.name().toLowerCase() + ".";
        if (na.links.size() >= MAX_LINKS || nb.links.size() >= MAX_LINKS) return "A circuit block holds at most " + MAX_LINKS + " wires.";
        double len = net.minecraft.world.phys.Vec3.atCenterOf(a).distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(b));
        if (len > coil.range) return String.format("Too far: %.1f m, a %s wire reaches %d m.", len, coil.id, coil.range);
        na.links.add(new Link(aOut, color, b.immutable(), bOut, coil));
        nb.links.add(new Link(bOut, color, a.immutable(), aOut, coil));
        na.changed();
        nb.changed();
        return null;
    }

    private boolean has(Color c, boolean selfOut, BlockPos other, boolean otherOut) {
        return links.stream().anyMatch(l -> l.color == c && l.selfOut == selfOut && l.other.equals(other) && l.otherOut == otherOut);
    }

    private void dropLinksTo(BlockPos other) {
        links.removeIf(l -> l.other.equals(other));
        changed();
    }

    void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /** Called whenever the block entity is placed or its chunk loads, so this is where the node joins the grid. */
    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level instanceof ServerLevel sl) CircuitGrid.add(sl, this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel sl) CircuitGrid.remove(sl, this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel sl)) return;
        for (Link l : new ArrayList<>(links)) {
            if (sl.getBlockEntity(l.other) instanceof CircuitNodeBE peer) peer.dropLinksTo(pos);
            Block.popResource(sl, pos, new ItemStack(Machines.CIRCUIT_COILS.get(Machines.coilKey(l.color, l.tier)).get()));
        }
        links.clear();
    }

    // ---- stepping ----------------------------------------------------------------------------------------------------------------

    /** What this node says next. Acting kinds set their block state and say nothing. */
    Signals compute(ServerLevel level, In in, long gameTime) {
        seen = in;
        if (kind == CircuitKind.READER) return read(level, gameTime);
        Logic.Ctx ctx = new Logic.Ctx(gameTime, worldPosition.asLong(), id -> {
            var item = BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.Identifier.parse(id));
            return item.map(i -> new ItemStack(i).getMaxStackSize()).orElse(64);
        });
        if (kind.acts()) {
            boolean on = ((Logic.When) logic).test(in);
            act(level, on, in);
            return Signals.EMPTY;
        }
        return logic.output(in, ctx);
    }

    private void act(ServerLevel level, boolean on, In in) {
        BlockState st = getBlockState();
        boolean was = st.getValue(CircuitBlock.LIT);
        if (was == on) return;
        level.setBlock(worldPosition, st.setValue(CircuitBlock.LIT, on), 3);
        if (kind == CircuitKind.GATE) level.updateNeighborsAt(worldPosition, st.getBlock());
        if (kind == CircuitKind.SPEAKER && on) {
            Cond first = ((Logic.When) logic).cond().clauses().get(0).get(0);
            int v = first.left().isConstant() || first.left().isWild() ? 12 : first.left().value(in);
            float pitch = (float) Math.max(0.5, Math.min(2.0, Math.pow(2.0, (Math.max(0, Math.min(24, v)) - 12) / 12.0)));
            level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 3.0F, pitch);
        }
    }

    /** Reads the block behind: items by id, fluids by id in litres, energy in joules (1 FE = 1 J) and net charging power in watts (20 ticks = 1 s). */
    private Signals read(ServerLevel level, long gameTime) {
        Signals r = new Signals();
        Direction face = getBlockState().getValue(CircuitBlock.FACING);
        BlockPos at = worldPosition.relative(face.getOpposite());
        ResourceHandler<ItemResource> items = level.getCapability(Capabilities.Item.BLOCK, at, face);
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                ItemResource res = items.getResource(i);
                long n = items.getAmountAsLong(i);
                if (!res.isEmpty() && n > 0) r.add(BuiltInRegistries.ITEM.getKey(res.getItem()).toString(), n);
            }
        }
        ResourceHandler<FluidResource> fluids = level.getCapability(Capabilities.Fluid.BLOCK, at, face);
        if (fluids != null) {
            for (int i = 0; i < fluids.size(); i++) {
                FluidResource res = fluids.getResource(i);
                long n = fluids.getAmountAsLong(i);
                if (!res.isEmpty() && n > 0) r.add(BuiltInRegistries.FLUID.getKey(res.getFluid()).toString(), n);
            }
        }
        readWorld(level, at, face, r);
        if (level.getBlockEntity(at) instanceof BlockEntity target) {
            var key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(target.getType());
            if (key != null && key.getNamespace().equals("belts")) BeltReading.read(target.saveWithoutMetadata(level.registryAccess()), r);
        }
        var energy = level.getCapability(Capabilities.Energy.BLOCK, at, face);
        if (energy != null) {
            long now = energy.getAmountAsLong();
            r.set("si:energy_j", now);
            r.set("si:capacity_j", energy.getCapacityAsLong());
            if (prevEnergy >= 0 && gameTime > prevTick) r.set("si:power_w", (now - prevEnergy) * 20 / (gameTime - prevTick));
            prevEnergy = now;
            prevTick = gameTime;
        } else {
            prevEnergy = -1;
        }
        return r;
    }

    /**
     * Signals any block can give with no code of its own: comparator output (si:analog, 0 to 15), integer and boolean block state properties
     * (state:powered, state:delay, state:lit...), and for a rail the carts on it (si:vehicles, si:speed_mps, and the items in a chest cart),
     * which makes station rails, signals and train stops readable.
     */
    private static void readWorld(ServerLevel level, BlockPos at, Direction face, Signals r) {
        BlockState bs = level.getBlockState(at);
        if (bs.hasAnalogOutputSignal()) r.set("si:analog", bs.getAnalogOutputSignal(level, at, face));
        for (net.minecraft.world.level.block.state.properties.Property<?> p : bs.getProperties()) {
            Object v = bs.getValue(p);
            if (v instanceof Integer i) r.set("state:" + p.getName(), i);
            else if (v instanceof Boolean b) r.set("state:" + p.getName(), b ? 1 : 0);
        }
        if (bs.getBlock() instanceof net.minecraft.world.level.block.BaseRailBlock) {
            var carts = level.getEntitiesOfClass(net.minecraft.world.entity.vehicle.minecart.AbstractMinecart.class, new net.minecraft.world.phys.AABB(at));
            r.set("si:vehicles", carts.size());
            double fastest = 0;
            for (var c : carts) {
                fastest = Math.max(fastest, c.getDeltaMovement().horizontalDistance() * 20.0);
                if (c instanceof net.minecraft.world.Container ct) {
                    for (int i = 0; i < ct.getContainerSize(); i++) {
                        ItemStack st = ct.getItem(i);
                        if (!st.isEmpty()) r.add(BuiltInRegistries.ITEM.getKey(st.getItem()).toString(), st.getCount());
                    }
                }
            }
            r.set("si:speed_mps", Math.round(fastest));
        }
    }

    // ---- save, load, client sync -------------------------------------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput o) {
        super.saveAdditional(o);
        o.putString("program", program);
        ValueOutput.ValueOutputList list = o.childrenList("links");
        for (Link l : links) {
            ValueOutput c = list.addChild();
            c.putInt("color", l.color.ordinal());
            c.putBoolean("self_out", l.selfOut);
            c.putBoolean("other_out", l.otherOut);
            c.putInt("tier", l.tier.ordinal());
            c.putIntArray("p", new int[] {l.other.getX(), l.other.getY(), l.other.getZ()});
        }
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        if (takesPrograms()) {
            try {
                setProgramQuiet(in.getStringOr("program", program));
            } catch (IllegalArgumentException e) {
                // keep the default program when a saved one no longer parses
            }
        }
        links.clear();
        for (ValueInput c : in.childrenListOrEmpty("links")) {
            c.getIntArray("p").ifPresent(a -> {
                if (a.length == 3) {
                    links.add(new Link(c.getBooleanOr("self_out", false), Color.values()[Math.floorMod(c.getIntOr("color", 0), 2)],
                            new BlockPos(a[0], a[1], a[2]), c.getBooleanOr("other_out", false), Tier.values()[Math.floorMod(c.getIntOr("tier", 0), 3)]));
                }
            });
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
