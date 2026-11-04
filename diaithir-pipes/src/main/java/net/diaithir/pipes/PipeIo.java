package net.diaithir.pipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Predicate;

/** Capability lookups and the two-step move: first learn how much fits, then move exactly that, so nothing is lost. */
final class PipeIo {
    private PipeIo() {
    }

    static boolean hasCapability(Level level, PipeKind kind, BlockPos pos, Direction side) {
        return switch (kind) {
            case ITEM -> level.getCapability(Capabilities.Item.BLOCK, pos, side) != null;
            case FLUID -> level.getCapability(Capabilities.Fluid.BLOCK, pos, side) != null;
            case ENERGY -> level.getCapability(Capabilities.Energy.BLOCK, pos, side) != null;
        };
    }

    /** Moves up to {@code budget} items from src to dst. Returns how many moved. */
    static long moveItems(ResourceHandler<ItemResource> src, ResourceHandler<ItemResource> dst, long budget, Predicate<ItemResource> allowed) {
        long moved = 0;
        for (int i = 0, n = src.size(); i < n && moved < budget; i++) {
            ItemResource r = src.getResource(i);
            long have = src.getAmountAsLong(i);
            if (r.isEmpty() || have <= 0 || !allowed.test(r)) continue;
            moved += transfer(src, dst, r, (int) Math.min(Math.min(have, budget - moved), Integer.MAX_VALUE));
        }
        return moved;
    }

    static long moveFluid(ResourceHandler<FluidResource> src, ResourceHandler<FluidResource> dst, long budget, Predicate<FluidResource> allowed) {
        long moved = 0;
        for (int i = 0, n = src.size(); i < n && moved < budget; i++) {
            FluidResource r = src.getResource(i);
            long have = src.getAmountAsLong(i);
            if (r.isEmpty() || have <= 0 || !allowed.test(r)) continue;
            moved += transfer(src, dst, r, (int) Math.min(Math.min(have, budget - moved), Integer.MAX_VALUE));
        }
        return moved;
    }

    private static <T extends net.neoforged.neoforge.transfer.resource.Resource> int transfer(ResourceHandler<T> src, ResourceHandler<T> dst, T r, int amount) {
        int fit;
        try (Transaction tx = Transaction.openRoot()) {
            int out = src.extract(r, amount, tx);
            fit = out <= 0 ? 0 : dst.insert(r, out, tx);
        }
        if (fit <= 0) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int out = src.extract(r, fit, tx);
            int in = out <= 0 ? 0 : dst.insert(r, out, tx);
            if (out == fit && in == fit) {
                tx.commit();
                return fit;
            }
        }
        return 0;
    }

    static long moveEnergy(EnergyHandler src, EnergyHandler dst, long budget) {
        int fit;
        try (Transaction tx = Transaction.openRoot()) {
            int out = src.extract((int) Math.min(budget, Integer.MAX_VALUE), tx);
            fit = out <= 0 ? 0 : dst.insert(out, tx);
        }
        if (fit <= 0) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int out = src.extract(fit, tx);
            int in = out <= 0 ? 0 : dst.insert(out, tx);
            if (out == fit && in == fit) {
                tx.commit();
                return fit;
            }
        }
        return 0;
    }
}
