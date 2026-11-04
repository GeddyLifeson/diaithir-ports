package dev.shadowsoffire.apothic_enchanting.data;

import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.BlockStats;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Generates all {@link BlockStats} entries for the {@code apothic_enchanting:enchanting_stats}
 * dynamic registry. Replaces the hand-authored JSONs that previously lived under
 * {@code src/main/resources/data/apothic_enchanting/enchanting_stats/}.
 * <p>
 * All entries emit {@link HolderSet.Direct} block lists. The pre-port JSON schema supported a
 * {@code "tag": "..."} form via {@link HolderSet.Named}, but constructing a serializable Named set
 * at datagen time requires a {@link net.minecraft.core.HolderOwner} we don't have here, and the
 * only in-repo user of that form ({@code basic_skulls}) is a static 8-block list that's simpler to
 * expand inline. Users who want tag-based stats can hand-author their own JSON files — the loader
 * still parses the {@code #namespace:tag} shape at runtime via
 * {@link net.minecraft.core.RegistryCodecs#homogeneousList}.
 */
public class EnchStatsProvider extends DynamicRegistryProvider<BlockStats> {

    public EnchStatsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, EnchantingStatRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Apothic Enchanting Stats";
    }

    @Override
    public void generate() {
        // Natural-world entries.
        add("amethyst_cluster", single(Blocks.AMETHYST_CLUSTER), stats(40, 1, -1, 0, 0));

        // Skull entries — formerly backed by the apothic_enchanting:basic_skulls block tag, expanded
        // to a direct list here. See class javadoc for rationale.
        add("basic_skulls", direct(
            Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL,
            Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD,
            Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD,
            Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD),
            stats(0, 0, 5, 0, 0));
        add("wither_skull", direct(Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL), stats(0, 0, 10, 0, 0));

        // Apothic shelves — one Block each.
        add("dormant_deepshelf", single(Ench.Blocks.DORMANT_DEEPSHELF), stats(30, 2, 0, 0, 0));
        add("deepshelf", single(Ench.Blocks.DEEPSHELF), stats(70, 5, 5, 5, 0));
        add("echoing_deepshelf", single(Ench.Blocks.ECHOING_DEEPSHELF), stats(75, 5, 0, 15, 0));
        add("soul_touched_deepshelf", single(Ench.Blocks.SOUL_TOUCHED_DEEPSHELF), stats(75, 5, 15, 0, 0));
        add("echoing_sculkshelf", single(Ench.Blocks.ECHOING_SCULKSHELF), stats(80, 10, 10, 15, 1));
        add("soul_touched_sculkshelf", single(Ench.Blocks.SOUL_TOUCHED_SCULKSHELF), stats(80, 10, 15, 10, 1));

        add("hellshelf", single(Ench.Blocks.HELLSHELF), stats(45, 3, 3, 0, 0));
        add("infused_hellshelf", single(Ench.Blocks.INFUSED_HELLSHELF), stats(60, 5, 5, 0, 0));
        add("blazing_hellshelf", single(Ench.Blocks.BLAZING_HELLSHELF), stats(65, 10, 10, 0, -1));
        add("glowing_hellshelf", single(Ench.Blocks.GLOWING_HELLSHELF), stats(60, 5, 5, 3.3333F, 0));

        add("seashelf", single(Ench.Blocks.SEASHELF), stats(45, 3, 0, 3, 0));
        add("infused_seashelf", single(Ench.Blocks.INFUSED_SEASHELF), stats(60, 5, 0, 5, 0));
        add("crystal_seashelf", single(Ench.Blocks.CRYSTAL_SEASHELF), stats(60, 5, 3, 5, 0));
        add("heart_seashelf", single(Ench.Blocks.HEART_SEASHELF), stats(60, 15, 0, 20, 0));

        add("endshelf", single(Ench.Blocks.ENDSHELF), stats(90, 5, 5, 5, 0));
        add("pearl_endshelf", single(Ench.Blocks.PEARL_ENDSHELF), stats(90, 10, 7.5F, 7.5F, 0));
        add("draconic_endshelf", single(Ench.Blocks.DRACONIC_ENDSHELF), stats(100, 20, 0, 0, 0));

        add("basic_bookshelf", single(Ench.Blocks.BASIC_BOOKSHELF), stats(30F, 2, 0, 0, 0));
        add("beeshelf", single(Ench.Blocks.BEESHELF), stats(0, -30, 100, 0, 0));
        add("melonshelf", single(Ench.Blocks.MELONSHELF), stats(0, -2, -10, 0, 0));
        add("stoneshelf", single(Ench.Blocks.STONESHELF), stats(0, -3, 0, -7.5F, 0));

        add("sightshelf", single(Ench.Blocks.SIGHTSHELF), cluesOnly(1));
        add("sightshelf_t2", single(Ench.Blocks.SIGHTSHELF_T2), cluesOnly(2));
    }

    private void add(String path, HolderSet<Block> blocks, Stats stats) {
        this.add(ApothicEnchanting.loc(path), new BlockStats(blocks, stats));
    }

    @SuppressWarnings("deprecation")
    private static HolderSet<Block> single(Block block) {
        return HolderSet.direct(block.builtInRegistryHolder());
    }

    private static HolderSet<Block> single(Holder<Block> block) {
        return HolderSet.direct(block);
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private static HolderSet<Block> direct(Block... blocks) {
        Holder<Block>[] holders = new Holder[blocks.length];
        for (int i = 0; i < blocks.length; i++) {
            holders[i] = blocks[i].builtInRegistryHolder();
        }
        return HolderSet.direct(holders);
    }

    private static Stats stats(float maxEterna, float eterna, float quanta, float arcana, int clues) {
        return new Stats(maxEterna, eterna, quanta, arcana, clues);
    }

    private static Stats cluesOnly(int clues) {
        return new Stats(30F, 0, 0, 0, clues);
    }
}
