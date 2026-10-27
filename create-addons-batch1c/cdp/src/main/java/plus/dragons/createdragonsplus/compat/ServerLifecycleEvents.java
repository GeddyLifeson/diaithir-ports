package plus.dragons.createdragonsplus.compat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Fabric-shaped server lifecycle hooks for the ported addons, implemented on the NeoForge game bus.
 * Call {@link #init()} once from the library mod constructor.
 */
public final class ServerLifecycleEvents {
    public static final Hook<ServerCallback> SERVER_STARTING = new Hook<>();
    public static final Hook<ServerCallback> SERVER_STARTED = new Hook<>();
    public static final Hook<ServerCallback> SERVER_STOPPED = new Hook<>();
    public static final Hook<ReloadCallback> END_DATA_PACK_RELOAD = new Hook<>();
    public static final Hook<SyncCallback> SYNC_DATA_PACK_CONTENTS = new Hook<>();
    private static boolean initialized;

    private ServerLifecycleEvents() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class,
                e -> SERVER_STARTING.listeners.forEach(l -> l.on(e.getServer())));
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class,
                e -> SERVER_STARTED.listeners.forEach(l -> l.on(e.getServer())));
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class,
                e -> SERVER_STOPPED.listeners.forEach(l -> l.on(e.getServer())));
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, e -> {
            MinecraftServer server = e.getPlayerList().getServer();
            if (e.getPlayer() == null) {
                END_DATA_PACK_RELOAD.listeners.forEach(l -> l.on(server, server.getResourceManager(), true));
            }
            boolean joined = e.getPlayer() != null;
            e.getRelevantPlayers().forEach(p -> SYNC_DATA_PACK_CONTENTS.listeners.forEach(l -> l.on(p, joined)));
        });
    }

    public static final class Hook<T> {
        private final List<T> listeners = new CopyOnWriteArrayList<>();

        public void register(T listener) {
            listeners.add(listener);
        }
    }

    @FunctionalInterface
    public interface ServerCallback {
        void on(MinecraftServer server);
    }

    /** {@code resources} is the live resource manager after the reload. */
    @FunctionalInterface
    public interface ReloadCallback {
        void on(MinecraftServer server, ResourceManager resources, boolean success);
    }

    @FunctionalInterface
    public interface SyncCallback {
        void on(ServerPlayer player, boolean joined);
    }
}
