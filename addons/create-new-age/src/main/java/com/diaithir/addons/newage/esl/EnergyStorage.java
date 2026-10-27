package com.diaithir.addons.newage.esl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** 64-bit energy storage with transaction support (same contract as the upstream ESL library, rebuilt on NeoForge transactions). */
public interface EnergyStorage {
    Map<BlockEntityType<?>, BiFunction<BlockEntity, Direction, EnergyStorage>> PROVIDERS = new ConcurrentHashMap<>();

    long insert(long maxAmount, TransactionContext transaction);

    long extract(long maxAmount, TransactionContext transaction);

    long getStoredEnergy();

    long getCapacity();

    default boolean supportsExtraction() { return true; }

    default boolean supportsInsertion() { return true; }

    @SuppressWarnings("unchecked")
    static <T extends BlockEntity> void registerForBlockEntity(BiFunction<T, Direction, EnergyStorage> provider, BlockEntityType<T> type) {
        PROVIDERS.putIfAbsent(type, (be, dir) -> provider.apply((T) be, dir));
    }

    /** Storage owned by this mod for the block entity, or null. */
    static EnergyStorage provided(BlockEntity be, Direction dir) {
        BiFunction<BlockEntity, Direction, EnergyStorage> f = PROVIDERS.get(be.getType());
        return f == null ? null : f.apply(be, dir);
    }

    /** Energy storage of an item stack; changes are written back into the given stack. */
    static EnergyStorage findForItem(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        var h = net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
        return h == null ? null : EnergyBridge.asStorage(h);
    }

    /** Finds our own storage first, then any NeoForge energy handler (Mekanism, Immersive Engineering, ...). */
    static EnergyStorage findForBlock(Level level, BlockPos pos, Direction dir) {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            EnergyStorage own = provided(be, dir);
            if (own != null) return own;
        }
        var h = level.getCapability(Capabilities.Energy.BLOCK, pos, dir);
        return h == null ? null : EnergyBridge.asStorage(h);
    }
}
