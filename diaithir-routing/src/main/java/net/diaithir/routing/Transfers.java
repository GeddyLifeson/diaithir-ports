package net.diaithir.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Predicate;

/** Shared movement code for routers, laser nodes and hoppers. Every move is two-step: measure what fits, then move exactly that, so nothing is lost. */
public final class Transfers {
    private Transfers() {
    }

    public static ResourceHandler<ItemResource> items(ServerLevel level, BlockPos pos, Direction side) {
        return level.isLoaded(pos) ? level.getCapability(Capabilities.Item.BLOCK, pos, side) : null;
    }

    public static ResourceHandler<FluidResource> fluids(ServerLevel level, BlockPos pos, Direction side) {
        return level.isLoaded(pos) ? level.getCapability(Capabilities.Fluid.BLOCK, pos, side) : null;
    }

    public static EnergyHandler energy(ServerLevel level, BlockPos pos, Direction side) {
        return level.isLoaded(pos) ? level.getCapability(Capabilities.Energy.BLOCK, pos, side) : null;
    }

    /** Moves up to max items from src to dst that the predicate accepts. Returns how many moved. */
    public static int moveItems(ResourceHandler<ItemResource> src, ResourceHandler<ItemResource> dst, int max, Predicate<ItemStack> accept) {
        int moved = 0;
        for (int i = 0, n = src.size(); i < n && moved < max; i++) {
            ItemResource r = src.getResource(i);
            long have = src.getAmountAsLong(i);
            if (r.isEmpty() || have <= 0 || !accept.test(r.toStack(1))) continue;
            moved += moveOne(src, dst, r, (int) Math.min(have, max - moved));
        }
        return moved;
    }

    private static int moveOne(ResourceHandler<ItemResource> src, ResourceHandler<ItemResource> dst, ItemResource r, int amount) {
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

    /** Destroys up to max matching items from src. Returns how many were destroyed. */
    public static int voidItems(ResourceHandler<ItemResource> src, int max, Predicate<ItemStack> accept) {
        int gone = 0;
        for (int i = 0, n = src.size(); i < n && gone < max; i++) {
            ItemResource r = src.getResource(i);
            long have = src.getAmountAsLong(i);
            if (r.isEmpty() || have <= 0 || !accept.test(r.toStack(1))) continue;
            try (Transaction tx = Transaction.openRoot()) {
                int out = src.extract(r, (int) Math.min(have, max - gone), tx);
                if (out > 0) {
                    tx.commit();
                    gone += out;
                }
            }
        }
        return gone;
    }

    public static int moveFluid(ResourceHandler<FluidResource> src, ResourceHandler<FluidResource> dst, int max) {
        for (int i = 0, n = src.size(); i < n; i++) {
            FluidResource r = src.getResource(i);
            long have = src.getAmountAsLong(i);
            if (r.isEmpty() || have <= 0) continue;
            int amount = (int) Math.min(have, max);
            int fit;
            try (Transaction tx = Transaction.openRoot()) {
                int out = src.extract(r, amount, tx);
                fit = out <= 0 ? 0 : dst.insert(r, out, tx);
            }
            if (fit <= 0) continue;
            try (Transaction tx = Transaction.openRoot()) {
                int out = src.extract(r, fit, tx);
                int in = out <= 0 ? 0 : dst.insert(r, out, tx);
                if (out == fit && in == fit) {
                    tx.commit();
                    return fit;
                }
            }
        }
        return 0;
    }

    public static int moveEnergy(EnergyHandler src, EnergyHandler dst, int max) {
        int fit;
        try (Transaction tx = Transaction.openRoot()) {
            int out = src.extract(max, tx);
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
