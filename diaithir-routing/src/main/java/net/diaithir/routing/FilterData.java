package net.diaithir.routing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Shared filter value: up to 9 entries, whitelist or blacklist, optional tag, durability and NBT-ignore matching. Immutable. */
public record FilterData(List<ItemStack> entries, boolean blacklist, boolean matchTag, boolean matchDurability, boolean ignoreData) {
    public static final int MAX_ENTRIES = 9;
    public static final FilterData EMPTY = new FilterData(List.of(), false, false, false, false);

    public static final Codec<FilterData> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.CODEC.listOf().fieldOf("entries").forGetter(FilterData::entries),
            Codec.BOOL.optionalFieldOf("blacklist", false).forGetter(FilterData::blacklist),
            Codec.BOOL.optionalFieldOf("match_tag", false).forGetter(FilterData::matchTag),
            Codec.BOOL.optionalFieldOf("match_durability", false).forGetter(FilterData::matchDurability),
            Codec.BOOL.optionalFieldOf("ignore_data", false).forGetter(FilterData::ignoreData)).apply(i, FilterData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FilterData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), FilterData::entries,
            ByteBufCodecs.BOOL, FilterData::blacklist,
            ByteBufCodecs.BOOL, FilterData::matchTag,
            ByteBufCodecs.BOOL, FilterData::matchDurability,
            ByteBufCodecs.BOOL, FilterData::ignoreData,
            FilterData::new);

    /** True if the stack passes. An empty entry list lets everything through, whatever the mode. */
    public boolean allows(ItemStack stack) {
        if (entries.isEmpty()) return true;
        return anyMatch(stack) != blacklist;
    }

    /** True if a whitelist filter names this stack: the owner of the filter wants it. */
    public boolean wants(ItemStack stack) {
        return !blacklist && !entries.isEmpty() && anyMatch(stack);
    }

    private boolean anyMatch(ItemStack stack) {
        for (ItemStack e : entries) if (matches(e, stack)) return true;
        return false;
    }

    private boolean matches(ItemStack entry, ItemStack stack) {
        if (entry.isEmpty() || stack.isEmpty()) return false;
        if (!ItemStack.isSameItem(entry, stack)) {
            if (!matchTag) return false;
            if (!sharesTag(entry, stack)) return false;
        }
        if (matchDurability && entry.getDamageValue() != stack.getDamageValue()) return false;
        if (!ignoreData && ItemStack.isSameItem(entry, stack) && !ItemStack.isSameItemSameComponents(entry, stack)) {
            return matchDurability && sameExceptDamage(entry, stack);
        }
        return true;
    }

    private static boolean sameExceptDamage(ItemStack a, ItemStack b) {
        ItemStack ca = a.copy();
        ItemStack cb = b.copy();
        ca.remove(DataComponents.DAMAGE);
        cb.remove(DataComponents.DAMAGE);
        return ItemStack.isSameItemSameComponents(ca, cb);
    }

    private static boolean sharesTag(ItemStack a, ItemStack b) {
        return a.typeHolder().tags().anyMatch(t -> b.typeHolder().is(t));
    }

    public FilterData withToggled(ItemStack stack) {
        List<ItemStack> next = new ArrayList<>(entries);
        boolean removed = next.removeIf(e -> ItemStack.isSameItemSameComponents(e, stack));
        if (!removed) {
            if (next.size() >= MAX_ENTRIES) return this;
            next.add(stack.copyWithCount(1));
        }
        return new FilterData(List.copyOf(next), blacklist, matchTag, matchDurability, ignoreData);
    }

    public FilterData withBlacklist(boolean b) {
        return new FilterData(entries, b, matchTag, matchDurability, ignoreData);
    }

    public FilterData withMatchTag(boolean b) {
        return new FilterData(entries, blacklist, b, matchDurability, ignoreData);
    }

    public FilterData withMatchDurability(boolean b) {
        return new FilterData(entries, blacklist, matchTag, b, ignoreData);
    }

    public FilterData withIgnoreData(boolean b) {
        return new FilterData(entries, blacklist, matchTag, matchDurability, b);
    }

    public String describe() {
        StringBuilder sb = new StringBuilder(blacklist ? "blacklist" : "whitelist");
        if (matchTag) sb.append(" +tag");
        if (matchDurability) sb.append(" +durability");
        if (ignoreData) sb.append(" +ignore-data");
        sb.append(": ");
        if (entries.isEmpty()) return sb.append("(empty, lets everything through)").toString();
        for (int i = 0; i < entries.size(); i++) sb.append(i == 0 ? "" : ", ").append(entries.get(i).getHoverName().getString());
        return sb.toString();
    }
}
