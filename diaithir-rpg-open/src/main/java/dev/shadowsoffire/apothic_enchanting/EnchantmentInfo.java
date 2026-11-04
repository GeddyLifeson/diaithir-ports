package dev.shadowsoffire.apothic_enchanting;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_enchanting.PowerFunction.DefaultMaxPowerFunction;
import dev.shadowsoffire.apothic_enchanting.PowerFunction.DefaultMinPowerFunction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * EnchantmentInfo retains all configurable per-enchantment data.
 * 
 * @param maxLevel             The max level. Falls back to {@link ApothicEnchanting#getDefaultMaxLevel(Holder)}, which computes the max level from the power
 *                             function.
 * @param maxLootLevel         The max level at which the enchantment may spawn at in loot. Falls back to the vanilla (unscaled) max level.
 * @param maxAnvilCombineLevel The max level the enchantment may be combined (N + N -> N+1) in an anvil. Falls back to the max level.
 * @param levelCap             A strict level cap that forces the enchantment to never go above the specified cap, bypassing things like custom NBT or runtime
 *                             bonuses.
 * @param maxPower             The maximum power function.
 * @param minPower             The minimum power function.
 */
public record EnchantmentInfo(Optional<Integer> maxLevel, Optional<Integer> maxLootLevel, Optional<Integer> maxAnvilCombineLevel, int levelCap, PowerFunction maxPower, PowerFunction minPower) {

    public static final EnchantmentInfo EMPTY = new EnchantmentInfo(Optional.empty(), Optional.empty(), Optional.empty(), -1, DefaultMaxPowerFunction.INSTANCE, DefaultMinPowerFunction.INSTANCE);

    // These clamp to 127 because we only supply language translations for up to 127. Technically vanilla supports up to 255 but meh.
    public static final MapCodec<EnchantmentInfo> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Codec.intRange(1, 127).optionalFieldOf("max_level").forGetter(EnchantmentInfo::maxLevel),
        Codec.intRange(1, 127).optionalFieldOf("max_loot_level").forGetter(EnchantmentInfo::maxLootLevel),
        Codec.intRange(1, 127).optionalFieldOf("max_anvil_combine_level").forGetter(EnchantmentInfo::maxAnvilCombineLevel),
        Codec.intRange(-1, 127).optionalFieldOf("level_cap", -1).forGetter(EnchantmentInfo::levelCap),
        PowerFunction.CODEC.codec().optionalFieldOf("max_power", DefaultMaxPowerFunction.INSTANCE).forGetter(EnchantmentInfo::maxPower),
        PowerFunction.CODEC.codec().optionalFieldOf("min_power", DefaultMinPowerFunction.INSTANCE).forGetter(EnchantmentInfo::minPower))
        .apply(inst, EnchantmentInfo::new));

    /**
     * Returns the max level of the enchantment. Falls back to {@link ApothicEnchanting#getDefaultMaxLevel} (cached)
     * if no override is set.
     */
    public int getMaxLevel(Holder<Enchantment> ench) {
        return this.maxLevel.orElseGet(() -> ApothicEnchanting.getDefaultMaxLevel(ench));
    }

    /**
     * Returns the max loot level of the enchantment. Loot level is used by loot table generation and villager trades.
     * Falls back to the enchantment's vanilla max level if no override is set.
     */
    public int getMaxLootLevel(Holder<Enchantment> ench) {
        return this.maxLootLevel.orElseGet(() -> ench.value().getMaxLevel());
    }

    /**
     * Returns the highest level this enchantment may be combined up to in an anvil. This caps <i>only</i> the case
     * where the enchantment is already present on both anvil inputs.
     * Defaults to {@link #getMaxLevel(Holder)}.
     */
    public int getMaxAnvilCombineLevel(Holder<Enchantment> ench) {
        return this.maxAnvilCombineLevel.orElseGet(() -> this.getMaxLevel(ench));
    }

    /**
     * Minimum enchanting power required to receive {@code level} of this enchantment in an enchanting table.
     */
    public int getMinPower(int level, Holder<Enchantment> ench) {
        return this.minPower.getPower(level, ench);
    }

    /**
     * Maximum enchanting power required to receive {@code level} of this enchantment in an enchanting table.
     * By default returns 200 for all levels.
     */
    public int getMaxPower(int level, Holder<Enchantment> ench) {
        return this.maxPower.getPower(level, ench);
    }

    /**
     * Fallback used when no datamap entry exists for an enchantment. Returns the shared {@link #EMPTY} instance —
     * actual default values are computed lazily by the getters above, allowing the expensive
     * {@link ApothicEnchanting#getDefaultMaxLevel} call to be memoized in one place.
     */
    public static EnchantmentInfo fallback(Holder<Enchantment> ench) {
        return EMPTY;
    }

}
