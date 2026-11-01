package net.diaithir.conduits;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A conduit node. It sits on one channel (owner plus number). Each of its six faces is off, send or receive:
 * a send face pulls from the block next to it (and also accepts from pipes) and delivers to the receive faces of every other node on the channel,
 * wherever they are; a receive face pushes what arrives into the block next to it. Items, fluids and FE can each be switched off.
 * A tier's bandwidth is the most each kind can leave this node per run (config), shared by active pulling and by pipes feeding it.
 */
public final class ConduitBlockEntity extends BlockEntity {
    public static final int OFF = 0, SEND = 1, RECEIVE = 2;
    public static final int ITEMS = 0, FLUIDS = 1, ENERGY = 2;
    public static final String[] KIND_NAMES = {"items", "fluids", "energy"};
    private static boolean delivering;

    String owner = "", ownerName = "";
    String chanOwner = "";
    int channel = -1;
    final int[] modes = new int[6];
    final boolean[] kinds = {true, true, true};
    private int age;

    /** How much of each kind has left this node in the current run. Rolled back with a failed transaction. */
    private final class Used extends SnapshotJournal<int[]> {
        final int[] v = new int[3];

        @Override
        protected int[] createSnapshot() {
            return v.clone();
        }

        @Override
        protected void revertToSnapshot(int[] s) {
            System.arraycopy(s, 0, v, 0, 3);
        }
    }

    private final Used used = new Used();

