package net.diaithir.chunkloaders;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.Team;

/** Burns fuel and, while it has some, holds a square of chunks loaded through the loader ticket controller. */
public final class LoaderBlockEntity extends BlockEntity {
    public String owner = "", ownerName = "";
    /** Fuel left, in loader ticks at this tier's own burn rate. */
    public long fuel;
    /** Radius of the tickets held now, or -1 when none are held. */
    private int held = -1;
    private boolean synced;

    public LoaderBlockEntity(BlockPos pos, BlockState state) {
        super(Registry.LOADER_ENTITY.get(), pos, state);
    }

    int tier() { return ((LoaderBlock) getBlockState().getBlock()).tier; }

    int cost() { return Config.COST[tier()].get(); }

    int radius() { return Config.RADIUS[tier()].get(); }

    long maxFuel() { return (long) Config.MAX_HOURS.get() * 72000L * cost(); }

    boolean holding() { return held >= 0; }

    int heldChunks() { return held < 0 ? 0 : (2 * held + 1) * (2 * held + 1); }

    long secondsLeft() { return fuel / cost() / 20; }

    /** The owner, a teammate of the owner, or an operator may fuel, break and pause the loader; an unowned loader is open to all. */
    boolean canManage(Player player) {
        if (owner.isEmpty() || owner.equals(player.getStringUUID())) return true;
        if (player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) return true;
        Team mine = player.getTeam();
        return mine != null && level != null && level.getScoreboard().getPlayersTeam(ownerName) instanceof Team theirs && theirs.getName().equals(mine.getName());
    }

    /** Adds burn ticks (vanilla fuel ticks); returns how many it took. */
    long addFuel(long vanillaBurnTicks) {
        long add = Math.min(vanillaBurnTicks * Config.FUEL_MULTIPLIER.get(), maxFuel() - fuel);
        if (add <= 0) return 0;
        fuel += add;
        setChanged();
        return add;
    }

    void claim(String uuid, String name) {
        owner = uuid;
        ownerName = name;
        setChanged();
        if (level instanceof ServerLevel sl) {
            LoaderData.get(sl.getServer()).add(new LoaderData.Entry(sl.dimension().identifier().toString(), worldPosition, tier(), owner, ownerName));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LoaderBlockEntity be) {
        if (level instanceof ServerLevel sl) be.serverTick(sl, state);
    }

    void serverTick(ServerLevel sl, BlockState state) {
        if (!synced) {
            synced = true;
            if (held >= 0) retain(sl, held);   // tickets persist, but make sure they match after a restart
        }
        if (fuel > 0) {
            if (held < 0) start(sl);
            fuel -= cost();
            if (fuel <= 0) {
                fuel = 0;
                stop(sl);
            }
            if (sl.getGameTime() % 100 == 0) setChanged();
        } else if (held >= 0) {
            stop(sl);
        }
        boolean lit = held >= 0;
        if (state.getValue(BlockStateProperties.LIT) != lit) sl.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, lit), 3);
    }

    void start(ServerLevel sl) {
        if (held >= 0) return;
        held = radius();
        retain(sl, held);
        setChanged();
    }

    void stop(ServerLevel sl) {
        if (held < 0) return;
        ChunkPos c = ChunkPos.containing(worldPosition);
        for (ChunkPos p : Tickets.around(c, held)) {
            // added with the natural-spawning flag when entityTicking is on (config may have changed since), so drop both kinds
            Tickets.BLOCKS.forceChunk(sl, worldPosition, p.x(), p.z(), false, true);
            Tickets.BLOCKS.forceChunk(sl, worldPosition, p.x(), p.z(), false, false);
        }
        held = -1;
        setChanged();
    }

    private void retain(ServerLevel sl, int radius) {
        ChunkPos c = ChunkPos.containing(worldPosition);
        boolean ticking = Config.ENTITY_TICKING.get();
        for (ChunkPos p : Tickets.around(c, radius)) Tickets.BLOCKS.forceChunk(sl, worldPosition, p.x(), p.z(), true, ticking);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel sl) {
            stop(sl);
            LoaderData.get(sl.getServer()).remove(sl.dimension().identifier().toString(), pos);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("owner", owner);
        out.putString("ownerName", ownerName);
        out.putLong("fuel", fuel);
        out.putInt("held", held);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        owner = in.getStringOr("owner", "");
        ownerName = in.getStringOr("ownerName", "");
        fuel = in.getLongOr("fuel", 0);
        held = in.getIntOr("held", -1);
    }
}
