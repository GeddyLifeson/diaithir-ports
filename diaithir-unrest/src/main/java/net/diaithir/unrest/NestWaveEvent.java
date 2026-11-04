package net.diaithir.unrest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Posted on the NeoForge bus when a nest has absorbed enough Unrest to send an attack wave. The creature module (Burrowkin) listens,
 * spawns kinds for the nest's tier and aims them at target (the loudest chunk's centre). Cancel it to refuse the wave; nothing is spent then.
 */
public final class NestWaveEvent extends Event implements ICancellableEvent {
    private final ServerLevel level;
    private final BlockPos nest, target;
    private final int tier, groupSize;

    public NestWaveEvent(ServerLevel level, BlockPos nest, BlockPos target, int tier, int groupSize) {
        this.level = level;
        this.nest = nest;
        this.target = target;
        this.tier = tier;
        this.groupSize = groupSize;
    }

    public ServerLevel level() { return level; }
    public BlockPos nest() { return nest; }
    public BlockPos target() { return target; }
    public int tier() { return tier; }
    public int groupSize() { return groupSize; }
}
