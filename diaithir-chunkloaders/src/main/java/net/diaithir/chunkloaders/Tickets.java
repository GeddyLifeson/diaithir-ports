package net.diaithir.chunkloaders;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The two NeoForge ticket controllers: one for loader blocks, one for carts. Tickets survive restarts, so they are validated on load. */
final class Tickets {
    static final TicketController BLOCKS = new TicketController(Identifier.fromNamespaceAndPath(DiaithirChunkLoaders.ID, "loaders"), (ServerLevel level, net.neoforged.neoforge.common.world.chunk.TicketHelper helper) -> {
        LoaderData data = LoaderData.get(level.getServer());
        String dim = level.dimension().identifier().toString();
        for (BlockPos pos : new ArrayList<>(helper.getBlockTickets().keySet())) {
            if (!data.has(dim, pos)) helper.removeAllTickets(pos);
        }
    });
    // A cart that was keeping chunks when the server stopped asks again when it loads, so stale tickets are dropped.
    static final TicketController CARTS = new TicketController(Identifier.fromNamespaceAndPath(DiaithirChunkLoaders.ID, "carts"), (ServerLevel level, net.neoforged.neoforge.common.world.chunk.TicketHelper helper) -> {
        for (UUID id : new ArrayList<>(helper.getEntityTickets().keySet())) helper.removeAllTickets(id);
    });

    private Tickets() {}

    /** The chunks within radius of a chunk, as a list of [x, z]. */
    static List<ChunkPos> around(ChunkPos centre, int radius) {
        List<ChunkPos> out = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) out.add(new ChunkPos(centre.x() + x, centre.z() + z));
        return out;
    }
}
