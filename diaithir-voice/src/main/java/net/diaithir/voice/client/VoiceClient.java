package net.diaithir.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.diaithir.voice.AdpcmCodec;
import net.diaithir.voice.DiaithirVoice;
import net.diaithir.voice.Spatial;
import net.diaithir.voice.VoiceConfig;
import net.diaithir.voice.VoiceDown;
import net.diaithir.voice.VoiceUp;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client side of the voice link: push-to-talk capture with Java Sound, ADPCM encode, send over the game connection;
 * and playback of relayed frames through a jitter buffer with distance gain and stereo placement.
 * Set -Ddiaithir.voice.testtone=440 to send a synthetic tone instead of reading a microphone (used by the cloud test).
 */
@EventBusSubscriber(modid = DiaithirVoice.ID, value = Dist.CLIENT)
public final class VoiceClient {
    private static final Logger LOG = LogUtils.getLogger();
    private static final AudioFormat MONO = new AudioFormat(AdpcmCodec.SAMPLE_RATE, 16, 1, true, false);
    private static final AudioFormat STEREO = new AudioFormat(AdpcmCodec.SAMPLE_RATE, 16, 2, true, false);
    private static final double TEST_TONE = Double.parseDouble(System.getProperty("diaithir.voice.testtone", "0"));

    private static KeyMapping talkKey;
    private static volatile boolean talking;
    private static volatile boolean inWorld;
    private static volatile double listenerX, listenerZ;
    private static volatile float listenerYaw;
    private static volatile boolean captureStarted;
    private static volatile boolean micFailed;
    private static short sendSeq;
    private static long sent;
    private static volatile long lastSendNanos;

    private static final Map<UUID, Speaker> SPEAKERS = new ConcurrentHashMap<>();
    private static volatile boolean mixerStarted;

    private static final class Speaker {
        final Jitter<short[]> jitter = new Jitter<>(VoiceConfig.JITTER_FRAMES.get(), VoiceConfig.JITTER_FRAMES.get() + 4);
        volatile int gain;
        volatile float x, z;
        long rx;
        double lastRms;
        float lastPan;
        long lastLog;
        long lastRxNanos;
    }

    private VoiceClient() {}

    /** True while frames are going out (within the last 200 ms). */
    static boolean transmitting() {
        return System.nanoTime() - lastSendNanos < 200_000_000L;
    }

