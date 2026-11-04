package dev.shadowsoffire.apothic_enchanting.data;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.EnchantmentInfo;
import dev.shadowsoffire.apothic_enchanting.PowerFunction.DefaultMaxPowerFunction;
import dev.shadowsoffire.apothic_enchanting.PowerFunction.DefaultMinPowerFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.data.DataMapProvider;

/**
 * Datamap provider for Apothic Enchanting. Every enchantment falls back to {@link EnchantmentInfo#EMPTY} when no
 * entry is present.
 * Use {@code /apoth dump_enchantment_info} in-game to capture the full effective state of every enchantment as a
 * datapack-ready JSON.
 */
public class ApothEnchDataMapProvider extends DataMapProvider {

    public ApothEnchDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<EnchantmentInfo, Enchantment> builder = this.builder(Ench.DataMaps.ENCHANTMENT_INFO);

        // We lock down some of our enchantments so they don't spawn at max level randomly.
        this.addLootClamp(builder, Ench.Enchantments.BERSERKERS_FURY, 1);
        this.addLootClamp(builder, Ench.Enchantments.BOON_OF_THE_EARTH, 2);
        this.addLootClamp(builder, Ench.Enchantments.KNOWLEDGE_OF_THE_AGES, 1);
        this.addLootClamp(builder, Ench.Enchantments.LIFE_MENDING, 1);
        this.addLootClamp(builder, Ench.Enchantments.MINERS_FERVOR, 2);
        this.addLootClamp(builder, Ench.Enchantments.SCAVENGER, 1);
    }

    private void addLootClamp(Builder<EnchantmentInfo, Enchantment> builder, ResourceKey<Enchantment> key, int lootCap) {
        EnchantmentInfo info = new EnchantmentInfo(
            Optional.empty(),
            Optional.of(lootCap),
            Optional.empty(),
            -1,
            DefaultMaxPowerFunction.INSTANCE,
            DefaultMinPowerFunction.INSTANCE);
        builder.add(key, info, false);
    }

}
