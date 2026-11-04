package net.diaithir.networkcompat.handshake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clean-room modpack version handshake. During the configuration phase the server sends the list of
 * Diaithir modules it runs (id and version); the client answers with its own list; the server
 * compares the two and disconnects with a readable message if they differ. Nothing is sent to
 * clients that do not have the channel, so vanilla-style joins are unaffected.
 */
public final class PackHandshake {
    private static final Logger LOG = LoggerFactory.getLogger("diaithir-handshake");
    private static final String PREFIX = "diaithir";

    private PackHandshake() {}

    public record Hello(List<String> modules) implements CustomPacketPayload {
        public static final Type<Hello> TYPE = new Type<>(Identifier.fromNamespaceAndPath("diaithircrashtools", "pack_handshake"));
        public static final StreamCodec<FriendlyByteBuf, Hello> CODEC =
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(2048)).map(Hello::new, Hello::modules).mapStream(b -> b);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Server-side configuration task: sends our module list and waits for the reply. */
    private record Task() implements ICustomConfigurationTask {
        static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(Identifier.fromNamespaceAndPath("diaithircrashtools", "pack_handshake"));

        @Override
        public void run(java.util.function.Consumer<CustomPacketPayload> sender) {
            sender.accept(new Hello(localModules()));
        }

        @Override
        public ConfigurationTask.Type type() {
            return TYPE;
        }
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, e ->
                e.registrar("1").optional().configurationBidirectional(Hello.TYPE, Hello.CODEC,
                        PackHandshake::onClient, PackHandshake::onServer));
        modBus.addListener(RegisterConfigurationTasksEvent.class, e -> {
            if (e.getListener().hasChannel(Hello.TYPE)) e.register(new Task());
        });
    }

    /** Sorted "id=version" for every loaded module whose id starts with "diaithir". */
    static List<String> localModules() {
        TreeMap<String, String> out = new TreeMap<>();
        ModList.get().getMods().forEach(m -> {
            if (m.getModId().startsWith(PREFIX)) out.put(m.getModId(), m.getVersion().toString());
        });
        List<String> l = new ArrayList<>();
        out.forEach((k, v) -> l.add(k + "=" + v));
        return Collections.unmodifiableList(l);
    }

    private static void onClient(Hello serverHello, IPayloadContext ctx) {
        ctx.reply(new Hello(localModules()));
    }

    private static void onServer(Hello clientHello, IPayloadContext ctx) {
        List<String> diff = diff(localModules(), clientHello.modules());
        if (diff.isEmpty()) {
            ctx.finishCurrentTask(Task.TYPE);
            return;
        }
        LOG.warn("Pack mismatch with a joining client: {}", diff);
        String shown = String.join(", ", diff.subList(0, Math.min(5, diff.size())))
                + (diff.size() > 5 ? " and " + (diff.size() - 5) + " more" : "");
        ctx.disconnect(Component.literal("Diaithir pack version mismatch. Update your pack to match the server. Differences: " + shown));
    }

    /** Entries present on only one side, or with different versions. Package-private for tests. */
    static List<String> diff(List<String> server, List<String> client) {
        List<String> d = new ArrayList<>();
        for (String s : server) if (!client.contains(s)) d.add("server " + s);
        for (String c : client) if (!server.contains(c)) d.add("client " + c);
        return d;
    }
}
