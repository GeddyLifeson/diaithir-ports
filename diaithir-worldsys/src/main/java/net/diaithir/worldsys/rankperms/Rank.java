package net.diaithir.worldsys.rankperms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

/** A named rank with display data and permission nodes. */
public record Rank(String name, int priority, String prefix, String color, List<String> nodes) {
    public static final Codec<Rank> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(Rank::name),
            Codec.INT.fieldOf("priority").forGetter(Rank::priority),
            Codec.STRING.fieldOf("prefix").forGetter(Rank::prefix),
            Codec.STRING.fieldOf("color").forGetter(Rank::color),
            Codec.STRING.listOf().fieldOf("nodes").forGetter(Rank::nodes)
    ).apply(i, Rank::new));

    public Rank withPrefix(String p) {
        return new Rank(name, priority, p, color, nodes);
    }

    public Rank withColor(String c) {
        return new Rank(name, priority, prefix, c, nodes);
    }

    public Rank withNodes(List<String> n) {
        return new Rank(name, priority, prefix, color, new ArrayList<>(n));
    }

    public boolean allows(String node) {
        for (String n : nodes) {
            if (n.equals(node) || n.equals("*")) return true;
            if (n.endsWith(".*") && node.startsWith(n.substring(0, n.length() - 1))) return true;
        }
        return false;
    }
}
