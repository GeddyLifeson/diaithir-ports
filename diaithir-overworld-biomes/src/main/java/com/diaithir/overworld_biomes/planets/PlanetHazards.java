package com.diaithir.overworld_biomes.planets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

/**
 * Real effects for the hazards listed in each planet's colony profile (planet_colony.json). Runs about every two seconds
 * for players standing outdoors on a planet. Effects are mild and short; shelter (a roof) removes them.
 * ash_fall: mining fatigue while the planet's weather is wet; storms: slowness in a storm; drought: faster hunger by day;
 * freezing: body heat loss unless wearing a leather chestplate. lava_sea, no_sea, extreme_terrain and dark_nights shape the
 * world itself and need no effect.
 */
final class PlanetHazards {
    private PlanetHazards() {}

    static void tick(ServerLevel level) {
        if (level.players().isEmpty()) return;
        String id = level.dimension().identifier().getPath();
        JsonObject prof = PlanetColony.profile(id);
        if (prof == null || !prof.has("hazards")) return;
        Set<String> h = new HashSet<>();
        JsonArray arr = prof.getAsJsonArray("hazards");
        for (JsonElement e : arr) h.add(e.getAsString());
        if (h.isEmpty()) return;
        boolean wet = PlanetWeather.wet(level), storm = PlanetWeather.storm(level);
        boolean day = level.isBrightOutside();
        for (ServerPlayer p : level.players()) {
            if (p.isCreative() || p.isSpectator() || !level.canSeeSky(p.blockPosition())) continue;
            if (h.contains("ash_fall") && wet) p.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 0, true, false));
            if (h.contains("storms") && storm) p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0, true, false));
            if (h.contains("drought") && day) p.causeFoodExhaustion(0.4F);
            if (h.contains("freezing") && !p.getItemBySlot(EquipmentSlot.CHEST).is(Items.LEATHER_CHESTPLATE))
                p.setTicksFrozen(Math.min(p.getTicksFrozen() + 10, p.getTicksRequiredToFreeze()));
        }
    }
}
