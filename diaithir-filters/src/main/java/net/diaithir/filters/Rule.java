package net.diaithir.filters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/** One node of a filter expression. Leaves test a stack, AND/OR/NOT combine children. */
public sealed interface Rule {

    boolean test(Probe p);

    int size();

    int depth();

    /** Everything a rule may ask about a stack, so the logic can be tested without a running game. */
    interface Probe {
        String itemId();

        boolean hasTag(String tagId);

        int durabilityPercent();

        boolean hasEnchantment(String enchantmentId);

        static Probe of(ItemStack s) {
            return new Probe() {
                public String itemId() {
                    return BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
                }

                public boolean hasTag(String tagId) {
                    String t = tagId.startsWith("#") ? tagId.substring(1) : tagId;
                    Identifier id = Identifier.tryParse(t);
                    if (id == null) return false;
                    return s.is(TagKey.<Item>create(Registries.ITEM, id));
                }

                public int durabilityPercent() {
                    if (!s.isDamageableItem()) return 100;
                    return (s.getMaxDamage() - s.getDamageValue()) * 100 / Math.max(1, s.getMaxDamage());
                }

                public boolean hasEnchantment(String id) {
                    return s.getEnchantments().keySet().stream().anyMatch(h -> h.unwrapKey().map(k -> k.identifier().toString().equals(id)).orElse(false));
                }
            };
        }
    }

    record ItemRule(String id) implements Rule {
        public boolean test(Probe p) {
            return p.itemId().equals(id);
        }

        public int size() {
            return 1;
        }

        public int depth() {
            return 1;
        }
    }

    record TagRule(String tag) implements Rule {
        public boolean test(Probe p) {
            return p.hasTag(tag);
        }

        public int size() {
            return 1;
        }

        public int depth() {
            return 1;
        }
    }

    record ModRule(String modId) implements Rule {
        public boolean test(Probe p) {
            String id = p.itemId();
            int c = id.indexOf(':');
            return (c < 0 ? "minecraft" : id.substring(0, c)).equals(modId.toLowerCase(Locale.ROOT));
        }

        public int size() {
            return 1;
        }

        public int depth() {
            return 1;
        }
    }

    /** Remaining durability in percent, inclusive range. Undamageable items count as 100. */
    record DurabilityRule(int min, int max) implements Rule {
        public boolean test(Probe p) {
            int d = p.durabilityPercent();
            return d >= min && d <= max;
        }

        public int size() {
            return 1;
        }

        public int depth() {
            return 1;
        }
    }

    record EnchantRule(String enchantment) implements Rule {
        public boolean test(Probe p) {
            return p.hasEnchantment(enchantment);
        }

        public int size() {
            return 1;
        }

        public int depth() {
            return 1;
        }
    }

    record And(List<Rule> children) implements Rule {
        public boolean test(Probe p) {
            for (Rule r : children) if (!r.test(p)) return false;
            return true;
        }

        public int size() {
            return 1 + children.stream().mapToInt(Rule::size).sum();
        }

        public int depth() {
            return 1 + children.stream().mapToInt(Rule::depth).max().orElse(0);
        }
    }

    record Or(List<Rule> children) implements Rule {
        public boolean test(Probe p) {
            for (Rule r : children) if (r.test(p)) return true;
            return false;
        }

        public int size() {
            return 1 + children.stream().mapToInt(Rule::size).sum();
        }

        public int depth() {
            return 1 + children.stream().mapToInt(Rule::depth).max().orElse(0);
        }
    }

    record Not(Rule child) implements Rule {
        public boolean test(Probe p) {
            return !child.test(p);
        }

        public int size() {
            return 1 + child.size();
        }

        public int depth() {
            return 1 + child.depth();
        }
    }

