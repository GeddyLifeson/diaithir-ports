package net.diaithir.unrest;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

/** Saved form of one level's {@link Field}. */
public final class UnrestData extends SavedData {
    public static final Codec<UnrestData> CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).xmap(m -> {
        UnrestData d = new UnrestData();
        m.forEach((k, v) -> d.field.raw().put(Long.parseLong(k), v));
        return d;
    }, d -> {
        Map<String, Double> m = new HashMap<>();
        d.field.raw().forEach((k, v) -> m.put(Long.toString(k), v));
        return m;
    });
    public static final SavedDataType<UnrestData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(DiaithirUnrest.ID, "field"), UnrestData::new, CODEC);

    public final Field field = new Field();
}
