package net.diaithir.elevators;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/** Tunables read once from data/diaithirelevators/elevators.json inside the jar. Defaults apply if it is missing. */
final class Config {
    /** Top cabin speed in blocks per tick. */
    static final double SPEED;
    /** Forge Energy per block travelled; 0 means the controller needs no power. */
    static final int ENERGY_PER_BLOCK;

    static {
        double speed = 0.3;
        int energy = 0;
        try (Reader r = new InputStreamReader(Config.class.getResourceAsStream("/data/diaithirelevators/elevators.json"), StandardCharsets.UTF_8)) {
            JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
            if (o.has("speed")) speed = Math.max(0.08, Math.min(0.6, o.get("speed").getAsDouble()));
            if (o.has("energy_per_block")) energy = Math.max(0, o.get("energy_per_block").getAsInt());
        } catch (Exception e) {
            // keep defaults
        }
        SPEED = speed;
        ENERGY_PER_BLOCK = energy;
    }

    private Config() {}
}
