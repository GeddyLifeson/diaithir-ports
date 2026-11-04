// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Routing over the pipe graph: connected pipes form a network, and a stack is sent to the sink pipe with the highest priority, then the nearest. */
public final class PipeNet {
    private PipeNet() {}

    static boolean isPipe(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos) && level.getBlockState(pos).getBlock() instanceof LogisticsPipeBlock;
    }

    static List<Long> neighbours(ServerLevel level, long at) {
        BlockPos p = BlockPos.of(at);
        List<Long> out = new ArrayList<>(6);
        for (Direction d : Direction.values()) {
            BlockPos n = p.relative(d);
            if (isPipe(level, n)) out.add(n.asLong());
        }
        return out;
    }

    public static Dijkstra discover(ServerLevel level, BlockPos origin) {
        return Dijkstra.discover(origin.asLong(), at -> neighbours(level, at), Config.MAX_PIPES.get());
    }

    /** Inventories touching a pipe, as handlers seen from the pipe's side. */
    static List<ResourceHandler<ItemResource>> inventories(ServerLevel level, BlockPos pipe) {
        List<ResourceHandler<ItemResource>> out = new ArrayList<>(6);
        for (Direction d : Direction.values()) {
            BlockPos n = pipe.relative(d);
            if (!level.isLoaded(n) || level.getBlockState(n).getBlock() instanceof LogisticsPipeBlock) continue;
            ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, n, d.getOpposite());
            if (h != null) out.add(h);
        }
        return out;
    }

    /** How many of the stack the inventories touching the pipe could take right now. Nothing is changed. */
    static int room(ServerLevel level, BlockPos pipe, ItemStack stack) {
        ItemResource r = ItemResource.of(stack);
        int total = 0;
        for (ResourceHandler<ItemResource> h : inventories(level, pipe)) {
            try (Transaction tx = Transaction.openRoot()) {
                total += h.insert(r, stack.getCount(), tx);
            }
        }
        return total;
    }

    /** Puts the stack into the inventories touching the pipe and returns how many went in. */
    static int insertInto(ServerLevel level, BlockPos pipe, ItemStack stack) {
        ItemResource r = ItemResource.of(stack);
        int left = stack.getCount();
        for (ResourceHandler<ItemResource> h : inventories(level, pipe)) {
            if (left <= 0) break;
            try (Transaction tx = Transaction.openRoot()) {
                int in = h.insert(r, left, tx);
                if (in > 0) {
                    tx.commit();
                    left -= in;
                }
            }
        }
        return stack.getCount() - left;
    }

    private record Candidate(PipeBlockEntity pipe, int distance) {}

    /** Sends as much of the stack as fits to sink pipes reachable from origin (not origin itself). Returns how many were sent. */
    public static int dispatch(ServerLevel level, BlockPos origin, ItemStack stack) {
        if (!(level.getBlockEntity(origin) instanceof PipeBlockEntity from) || stack.isEmpty()) return 0;
        Dijkstra d = discover(level, origin);
        List<Candidate> sinks = new ArrayList<>();
        for (var e : d.distances().entrySet()) {
            if (e.getKey() == origin.asLong()) continue;
            if (level.getBlockEntity(BlockPos.of(e.getKey())) instanceof PipeBlockEntity pbe && pbe.kind() == PipeKind.SINK && pbe.sink().wants(stack)) {
                sinks.add(new Candidate(pbe, e.getValue()));
            }
        }
        sinks.sort((a, b) -> a.pipe.sink().priority != b.pipe.sink().priority
                ? Integer.compare(b.pipe.sink().priority, a.pipe.sink().priority)
                : Integer.compare(a.distance, b.distance));
        int left = stack.getCount();
        for (Candidate c : sinks) {
            if (left <= 0) break;
            int amount = Math.min(left, room(level, c.pipe.getBlockPos(), stack.copyWithCount(left)));
            if (amount <= 0) continue;
            List<Long> path = d.route(c.pipe.getBlockPos().asLong());
            if (path.isEmpty()) continue;
            from.accept(new Traveller(stack.copyWithCount(amount), path, 0, 0, 0));
            left -= amount;
        }
        return stack.getCount() - left;
    }

    /** True if some sink pipe reachable from origin wants this stack and has room for at least one. Used by extractors before taking anything. */
    public static int capacityFor(ServerLevel level, BlockPos origin, ItemStack stack) {
        Dijkstra d = discover(level, origin);
        int total = 0;
        for (long key : d.distances().keySet()) {
            if (key == origin.asLong()) continue;
            if (level.getBlockEntity(BlockPos.of(key)) instanceof PipeBlockEntity pbe && pbe.kind() == PipeKind.SINK && pbe.sink().wants(stack)) {
                total += room(level, pbe.getBlockPos(), stack);
            }
        }
        return total;
    }

    static @Nullable PipeBlockEntity pipeAt(ServerLevel level, long pos) {
        BlockPos p = BlockPos.of(pos);
        return level.isLoaded(p) && level.getBlockEntity(p) instanceof PipeBlockEntity pbe ? pbe : null;
    }
}
