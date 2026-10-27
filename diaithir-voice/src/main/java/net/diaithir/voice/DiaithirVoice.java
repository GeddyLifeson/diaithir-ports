package net.diaithir.voice;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proximity voice that travels inside the normal Minecraft connection, so a single TCP tunnel is enough.
 * Clean-room: written from how proximity voice behaves, no code taken from any voice mod.
 */
@Mod(DiaithirVoice.ID)
public final class DiaithirVoice {
    public static final String ID = "diaithirvoice";
    static final Logger LOG = LogUtils.getLogger();
    private static final Map<UUID, RateLimiter> LIMITERS = new ConcurrentHashMap<>();
    private static MethodHandle speechHook;
    private static boolean hookLooked;
    private static long relayed;
    private static long lastLog;

    public DiaithirVoice(IEventBus modBus, ModContainer container) {
        modBus.addListener(DiaithirVoice::registerPayloads);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.class, e -> forget(e.getEntity().getUUID()));
        container.registerConfig(ModConfig.Type.COMMON, VoiceConfig.COMMON);
        container.registerConfig(ModConfig.Type.CLIENT, VoiceConfig.CLIENT);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var reg = event.registrar("1").optional();
        reg.playToServer(VoiceUp.TYPE, VoiceUp.CODEC, DiaithirVoice::onUp);
        reg.executesOn(HandlerThread.NETWORK).playToClient(VoiceDown.TYPE, VoiceDown.CODEC, (down, ctx) -> net.diaithir.voice.client.VoiceClient.receive(down));
    }

    private static void onUp(VoiceUp up, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer speaker) || speaker.isSpectator()) return;
        RateLimiter limiter = LIMITERS.computeIfAbsent(speaker.getUUID(), k -> new RateLimiter(VoiceConfig.MAX_FRAMES_PER_SECOND.get(), 10));
        if (!limiter.tryAcquire(System.nanoTime())) return;
        short[] pcm = AdpcmCodec.decode(up.data());
        if (pcm == null) return;
        double range = VoiceConfig.RANGE.get(), near = VoiceConfig.NEAR.get();
        VoiceDown out = null;
        int heard = 0;
        double nearest = Double.MAX_VALUE;
        for (ServerPlayer other : speaker.level().getServer().getPlayerList().getPlayers()) {
            if (other == speaker || other.level() != speaker.level()) continue;
            double d = other.distanceTo(speaker);
            nearest = Math.min(nearest, d);
            int gain = Spatial.quantise(Spatial.gain(d, near, range));
            if (gain == 0 || !NetworkRegistry.hasChannel(other.connection, VoiceDown.TYPE.id())) continue;
            if (out == null) out = new VoiceDown(speaker.getUUID(), up.seq(), 0, (float) speaker.getX(), (float) speaker.getEyeY(), (float) speaker.getZ(), up.data());
            other.connection.send(new VoiceDown(out.speaker(), out.seq(), gain, out.x(), out.y(), out.z(), out.data()));
            heard++;
        }
        relayed += heard;
        long now = System.nanoTime();
        if (heard > 0 && now - lastLog > 2_000_000_000L) {
            lastLog = now;
            LOG.info("voice relay: {} frames sent so far, last from {} to {} listeners (nearest {} blocks)", relayed, speaker.getName().getString(), heard, Math.round(nearest));
        }
        reportSpeech(speaker, AdpcmCodec.levelDb(AdpcmCodec.rms(pcm, pcm.length)));
    }

    /** Lets the optional diaithir-voice-interaction mod hear that someone spoke, without depending on it. */
    private static void reportSpeech(ServerPlayer speaker, float levelDb) {
        if (!hookLooked) {
            hookLooked = true;
            if (ModList.get().isLoaded("diaithirvoiceinteraction")) {
                try {
                    speechHook = MethodHandles.publicLookup().findStatic(
                        Class.forName("net.diaithir.voiceinteraction.api.VoiceInteractionApi"), "onSpeech",
                        MethodType.methodType(void.class, ServerPlayer.class, float.class, boolean.class));
                } catch (ReflectiveOperationException e) {
                    LOG.warn("diaithirvoiceinteraction is loaded but its API was not found", e);
                }
            }
        }
        if (speechHook == null) return;
        try {
            speechHook.invoke(speaker, levelDb, false);
        } catch (Throwable t) {
            LOG.warn("speech hook failed", t);
            speechHook = null;
        }
    }

    public static void forget(UUID player) {
        LIMITERS.remove(player);
    }
}
