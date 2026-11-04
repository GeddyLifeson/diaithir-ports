package net.diaithir.worldsys.packversioncheck;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Compares each joining player's pack version with the server pack version. */
public final class PackVersionCheckFeature {
    private static final Map<UUID, Integer> PENDING = new HashMap<>();
    private static final Map<UUID, String> REPORTED = new HashMap<>();

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, PackVersionCheckFeature::registerPayloads);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) PENDING.put(p.getUUID(), PackVersionConstants.CHECK_DELAY_TICKS);
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, e -> {
            PENDING.remove(e.getEntity().getUUID());
            REPORTED.remove(e.getEntity().getUUID());
        });
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, PackVersionCheckFeature::tick);
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) PackVersionClient.init();
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent e) {
        PayloadRegistrar registrar = e.registrar("1").optional();
        registrar.playToServer(PackVersionPayload.TYPE, PackVersionPayload.CODEC, (payload, ctx) -> {
            if (ctx.player() instanceof ServerPlayer p) REPORTED.put(p.getUUID(), payload.version());
        });
    }

    private static void tick(ServerTickEvent.Post e) {
        if (PENDING.isEmpty()) return;
        var it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            ServerPlayer p = e.getServer().getPlayerList().getPlayer(entry.getKey());
            if (p == null) {
                it.remove();
                continue;
            }
            String reported = REPORTED.get(entry.getKey());
            if (reported != null) {
                it.remove();
                if (!reported.equals(PackVersionConstants.PACK_VERSION)) kick(p, reported);
                continue;
            }
            int left = entry.getValue() - 1;
            if (left > 0) {
                entry.setValue(left);
            } else {
                it.remove();
                if (!PackVersionConstants.ALLOW_UNVERSIONED) kick(p, "none");
            }
        }
    }

    private static void kick(ServerPlayer p, String clientVersion) {
        p.connection.disconnect(Component.literal("Modpack version mismatch. Server: " + PackVersionConstants.PACK_VERSION + ", you: " + clientVersion + "."));
    }

    private PackVersionCheckFeature() {}
}