    public ConduitBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirConduits.CONDUIT_BE.get(), pos, state);
    }

    public int tier() {
        return getBlockState().getBlock() instanceof ConduitBlock b ? b.tier : 1;
    }

    public int budget(int kind) {
        int base = kind == ITEMS ? Config.ITEMS_PER_RUN.get() : kind == FLUIDS ? Config.FLUID_PER_RUN.get() : Config.ENERGY_PER_RUN.get();
        long v = (long) base * Config.mult(tier());
        return (int) Math.min(v, Integer.MAX_VALUE);
    }

    public int left(int kind) {
        return Math.max(0, budget(kind) - used.v[kind]);
    }

    public int mode(Direction d) {
        return modes[d.get3DDataValue()];
    }

    public int channel() {
        return channel;
    }

    public String channelOwner() {
        return chanOwner;
    }

    public String owner() {
        return owner;
    }

    public void claim(Player p) {
        owner = p.getStringUUID();
        ownerName = p.getGameProfile().name();
        setChanged();
    }

    public static boolean isOp(Player p) {
        return Config.OPS_MANAGE_ALL.get() && p.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    public boolean canManage(Player p) {
        return owner.isEmpty() || owner.equals(p.getStringUUID()) || isOp(p);
    }

    public String setMode(Direction d, int mode) {
        modes[d.get3DDataValue()] = mode;
        invalidateCapabilities();
        setChanged();
        return d.getName() + ": " + modeName(mode);
    }

    public static String modeName(int m) {
        return m == SEND ? "send" : m == RECEIVE ? "receive" : "off";
    }

    public void setKind(int kind, boolean on) {
        kinds[kind] = on;
        invalidateCapabilities();
        setChanged();
    }

    /** Puts the node on a channel. Returns null on success or why it was refused. Only the node's owner (or an operator) may do this. */
    public @Nullable String join(ServerLevel level, String channelOwner, int number, Player by) {
        if (!canManage(by)) return "This node belongs to " + ownerName + ".";
        String err = Channels.get(level.getServer()).join(channelOwner, number, level.dimension().identifier().toString(), worldPosition, by.getStringUUID(), isOp(by));
        if (err != null) return err;
        chanOwner = channelOwner;
        channel = number;
        setChanged();
        return null;
    }

    public @Nullable String leave(ServerLevel level, Player by) {
        if (!canManage(by)) return "This node belongs to " + ownerName + ".";
        Channels.get(level.getServer()).leave(level.dimension().identifier().toString(), worldPosition);
        channel = -1;
        chanOwner = "";
        setChanged();
        return null;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel sl) Channels.get(sl.getServer()).leave(sl.dimension().identifier().toString(), pos);
        super.preRemoveSideEffects(pos, state);
    }

    // ---- moving --------------------------------------------------------------------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, ConduitBlockEntity be) {
        if (be.channel < 0 || !(level instanceof ServerLevel sl)) return;
        if (++be.age % Config.INTERVAL.get() == 0) be.work(sl);
    }

    public void newWindow() {
        java.util.Arrays.fill(used.v, 0);
    }

    /** One run: starts a fresh bandwidth window, then pulls from every send face. Returns how many units moved (all kinds). */
    public int work(ServerLevel level) {
        newWindow();
        if (channel < 0) return 0;
        int moved = 0;
        for (Direction d : Direction.values()) {
            if (modes[d.get3DDataValue()] != SEND) continue;
            BlockPos np = worldPosition.relative(d);
            if (!level.isLoaded(np)) continue;
            Direction from = d.getOpposite();
            if (kinds[ITEMS] && left(ITEMS) > 0) {
                ResourceHandler<ItemResource> src = level.getCapability(Capabilities.Item.BLOCK, np, from);
                if (src != null) moved += count(ITEMS, Moves.move(src, receivers(level, ITEMS, Capabilities.Item.BLOCK), left(ITEMS)));
            }
            if (kinds[FLUIDS] && left(FLUIDS) > 0) {
                ResourceHandler<FluidResource> src = level.getCapability(Capabilities.Fluid.BLOCK, np, from);
                if (src != null) moved += count(FLUIDS, Moves.move(src, receivers(level, FLUIDS, Capabilities.Fluid.BLOCK), left(FLUIDS)));
            }
            if (kinds[ENERGY] && left(ENERGY) > 0) {
                EnergyHandler src = level.getCapability(Capabilities.Energy.BLOCK, np, from);
                if (src != null) moved += count(ENERGY, Moves.moveEnergy(src, receivingEnergy(level), left(ENERGY)));
            }
        }
        return moved;
    }

    private int count(int kind, int n) {
        used.v[kind] += n;
        return n;
    }

    /** Every other node on this channel that can be reached right now (its chunk is loaded), in any dimension. */
    private List<ConduitBlockEntity> peers(ServerLevel level) {
        List<ConduitBlockEntity> out = new ArrayList<>();
        if (channel < 0 || level.getServer() == null) return out;
        String here = level.dimension().identifier().toString();
        for (Channels.Entry e : Channels.get(level.getServer()).on(chanOwner, channel)) {
            if (e.dim().equals(here) && e.pos().equals(worldPosition)) continue;
            ServerLevel other = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(e.dim())));
            if (other == null || !other.isLoaded(e.pos())) continue;
            if (other.getBlockEntity(e.pos()) instanceof ConduitBlockEntity c && c.channel == channel && c.chanOwner.equals(chanOwner)) out.add(c);
        }
        return out;
    }

    private <T extends Resource> List<ResourceHandler<T>> receivers(ServerLevel level, int kind, BlockCapability<ResourceHandler<T>, Direction> cap) {
        List<ResourceHandler<T>> out = new ArrayList<>();
        for (ConduitBlockEntity c : peers(level)) {
            if (!c.kinds[kind] || !(c.level instanceof ServerLevel cl)) continue;
            for (Direction d : Direction.values()) {
                if (c.modes[d.get3DDataValue()] != RECEIVE) continue;
                BlockPos np = c.worldPosition.relative(d);
                if (!cl.isLoaded(np)) continue;
                ResourceHandler<T> h = cl.getCapability(cap, np, d.getOpposite());
                if (h != null) out.add(h);
            }
        }
        return out;
    }

    private List<EnergyHandler> receivingEnergy(ServerLevel level) {
        List<EnergyHandler> out = new ArrayList<>();
        for (ConduitBlockEntity c : peers(level)) {
            if (!c.kinds[ENERGY] || !(c.level instanceof ServerLevel cl)) continue;
            for (Direction d : Direction.values()) {
                if (c.modes[d.get3DDataValue()] != RECEIVE) continue;
                BlockPos np = c.worldPosition.relative(d);
                if (!cl.isLoaded(np)) continue;
                EnergyHandler h = cl.getCapability(Capabilities.Energy.BLOCK, np, d.getOpposite());
                if (h != null) out.add(h);
            }
        }
        return out;
    }

    // ---- capabilities (pipes and machines may push into a send face) --------------------------------------------------------------

    private boolean accepts(int kind, @Nullable Direction side) {
        if (channel < 0 || !kinds[kind]) return false;
        if (side != null) return modes[side.get3DDataValue()] == SEND;
        for (int m : modes) if (m == SEND) return true;
        return false;
    }

    private <T extends Resource> int push(int kind, BlockCapability<ResourceHandler<T>, Direction> cap, T r, int amount, TransactionContext tx) {
        if (delivering || !(level instanceof ServerLevel sl)) return 0;
        int lim = Math.min(amount, left(kind));
        if (lim <= 0) return 0;
        delivering = true;
        try {
            int total = 0;
            for (ResourceHandler<T> h : receivers(sl, kind, cap)) {
                total += h.insert(r, lim - total, tx);
                if (total >= lim) break;
            }
            if (total > 0) {
                used.updateSnapshots(tx);
                used.v[kind] += total;
            }
            return total;
        } finally {
            delivering = false;
        }
    }

    public @Nullable ResourceHandler<ItemResource> itemSink(@Nullable Direction side) {
        return accepts(ITEMS, side) ? new Moves.Sink<>(ItemResource.EMPTY, (r, n, tx) -> push(ITEMS, Capabilities.Item.BLOCK, r, n, tx)) : null;
    }

    public @Nullable ResourceHandler<FluidResource> fluidSink(@Nullable Direction side) {
        return accepts(FLUIDS, side) ? new Moves.Sink<>(FluidResource.EMPTY, (r, n, tx) -> push(FLUIDS, Capabilities.Fluid.BLOCK, r, n, tx)) : null;
    }

    public @Nullable EnergyHandler energySink(@Nullable Direction side) {
        if (!accepts(ENERGY, side)) return null;
        return new Moves.EnergySink((amount, tx) -> {
            if (delivering || !(level instanceof ServerLevel sl)) return 0;
            int lim = Math.min(amount, left(ENERGY));
            if (lim <= 0) return 0;
            delivering = true;
            try {
                int total = 0;
                for (EnergyHandler h : receivingEnergy(sl)) {
                    total += h.insert(lim - total, tx);
                    if (total >= lim) break;
                }
                if (total > 0) {
                    used.updateSnapshots(tx);
                    used.v[ENERGY] += total;
                }
                return total;
            } finally {
                delivering = false;
            }
        });
    }

    // ---- saving --------------------------------------------------------------------------------------------------------------------

    public String describe(Channels ch) {
        StringBuilder sb = new StringBuilder("Tier " + tier() + ", ");
        sb.append(channel < 0 ? "no channel" : "channel " + channel + " of " + ch.nameOf(chanOwner));
        sb.append(", faces");
        for (Direction d : Direction.values()) sb.append(' ').append(d.getName().charAt(0)).append('=').append(modeName(modes[d.get3DDataValue()]).charAt(0));
        sb.append(", per run ").append(budget(ITEMS)).append(" items ").append(budget(FLUIDS)).append(" mB ").append(budget(ENERGY)).append(" FE");
        for (int k = 0; k < 3; k++) if (!kinds[k]) sb.append(", ").append(KIND_NAMES[k]).append(" off");
        return sb.toString();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("owner", owner);
        out.putString("ownerName", ownerName);
        out.putString("chanOwner", chanOwner);
        out.putInt("channel", channel);
        out.putIntArray("modes", modes);
        int k = 0;
        for (int i = 0; i < 3; i++) if (kinds[i]) k |= 1 << i;
        out.putInt("kinds", k);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        owner = in.getStringOr("owner", "");
        ownerName = in.getStringOr("ownerName", "");
        chanOwner = in.getStringOr("chanOwner", "");
        channel = in.getIntOr("channel", -1);
        in.getIntArray("modes").ifPresent(a -> {
            for (int i = 0; i < 6 && i < a.length; i++) modes[i] = Math.floorMod(a[i], 3);
        });
        int k = in.getIntOr("kinds", 7);
        for (int i = 0; i < 3; i++) kinds[i] = (k & (1 << i)) != 0;
    }
}
