package net.diaithir.goety.rite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** One rite (data/goety/rituals/*.json): an activation item, offerings on pedestals, a cost in souls, and an outcome. */
public record Ritual(String type, String school, String activation, List<String> offerings, int soulCost, int duration,
                     String result, int count, String entity) {
    public static final Codec<Ritual> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.optionalFieldOf("type", "craft").forGetter(Ritual::type),
        Codec.STRING.optionalFieldOf("school", "magic").forGetter(Ritual::school),
        Codec.STRING.fieldOf("activation").forGetter(Ritual::activation),
        Codec.STRING.listOf().fieldOf("offerings").forGetter(Ritual::offerings),
        Codec.INT.optionalFieldOf("soul_cost", 1).forGetter(Ritual::soulCost),
        Codec.INT.optionalFieldOf("duration", 30).forGetter(Ritual::duration),
        Codec.STRING.optionalFieldOf("result", "").forGetter(Ritual::result),
        Codec.INT.optionalFieldOf("count", 1).forGetter(Ritual::count),
        Codec.STRING.optionalFieldOf("entity", "").forGetter(Ritual::entity)
    ).apply(i, Ritual::new));

    /** An entry is an item id or, with a leading #, an item tag. */
    public static boolean matches(String entry, ItemStack s) {
        if (s.isEmpty()) return false;
        if (entry.startsWith("#")) return s.is(TagKey.create(Registries.ITEM, Identifier.parse(entry.substring(1))));
        return BuiltInRegistries.ITEM.getKey(s.getItem()).equals(Identifier.parse(entry));
    }

    public boolean activatedBy(ItemStack s) {
        return matches(activation, s);
    }

    /** Pairs each offering with a distinct pedestal stack (backtracking, offerings are few); null when impossible. */
    public List<Integer> assign(List<ItemStack> pedestalStacks) {
        int[] pick = new int[offerings.size()];
        return solve(0, pick, new boolean[pedestalStacks.size()], pedestalStacks) ? java.util.Arrays.stream(pick).boxed().toList() : null;
    }

    private boolean solve(int i, int[] pick, boolean[] used, List<ItemStack> stacks) {
        if (i == offerings.size()) return true;
        for (int k = 0; k < stacks.size(); k++) {
            if (used[k] || !matches(offerings.get(i), stacks.get(k))) continue;
            used[k] = true; pick[i] = k;
            if (solve(i + 1, pick, used, stacks)) return true;
            used[k] = false;
        }
        return false;
    }

    public Item resultItem() {
        return result.isEmpty() ? null : BuiltInRegistries.ITEM.getValue(Identifier.parse(result));
    }
}
