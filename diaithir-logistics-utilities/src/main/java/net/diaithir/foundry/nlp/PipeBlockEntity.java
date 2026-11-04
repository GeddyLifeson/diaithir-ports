// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/** Carries the stacks currently inside this pipe, and the sink settings of a sink pipe. Moves stacks one block per hop time. */
public final class PipeBlockEntity extends BlockEntity {
    private final List<Traveller> travellers = new ArrayList<>();
    private final SinkConfig sink = new SinkConfig();
    private final int offset;

    public PipeBlockEntity(BlockPos pos, BlockState state) {
        super(LogisticsMod.PIPE_BE.get(), pos, state);
        this.offset = Math.floorMod(pos.hashCode(), 20);
    }

    public PipeKind kind() {
        return getBlockState().getBlock() instanceof LogisticsPipeBlock p ? p.kind() : PipeKind.PLAIN;
    }

    public SinkConfig sink() {
        return sink;
    }

    public int travelling() {
        int n = 0;
        for (Traveller t : travellers) n += t.stack.getCount();
        return n;
    }

    void accept(Traveller t) {
        travellers.add(t);
        setChanged();
    }

    void serverTick(ServerLevel level) {
        serverTick(level, level.getGameTime());
    }

    void serverTick(ServerLevel level, long now) {
        if (kind() == PipeKind.EXTRACTOR && (now + offset) % Config.EXTRACT_INTERVAL.get() == 0) extract(level);
        if (travellers.isEmpty()) return;
        boolean changed = false;
        for (Traveller t : new ArrayList<>(travellers)) {
            if (t.movedAt == now || !travellers.contains(t)) continue;
            if (++t.progress < kind().hopTicks()) continue;
            changed = true;
            if (t.atEnd()) arrive(level, t);
            else hop(level, t, now);
        }
        if (changed) setChanged();
    }

    private void hop(ServerLevel level, Traveller t, long now) {
        BlockPos next = BlockPos.of(t.next());
        if (!level.isLoaded(next)) {
            t.progress = kind().hopTicks();
            return;
        }
        if (PipeNet.pipeAt(level, t.next()) instanceof PipeBlockEntity nb) {
            travellers.remove(t);
            t.idx++;
            t.progress = 0;
            t.movedAt = now;
            nb.accept(t);
        } else {
            reroute(level, t);
        }
    }

    private void arrive(ServerLevel level, Traveller t) {
        int in = PipeNet.insertInto(level, worldPosition, t.stack);
        t.stack.shrink(in);
        if (t.stack.isEmpty()) {
            travellers.remove(t);
            return;
        }
        t.progress = 0;
        t.tries = in > 0 ? 0 : t.tries + 1;
        if (t.tries >= Config.MAX_TRIES.get()) reroute(level, t);
    }

    /** The route is gone or the sink is full: look for another sink from here, spill what finds none. */
    private void reroute(ServerLevel level, Traveller t) {
        travellers.remove(t);
        int sent = PipeNet.dispatch(level, worldPosition, t.stack);
        t.stack.shrink(sent);
        if (!t.stack.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, t.stack);
        }
    }

    private void extract(ServerLevel level) {
        int budget = Config.EXTRACT_AMOUNT.get();
        List<ResourceHandler<ItemResource>> sources = PipeNet.inventories(level, worldPosition);
        java.util.Collections.shuffle(sources, new java.util.Random(level.getRandom().nextLong()));
        for (ResourceHandler<ItemResource> h : sources) {
            for (int i = 0, n = h.size(); i < n && budget > 0; i++) {
                ItemResource r = h.getResource(i);
                long have = h.getAmountAsLong(i);
                if (r.isEmpty() || have <= 0) continue;
                ItemStack probe = r.toStack((int) Math.min(have, budget));
                int fit = Math.min(probe.getCount(), PipeNet.capacityFor(level, worldPosition, probe));
                if (fit <= 0) continue;
                int took;
                try (Transaction tx = Transaction.openRoot()) {
                    took = h.extract(r, fit, tx);
                    if (took > 0) tx.commit();
                }
                if (took <= 0) continue;
                ItemStack out = r.toStack(took);
                int sent = PipeNet.dispatch(level, worldPosition, out);
                budget -= sent;
                out.shrink(sent);
                if (!out.isEmpty()) {
                    try (Transaction tx = Transaction.openRoot()) {
                        int back = h.insert(r, out.getCount(), tx);
                        tx.commit();
                        out.shrink(back);
                    }
                    if (!out.isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, out);
                }
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null) return;
        for (Traveller t : travellers) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, t.stack);
        travellers.clear();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        var list = out.list("Travel", Traveller.CODEC);
        for (Traveller t : travellers) list.add(t);
        out.putInt("SinkPriority", sink.priority);
        out.putBoolean("SinkAll", sink.all);
        out.store("SinkItems", Identifier.CODEC.listOf(), new ArrayList<>(sink.items));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        travellers.clear();
        in.listOrEmpty("Travel", Traveller.CODEC).forEach(travellers::add);
        sink.priority = in.getIntOr("SinkPriority", SinkConfig.DEFAULT_PRIORITY);
        sink.all = in.getBooleanOr("SinkAll", false);
        sink.items.clear();
        in.read("SinkItems", Identifier.CODEC.listOf()).ifPresent(l -> l.stream().filter(BuiltInRegistries.ITEM::containsKey).forEach(sink.items::add));
    }
}
