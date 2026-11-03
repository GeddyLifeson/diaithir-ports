package net.diaithir.stations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * One job at a station. {@code station} is a progression graph station key (station.wright_bench ...), {@code stage} is
 * part (cast, forged or pressed from raw material), assembly (join a few major parts) or process (one input, one output, over time).
 * With {@code operator} true the job also needs a fed Operator Seat in range to start.
 */
public record StationRecipe(String station, String stage, List<Ing> ingredients, ItemStack result, int timeS, boolean operator) {
    public static final List<String> STAGES = List.of("part", "assembly", "process");

    /** An ingredient is an item id or a tag, with a count. */
    public record Ing(Optional<Identifier> item, Optional<Identifier> tag, int count) {
        public static final Codec<Ing> CODEC = RecordCodecBuilder.<Ing>create(i -> i.group(
                Identifier.CODEC.optionalFieldOf("item").forGetter(Ing::item),
                Identifier.CODEC.optionalFieldOf("tag").forGetter(Ing::tag),
                Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Ing::count)
        ).apply(i, Ing::new)).validate(g -> g.item().isPresent() != g.tag().isPresent()
                ? com.mojang.serialization.DataResult.success(g)
                : com.mojang.serialization.DataResult.error(() -> "an ingredient needs exactly one of item or tag"));

        public boolean matches(ItemStack s) {
            if (s.isEmpty()) return false;
            if (item.isPresent()) return BuiltInRegistries.ITEM.getKey(s.getItem()).equals(item.get());
            return s.is(TagKey.create(Registries.ITEM, tag.get()));
        }
    }

    public static final Codec<StationRecipe> CODEC = RecordCodecBuilder.<StationRecipe>create(i -> i.group(
            Codec.STRING.fieldOf("station").forGetter(StationRecipe::station),
            Codec.STRING.fieldOf("stage").forGetter(StationRecipe::stage),
            Ing.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(StationRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(StationRecipe::result),
            Codec.intRange(0, 100000).optionalFieldOf("time_s", 2).forGetter(StationRecipe::timeS),
            Codec.BOOL.optionalFieldOf("operator", false).forGetter(StationRecipe::operator)
    ).apply(i, StationRecipe::new)).validate(r -> STAGES.contains(r.stage())
            ? com.mojang.serialization.DataResult.success(r)
            : com.mojang.serialization.DataResult.error(() -> "stage must be part, assembly or process"));

    /** Whether the stacks hold every ingredient (summed over slots). */
    public boolean satisfiedBy(List<ItemStack> inv) {
        for (Ing g : ingredients) {
            int have = 0;
            for (ItemStack s : inv) if (g.matches(s)) have += s.getCount();
            if (have < g.count()) return false;
        }
        return true;
    }

    /** Removes the ingredients from the stacks. Call only after {@link #satisfiedBy}. */
    public void consume(List<ItemStack> inv) {
        for (Ing g : ingredients) {
            int need = g.count();
            for (ItemStack s : inv) {
                if (need <= 0) break;
                if (g.matches(s)) {
                    int n = Math.min(need, s.getCount());
                    s.shrink(n);
                    need -= n;
                }
            }
        }
    }
}
