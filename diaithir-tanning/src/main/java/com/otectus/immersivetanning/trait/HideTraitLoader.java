package com.otectus.immersivetanning.trait;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads {@code data/<namespace>/immersive_tanning/hide_traits/<name>.json}. The file id is the trait id:
 * <pre>
 * { "effect": "immersive_tanning:heatproof", "strength": 1.0, "color": "#E0703A" }
 * </pre>
 * A trait only exists on hides whose hide source names it. The player never chooses it.
 */
public class HideTraitLoader extends SimpleJsonResourceReloadListener<JsonElement> {
    private static volatile Map<Identifier, TraitRef> traits = Map.of();

    public HideTraitLoader() {
        super(net.minecraft.util.ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json("immersive_tanning/hide_traits"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, TraitRef> loaded = new LinkedHashMap<>();
        files.forEach((id, element) -> {
            try {
                loaded.put(id, parse(id, GsonHelper.convertToJsonObject(element, "hide trait")));
            } catch (RuntimeException e) {
                ImmersiveTanning.LOGGER.error("Skipping hide trait {}: {}", id, e.getMessage());
            }
        });
        traits = Map.copyOf(loaded);
        ImmersiveTanning.LOGGER.info("Loaded {} hide traits", loaded.size());
    }

    static TraitRef parse(Identifier id, JsonObject json) {
        Identifier effect = Identifier.parse(GsonHelper.getAsString(json, "effect"));
        if (!TraitEffects.isKnown(effect)) throw new JsonParseException("unknown trait effect " + effect);
        float strength = GsonHelper.getAsFloat(json, "strength", 1.0F);
        if (!(strength > 0F) || strength > 10F) throw new JsonParseException("strength must be in (0, 10]");
        String color = GsonHelper.getAsString(json, "color", "#C8A060");
        int rgb;
        try {
            rgb = Integer.parseInt(color.startsWith("#") ? color.substring(1) : color, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            throw new JsonParseException("color must be #RRGGBB");
        }
        return new TraitRef(id, effect, strength, rgb);
    }

    @Nullable
    public static TraitRef get(Identifier id) {
        return traits.get(id);
    }

    public static Collection<TraitRef> all() {
        return traits.values();
    }
}
