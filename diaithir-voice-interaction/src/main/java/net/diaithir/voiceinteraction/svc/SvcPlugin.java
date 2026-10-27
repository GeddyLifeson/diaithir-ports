package net.diaithir.voiceinteraction.svc;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.diaithir.voiceinteraction.api.VoiceInteractionApi;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin for Simple Voice Chat, found by it through the {@code @ForgeVoicechatPlugin} annotation. It listens to microphone
 * packets, measures how loud each is and reports it. Only loaded when Simple Voice Chat is installed.
 */
@ForgeVoicechatPlugin
public class SvcPlugin implements VoicechatPlugin {
    private VoicechatApi api;
    private final Map<UUID, OpusDecoder> decoders = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return "diaithirvoiceinteraction";
    }

    @Override
    public void initialize(VoicechatApi api) {
        this.api = api;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMic);
        registration.registerEvent(PlayerDisconnectedEvent.class, e -> {
            OpusDecoder d = decoders.remove(e.getPlayerUuid());
            if (d != null) d.close();
        });
    }

    private void onMic(MicrophonePacketEvent event) {
        VoicechatConnection c = event.getSenderConnection();
        if (c == null || !(c.getPlayer().getPlayer() instanceof ServerPlayer player)) return;
        byte[] opus = event.getPacket().getOpusEncodedData();
        if (opus == null || opus.length == 0) return;
        OpusDecoder decoder = decoders.computeIfAbsent(player.getUUID(), k -> api.createDecoder());
        short[] pcm = decoder.decode(opus);
        float db = net.diaithir.voiceinteraction.Loudness.db(pcm);
        boolean whisper = event.getPacket().isWhispering();
        if (c.isInGroup()) VoiceInteractionApi.onGroupSpeech(player, db, whisper);
        else VoiceInteractionApi.onSpeech(player, db, whisper);
    }
}
