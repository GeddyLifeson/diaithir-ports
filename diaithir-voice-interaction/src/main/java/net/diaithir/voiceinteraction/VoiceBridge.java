package net.diaithir.voiceinteraction;

import net.diaithir.voice.api.SpeechEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Listens to diaithir-voice's {@link SpeechEvent}. Only loaded when diaithir-voice is installed. */
final class VoiceBridge {
    private VoiceBridge() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(SpeechEvent.class, e -> Noticing.speech(e.getPlayer(), e.getLevelDb(), e.isWhisper(), false));
    }
}
