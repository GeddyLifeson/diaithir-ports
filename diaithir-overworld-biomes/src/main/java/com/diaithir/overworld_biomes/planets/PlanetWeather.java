package com.diaithir.overworld_biomes.planets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.WeatherData;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Each planet runs its own weather (vanilla shares one weather state across all dimensions). Climate comes from the planet's
 * atmosphere spec: precipitation bias sets how often it is wet, "stormy" adds thunder and a longer wet share. Where it cannot
 * rain (dry bias) a wet phase is a dry storm: dark sky, lightning when thundering, and a burst of the planet's own particles
 * (ash fall, spores, glow) around every player.
 */
public final class PlanetWeather {
    private record Profile(String bias, boolean stormy, String particle) {}
    private static final class State { WeatherData data = new WeatherData(); int left; boolean wet, thunder; }

    private static final Map<String, Profile> PROFILES = new HashMap<>();
    private static final Map<ResourceKey<Level>, State> STATES = new HashMap<>();

    static {
        try (var in = PlanetWeather.class.getResourceAsStream("/diaithir_planet_weather.json")) {
            if (in != null) {
                JsonObject o = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
                o.entrySet().forEach(e -> {
                    JsonObject p = e.getValue().getAsJsonObject();
                    PROFILES.put(e.getKey(), new Profile(p.get("bias").getAsString(), p.get("stormy").getAsBoolean(), p.get("particle").getAsString()));
                });
            }
        } catch (Exception ex) {
            PROFILES.clear();
        }
    }

    private PlanetWeather() {}

    /** The planet's own weather state, or null for any level that is not a planet. */
    public static WeatherData of(ServerLevel level) {
        ResourceKey<Level> k = level.dimension();
        if (!k.identifier().getNamespace().equals(PlanetGate.NS)) return null;
        return STATES.computeIfAbsent(k, x -> new State()).data;
    }

    public static String describe(ServerLevel level) {
        State s = STATES.get(level.dimension()); Profile p = PROFILES.get(level.dimension().identifier().getPath());
        return s == null || p == null ? "no weather state" : (s.wet ? (s.thunder ? "storm" : "wet") : "clear") + " for " + s.left + " ticks (" + p.bias() + (p.stormy() ? ", stormy" : "") + ", particle " + p.particle() + ")";
    }

    static void forget(ResourceKey<Level> k) { STATES.remove(k); }

    static void tick(ServerLevel level) {
        Profile p = PROFILES.get(level.dimension().identifier().getPath());
        State s = STATES.computeIfAbsent(level.dimension(), x -> new State());
        if (p == null) return;
        if (--s.left <= 0) {
            double duty = switch (p.bias()) { case "dry" -> p.stormy() ? 0.2 : 0.0; case "rain" -> 0.45; case "snow" -> 0.4; default -> 0.3; };
            if (p.stormy() && !p.bias().equals("dry")) duty += 0.15;
            var rnd = level.getRandom();
            if (duty <= 0) { s.wet = false; s.thunder = false; s.left = 24000; }
            else if (s.wet) { s.wet = false; s.thunder = false; s.left = (int) (level.getRandom().nextInt(6000, 14000) * (1 - duty) / duty); }
            else { s.wet = true; s.left = rnd.nextInt(6000, 14000); s.thunder = p.stormy() && rnd.nextFloat() < 0.6f; }
        }
        WeatherData w = s.data;
        w.setClearWeatherTime(0); w.setRainTime(1_000_000); w.setThunderTime(1_000_000);
        w.setRaining(s.wet); w.setThundering(s.wet && s.thunder);
        if (s.wet && !p.particle().isEmpty() && (p.bias().equals("dry") || p.stormy()) && level.getGameTime() % 5 == 0) {
            var type = BuiltInRegistries.PARTICLE_TYPE.getValue(Identifier.withDefaultNamespace(p.particle()));
            if (type instanceof SimpleParticleType simple)
                for (ServerPlayer pl : level.players())
                    level.sendParticles(simple, pl.getX(), pl.getY() + 6, pl.getZ(), 14, 9, 5, 9, 0.02);
        }
    }
}
