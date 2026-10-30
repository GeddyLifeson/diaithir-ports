package com.otectus.immersivetanning.util;

import com.otectus.immersivetanning.network.packet.StationEffectPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Visual effects are sent as small packets to players tracking the chunk and spawned client-side, so each client can
 * honour its own particle settings.
 */
public final class StationEffects {
    public static final int SCRAPE = 0;
    public static final int STITCH = 1;
    public static final int SPLASH = 2;
    public static final int FINISHED = 3;

    public static void send(ServerLevel level, BlockPos pos, int type, float strength) {
        PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunkAt(pos).getPos(), new StationEffectPacket(pos, type, strength));
    }

    private StationEffects() {}
}