    MapCodec<ItemRule> ITEM_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.STRING.fieldOf("id").forGetter(ItemRule::id)).apply(i, ItemRule::new));
    MapCodec<TagRule> TAG_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.STRING.fieldOf("tag").forGetter(TagRule::tag)).apply(i, TagRule::new));
    MapCodec<ModRule> MOD_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.STRING.fieldOf("mod").forGetter(ModRule::modId)).apply(i, ModRule::new));
    MapCodec<DurabilityRule> DUR_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(0, 100).fieldOf("min").forGetter(DurabilityRule::min),
            Codec.intRange(0, 100).fieldOf("max").forGetter(DurabilityRule::max)).apply(i, DurabilityRule::new));
    MapCodec<EnchantRule> ENCH_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.STRING.fieldOf("enchantment").forGetter(EnchantRule::enchantment)).apply(i, EnchantRule::new));

    Codec<Rule> CODEC = Codec.recursive("diaithirfilters_rule", self -> {
        MapCodec<And> and = RecordCodecBuilder.mapCodec(i -> i.group(self.listOf().fieldOf("all").forGetter(And::children)).apply(i, And::new));
        MapCodec<Or> or = RecordCodecBuilder.mapCodec(i -> i.group(self.listOf().fieldOf("any").forGetter(Or::children)).apply(i, Or::new));
        MapCodec<Not> not = RecordCodecBuilder.mapCodec(i -> i.group(self.fieldOf("not").forGetter(Not::child)).apply(i, Not::new));
        List<MapCodec<? extends Rule>> all = List.of(ITEM_CODEC, TAG_CODEC, MOD_CODEC, DUR_CODEC, ENCH_CODEC, and, or, not);
        return new Codec<Rule>() {
            @Override
            public <T> DataResult<Pair<Rule, T>> decode(DynamicOps<T> ops, T input) {
                for (MapCodec<? extends Rule> c : all) {
                    DataResult<Pair<Rule, T>> r = c.codec().map(x -> (Rule) x).decode(ops, input);
                    if (r.result().isPresent()) return r;
                }
                return DataResult.error(() -> "Unknown filter rule");
            }

            @Override
            public <T> DataResult<T> encode(Rule rule, DynamicOps<T> ops, T prefix) {
                return switch (rule) {
                    case ItemRule r -> ITEM_CODEC.codec().encode(r, ops, prefix);
                    case TagRule r -> TAG_CODEC.codec().encode(r, ops, prefix);
                    case ModRule r -> MOD_CODEC.codec().encode(r, ops, prefix);
                    case DurabilityRule r -> DUR_CODEC.codec().encode(r, ops, prefix);
                    case EnchantRule r -> ENCH_CODEC.codec().encode(r, ops, prefix);
                    case And r -> and.codec().encode(r, ops, prefix);
                    case Or r -> or.codec().encode(r, ops, prefix);
                    case Not r -> not.codec().encode(r, ops, prefix);
                };
            }
        };
    });

    StreamCodec<RegistryFriendlyByteBuf, Rule> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    /** Short human readable form for tooltips and the editor. */
    static String describe(Rule r) {
        return switch (r) {
            case ItemRule x -> "item " + x.id();
            case TagRule x -> "tag " + x.tag();
            case ModRule x -> "mod " + x.modId();
            case DurabilityRule x -> "durability " + x.min() + "-" + x.max() + "%";
            case EnchantRule x -> "enchant " + x.enchantment();
            case And x -> join("AND", x.children());
            case Or x -> join("OR", x.children());
            case Not x -> "NOT (" + describe(x.child()) + ")";
        };
    }

    private static String join(String op, List<Rule> kids) {
        List<String> parts = new ArrayList<>();
        for (Rule k : kids) parts.add(describe(k));
        return "(" + String.join(" " + op + " ", parts) + ")";
    }

    /** Applies f to every child list element; used by the editor to rebuild trees. */
    static List<Rule> mapAll(List<Rule> in, Function<Rule, Rule> f) {
        List<Rule> out = new ArrayList<>();
        for (Rule r : in) out.add(f.apply(r));
        return out;
    }
}
