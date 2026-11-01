package net.diaithir.footsteps;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Reader;
import java.util.List;
import java.util.Map;

/** What to layer onto a step, worked out from plain values so it can be tested without Minecraft. */
final class Layers {
    record Armor(String sound, float volume, float pitch) {}
    record Layer(String sound, float volume, float pitch) {}

    final Map<String, Armor> armor;
    final Armor wet;
    final float perPiece, sneakScale, sprintScale, pitchAlternate;
    final String stepSuffix;

    private Layers(JsonObject j) {
        Gson gson = new Gson();
        armor = new java.util.HashMap<>();
        j.getAsJsonObject("armor").entrySet().forEach(e -> armor.put(e.getKey(), gson.fromJson(e.getValue(), Armor.class)));
        wet = gson.fromJson(j.get("wet"), Armor.class);
        perPiece = j.get("per_piece").getAsFloat();
        sneakScale = j.get("sneak_scale").getAsFloat();
        sprintScale = j.get("sprint_scale").getAsFloat();
        pitchAlternate = j.get("pitch_alternate").getAsFloat();
        stepSuffix = j.get("step_suffix").getAsString();
    }

    static Layers load(Reader in) {
        try {
            return new Layers(new Gson().fromJson(in, JsonObject.class));
        } catch (JsonParseException | NullPointerException e) {
            throw new IllegalStateException("footsteps.json is malformed", e);
        }
    }

    boolean isStep(String soundPath) {
        return soundPath.endsWith(stepSuffix);
    }

    /** The armour clink for the worn pieces: the commonest material sets the sound, each piece adds volume. Null when unarmoured. */
    Layer armorLayer(List<String> pieceMaterials, boolean sneaking, boolean sprinting) {
        Map<String, Integer> count = new java.util.HashMap<>();
        for (String m : pieceMaterials) {
            if (armor.containsKey(m)) count.merge(m, 1, Integer::sum);
        }
        if (count.isEmpty()) return null;
        String top = count.entrySet().stream().max(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey())).get().getKey();
        int pieces = count.values().stream().mapToInt(Integer::intValue).sum();
        Armor a = armor.get(top);
        float v = a.volume() * Math.min(1F, pieces * perPiece) * (sneaking ? sneakScale : sprinting ? sprintScale : 1F);
        return new Layer(a.sound(), v, a.pitch());
    }

    Layer wetLayer(boolean raining, boolean sneaking) {
        return raining ? new Layer(wet.sound(), wet.volume() * (sneaking ? sneakScale : 1F), wet.pitch()) : null;
    }

    /** Left and right foot differ a little in pitch. */
    float stepPitch(float original, boolean leftFoot) {
        return original * (leftFoot ? 1F - pitchAlternate : 1F + pitchAlternate);
    }
}
