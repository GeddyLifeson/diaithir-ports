package net.diaithir.claims.capitals;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/** A realm: a name, the chunk it sits in, who rules it, who comes next, and its subjects in the order they swore. */
public record Capital(String name, String dimension, int cx, int cz, Optional<UUID> ruler, String rulerName, Optional<UUID> heir, List<UUID> subjects, long founded) {
    public static final Codec<Capital> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.fieldOf("name").forGetter(Capital::name),
        Codec.STRING.fieldOf("dim").forGetter(Capital::dimension),
        Codec.INT.fieldOf("cx").forGetter(Capital::cx),
        Codec.INT.fieldOf("cz").forGetter(Capital::cz),
        UUIDUtil.CODEC.optionalFieldOf("ruler").forGetter(Capital::ruler),
        Codec.STRING.optionalFieldOf("ruler_name", "").forGetter(Capital::rulerName),
        UUIDUtil.CODEC.optionalFieldOf("heir").forGetter(Capital::heir),
        UUIDUtil.CODEC.listOf().optionalFieldOf("subjects", List.of()).forGetter(Capital::subjects),
        Codec.LONG.optionalFieldOf("founded", 0L).forGetter(Capital::founded)).apply(i, Capital::new));

    public Capital withRuler(UUID id, String n) {
        return new Capital(name, dimension, cx, cz, Optional.ofNullable(id), n, heir.filter(h -> !h.equals(id)), subjects.stream().filter(s -> !s.equals(id)).toList(), founded);
    }

    public Capital withHeir(UUID id) {
        return new Capital(name, dimension, cx, cz, ruler, rulerName, Optional.ofNullable(id), subjects, founded);
    }

    public Capital withSubjects(List<UUID> s) {
        return new Capital(name, dimension, cx, cz, ruler, rulerName, heir, s, founded);
    }

    public List<UUID> plus(UUID id) {
        List<UUID> l = new ArrayList<>(subjects);
        if (!l.contains(id)) l.add(id);
        return l;
    }
}
