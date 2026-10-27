package net.diaithir.voice.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Posted on {@code NeoForge.EVENT_BUS}, on the server thread, for every 20 ms frame of speech a player sends.
 * {@code levelDb} is the frame's loudness in dB below full scale (0 is full scale, -96 is silence).
 * Other mods listen to this to react to voices.
 */
public class SpeechEvent extends Event {
    private final ServerPlayer player;
    private final float levelDb;
    private final boolean whisper;

    public SpeechEvent(ServerPlayer player, float levelDb, boolean whisper) {
        this.player = player;
        this.levelDb = levelDb;
        this.whisper = whisper;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public float getLevelDb() {
        return levelDb;
    }

    public boolean isWhisper() {
        return whisper;
    }
}
