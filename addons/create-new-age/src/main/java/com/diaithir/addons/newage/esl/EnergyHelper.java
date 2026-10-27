package com.diaithir.addons.newage.esl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class EnergyHelper {
    private EnergyHelper() {}

    public static long moveEnergy(EnergyStorage from, EnergyStorage to, long max, TransactionContext tx) {
        if (from == null || to == null || max <= 0) return 0;
        try (Transaction t = Transaction.open(tx)) {
            long probe;
            try (Transaction sim = Transaction.open(t)) {
                long ext = from.extract(max, sim);
                probe = to.insert(ext, sim);
            }
            if (probe <= 0) return 0;
            long ext = from.extract(probe, t);
            long ins = to.insert(ext, t);
            if (ins > 0 && ins == ext) {
                t.commit();
                return ins;
            }
            return 0;
        }
    }

    public static long insertToSurrounding(EnergyStorage source, BlockPos pos, Level level, long amount, TransactionContext tx) {
        long moved = 0;
        for (Direction d : Direction.values()) {
            if (moved >= amount) break;
            EnergyStorage target = EnergyStorage.findForBlock(level, pos.relative(d), d.getOpposite());
            if (target == null || target == source) continue;
            moved += moveEnergy(source, target, amount - moved, tx);
        }
        return moved;
    }
}
