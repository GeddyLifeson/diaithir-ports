package net.diaithir.filters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/** The data component on a filter: the root rule and a flag for an empty filter (matches everything). */
public record FilterData(List<Rule> rules) {
    public static final FilterData EMPTY = new FilterData(List.of());

    public static final Codec<FilterData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Rule.CODEC.listOf().fieldOf("rules").forGetter(FilterData::rules)).apply(i, FilterData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FilterData> STREAM_CODEC = StreamCodec.composite(
            Rule.STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list()), FilterData::rules, FilterData::new);

    /** Top level rules are ANDed together. */
    public Rule root() {
        return new Rule.And(rules);
    }

    public FilterData with(Rule r) {
        List<Rule> l = new java.util.ArrayList<>(rules);
        l.add(r);
        return new FilterData(List.copyOf(l));
    }

    public FilterData without(int index) {
        if (index < 0 || index >= rules.size()) return this;
        List<Rule> l = new java.util.ArrayList<>(rules);
        l.remove(index);
        return new FilterData(List.copyOf(l));
    }

    /** True if the rule set is within the configured size and depth limits. */
    public boolean withinLimits(int maxRules, int maxDepth) {
        return root().size() - 1 <= maxRules && root().depth() - 1 <= maxDepth;
    }
}
