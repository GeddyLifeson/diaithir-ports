package net.diaithir.worldsys.skilltrees;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

/** Immutable per-player skill XP totals. */
public record SkillData(Map<String, Integer> xp) {
    public static final SkillData EMPTY = new SkillData(Map.of());
    public static final Codec<SkillData> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(SkillData::new, SkillData::xp);

    public int xp(Skill s) {
        return xp.getOrDefault(s.key(), 0);
    }

    public int level(Skill s) {
        int total = xp(s);
        int level = 0;
        while (level < Skill.MAX_LEVEL && total >= Skill.xpForLevel(level + 1)) {
            total -= Skill.xpForLevel(level + 1);
            level++;
        }
        return level;
    }

    public SkillData with(Skill s, int value) {
        Map<String, Integer> copy = new HashMap<>(xp);
        copy.put(s.key(), value);
        return new SkillData(Map.copyOf(copy));
    }
}
