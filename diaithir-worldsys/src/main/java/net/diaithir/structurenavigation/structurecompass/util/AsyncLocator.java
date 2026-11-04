// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Only the HolderSet locate variant kept; thread name prefix adjusted.
package net.diaithir.structurenavigation.structurecompass.util;

import com.mojang.datafixers.util.Pair;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;

// Source: https://github.com/thebrightspark/AsyncLocator
public final class AsyncLocator {
    private static ExecutorService executor = null;

    private AsyncLocator() {}

    private static void setupExecutorService() {
        shutdownExecutorService();
        executor = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private static final AtomicInteger POOL_NUM = new AtomicInteger(1);
            private final AtomicInteger threadNum = new AtomicInteger(1);
            private final String namePrefix = "diaithir-structurecompass-" + POOL_NUM.getAndIncrement() + "-thread-";

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(null, r, namePrefix + threadNum.getAndIncrement());
            }
        });
    }

    private static void shutdownExecutorService() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    public static void handleServerAboutToStartEvent() {
        setupExecutorService();
    }

    public static void handleServerStoppingEvent() {
        shutdownExecutorService();
    }

    public static LocateTask<Pair<BlockPos, Holder<Structure>>> locate(
            ServerLevel level,
            HolderSet<Structure> structureSet,
            BlockPos pos,
            int searchRadius,
            boolean skipKnownStructures
    ) {
        if (executor == null) {
            setupExecutorService();
        }
        CompletableFuture<Pair<BlockPos, Holder<Structure>>> completableFuture = new CompletableFuture<>();
        Future<?> future = executor.submit(() -> {
            Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, structureSet, pos, searchRadius, skipKnownStructures);
            completableFuture.complete(found);
        });
        return new LocateTask<>(level.getServer(), completableFuture, future);
    }

    public record LocateTask<T>(MinecraftServer server, CompletableFuture<T> completableFuture, Future<?> taskFuture) {
        public LocateTask<T> then(Consumer<T> action) {
            completableFuture.thenAccept(action);
            return this;
        }

        public LocateTask<T> thenOnServerThread(Consumer<T> action) {
            completableFuture.thenAccept(pos -> server.submit(() -> action.accept(pos)));
            return this;
        }

        public void cancel() {
            taskFuture.cancel(true);
            completableFuture.cancel(false);
        }
    }
}