    /** Ids of the players heard in the last 400 ms. */
    static java.util.List<UUID> speaking() {
        long now = System.nanoTime();
        var out = new java.util.ArrayList<UUID>();
        for (var e : SPEAKERS.entrySet()) if (now - e.getValue().lastRxNanos < 400_000_000L) out.add(e.getKey());
        return out;
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent event) {
        var category = new KeyMapping.Category(Identifier.fromNamespaceAndPath(DiaithirVoice.ID, "voice"));
        event.registerCategory(category);
        talkKey = new KeyMapping("key.diaithirvoice.talk", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, category);
        event.register(talkKey);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean ready = mc.player != null && mc.getConnection() != null
            && NetworkRegistry.hasChannel(mc.getConnection(), VoiceUp.TYPE.id());
        inWorld = ready;
        if (!ready) { talking = false; return; }
        listenerX = mc.player.getX();
        listenerZ = mc.player.getZ();
        listenerYaw = mc.player.getYRot();
        talking = TEST_TONE > 0 || (talkKey != null && talkKey.isDown() && mc.screen == null);
        if (talking && !captureStarted && !micFailed) { // the microphone is only opened the first time you talk
            captureStarted = true;
            Thread t = new Thread(VoiceClient::captureLoop, "diaithir-voice-capture");
            t.setDaemon(true);
            t.start();
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        inWorld = false;
        talking = false;
        SPEAKERS.clear();
    }

    // ---- capture ----

    private static void captureLoop() {
        TargetDataLine line = null;
        if (TEST_TONE <= 0) {
            line = openMic();
            if (line == null) { LOG.warn("voice: no microphone available, you can listen but not talk"); micFailed = true; captureStarted = false; return; }
        } else {
            LOG.info("voice: test tone {} Hz replaces the microphone", TEST_TONE);
        }
        byte[] raw = new byte[AdpcmCodec.FRAME_SAMPLES * 2];
        short[] pcm = new short[AdpcmCodec.FRAME_SAMPLES];
        long phase = 0;
        AdpcmCodec.State enc = new AdpcmCodec.State();
        long next = System.nanoTime();
        try {
            while (true) {
                if (line != null) {
                    int got = 0;
                    while (got < raw.length) {
                        int n = line.read(raw, got, raw.length - got);
                        if (n <= 0) return;
                        got += n;
                    }
                    for (int i = 0; i < pcm.length; i++) pcm[i] = (short) ((raw[2 * i] & 0xFF) | (raw[2 * i + 1] << 8));
                } else {
                    next += 20_000_000L;
                    long wait = next - System.nanoTime();
                    if (wait > 0) Thread.sleep(wait / 1_000_000L, (int) (wait % 1_000_000L));
                    for (int i = 0; i < pcm.length; i++, phase++) pcm[i] = (short) (8000 * Math.sin(2 * Math.PI * TEST_TONE * phase / AdpcmCodec.SAMPLE_RATE));
                }
                if (!(talking && inWorld)) { enc = new AdpcmCodec.State(); continue; } // a mic keeps being drained so frames stay current
                var conn = Minecraft.getInstance().getConnection();
                if (conn == null) continue;
                conn.send(new VoiceUp(sendSeq++, AdpcmCodec.encode(pcm, pcm.length, enc)));
                lastSendNanos = System.nanoTime();
                if (++sent % 100 == 1) LOG.info("voice tx: {} frames sent", sent);
            }
        } catch (InterruptedException ignored) {
        } finally {
            if (line != null) line.close();
            captureStarted = false;
        }
    }

    private static TargetDataLine openMic() {
        String want = VoiceConfig.MIC_NAME.get().toLowerCase();
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, MONO);
            TargetDataLine line = null;
            if (!want.isEmpty()) {
                for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
                    if (mi.getName().toLowerCase().contains(want) && AudioSystem.getMixer(mi).isLineSupported(info)) {
                        line = (TargetDataLine) AudioSystem.getMixer(mi).getLine(info);
                        break;
                    }
                }
            }
            if (line == null && AudioSystem.isLineSupported(info)) line = (TargetDataLine) AudioSystem.getLine(info);
            if (line == null) return null;
            line.open(MONO, AdpcmCodec.FRAME_SAMPLES * 2 * 4);
            line.start();
            return line;
        } catch (Exception | LinkageError e) {
            LOG.warn("voice: could not open the microphone: {}", e.toString());
            return null;
        }
    }

    // ---- playback ----

    /** Called on the network thread for every relayed frame. */
    public static void receive(VoiceDown down) {
        short[] pcm = AdpcmCodec.decode(down.data());
        if (pcm == null) return;
        Speaker s = SPEAKERS.computeIfAbsent(down.speaker(), k -> new Speaker());
        s.gain = down.gain();
        s.x = down.x();
        s.z = down.z();
        s.lastRxNanos = System.nanoTime();
        synchronized (s.jitter) { s.jitter.push(pcm); }
        s.rx++;
        s.lastRms = AdpcmCodec.rms(pcm, pcm.length);
        if (!mixerStarted) {
            mixerStarted = true;
            Thread t = new Thread(VoiceClient::mixLoop, "diaithir-voice-mix");
            t.setDaemon(true);
            t.start();
        }
    }

    private static SourceDataLine openSpeakers() {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, STEREO);
            if (!AudioSystem.isLineSupported(info)) return null;
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(STEREO, AdpcmCodec.FRAME_SAMPLES * 4 * 5);
            line.start();
            return line;
        } catch (Exception | LinkageError e) {
            LOG.warn("voice: no audio output ({}), incoming speech is counted but not heard", e.toString());
            return null;
        }
    }

    private static void mixLoop() {
        SourceDataLine out = openSpeakers();
        int n = AdpcmCodec.FRAME_SAMPLES;
        float[] mix = new float[n * 2];
        byte[] block = new byte[n * 4];
        long next = System.nanoTime();
        while (true) {
            next += 20_000_000L;
            boolean any = false;
            java.util.Arrays.fill(mix, 0f);
            double volume = VoiceConfig.VOLUME.get();
            for (var it = SPEAKERS.entrySet().iterator(); it.hasNext(); ) {
                Speaker s = it.next().getValue();
                if (System.nanoTime() - s.lastRxNanos > 5_000_000_000L) { it.remove(); continue; }
                short[] f;
                synchronized (s.jitter) { f = s.jitter.pop(); }
                if (f == null) continue;
                any = true;
                float pan = Spatial.pan(s.x - listenerX, s.z - listenerZ, listenerYaw);
                float[] lr = Spatial.stereo(pan);
                float g = (float) (s.gain / 255.0 * volume);
                s.lastPan = pan;
                for (int i = 0; i < n && i < f.length; i++) {
                    mix[2 * i] += f[i] * g * lr[0];
                    mix[2 * i + 1] += f[i] * g * lr[1];
                }
                long now = System.nanoTime();
                if (now - s.lastLog > 2_000_000_000L) {
                    s.lastLog = now;
                    LOG.info("voice rx: {} frames, rms {}, gain {}/255, pan {} (speaker offset {},{} listener yaw {}), depth {}, dropped {}, underruns {}",
                        s.rx, (int) s.lastRms, s.gain, String.format("%.2f", pan), Math.round(s.x - listenerX), Math.round(s.z - listenerZ), Math.round(listenerYaw), s.jitter.depth(), s.jitter.dropped, s.jitter.underruns);
                }
            }
            if (any && out != null) {
                for (int i = 0; i < mix.length; i++) {
                    int v = Math.max(-32768, Math.min(32767, Math.round(mix[i])));
                    block[2 * i] = (byte) v;
                    block[2 * i + 1] = (byte) (v >> 8);
                }
                out.write(block, 0, block.length);
                long wait = next - System.nanoTime(); // a real line paces us by blocking in write; this only matters when it does not
                if (wait > 0) try { Thread.sleep(wait / 1_000_000L, (int) (wait % 1_000_000L)); } catch (InterruptedException e) { return; }
                else if (wait < -100_000_000L) next = System.nanoTime();
            } else {
                long wait = next - System.nanoTime();
                if (wait > 0) {
                    try { Thread.sleep(wait / 1_000_000L, (int) (wait % 1_000_000L)); } catch (InterruptedException e) { return; }
                } else if (wait < -100_000_000L) {
                    next = System.nanoTime(); // stalled for a long time (loaded machine): stop trying to catch up
                }
            }
        }
    }
}
