package net.diaithir.voiceinteraction.api;

import net.diaithir.voiceinteraction.Noticing;
import net.minecraft.server.level.ServerPlayer;

/**
 * Anything that carries voice (Simple Voice Chat through its plugin, diaithir-voice through this call) reports speech here.
 * Call on the server thread or any thread: the work is handed to the server thread.
 */
public final class VoiceInteractionApi {
    private VoiceInteractionApi() {}

    /** A player produced {@code levelDb} (dB below full scale, -96 is silence) of speech. */
    public static void onSpeech(ServerPlayer speaker, float levelDb, boolean whisper) {
        Noticing.speech(speaker, levelDb, whisper, false);
    }

    /** Same, for speech inside a private group. */
    public static void onGroupSpeech(ServerPlayer speaker, float levelDb, boolean whisper) {
        Noticing.speech(speaker, levelDb, whisper, true);
    }
}
