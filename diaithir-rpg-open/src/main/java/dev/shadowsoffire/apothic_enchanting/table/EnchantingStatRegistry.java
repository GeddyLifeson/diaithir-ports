package dev.shadowsoffire.apothic_enchanting.table;

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.api.EnchantmentStatBlock;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.BlockStats;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import dev.shadowsoffire.placebo.dynreg.RegistrySerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantingStatRegistry extends DynamicRegistry<BlockStats> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final EnchantingStatRegistry INSTANCE = new EnchantingStatRegistry();

    private Map<Holder<Block>, Stats> statsPerBlock = Map.of();
    private boolean statsCacheDirty = true;

    protected EnchantingStatRegistry() {
        super(ApothicEnchanting.LOGGER, ApothicEnchanting.loc("enchanting_stats"), RegistrySerializer.synced(BlockStats.CODEC));
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.statsPerBlock = Map.of();
        this.statsCacheDirty = true;
    }

    /**
     * Flattens all loaded {@link BlockStats} entries into a direct {@code Holder<Block> -> Stats}
     * lookup map. Deferred to first access rather than built in {@code onReload} because
     * {@link HolderSet.Named} entries can't be iterated until after tag binding completes.
     */
    private void buildStatsCache() {
        var map = new IdentityHashMap<Holder<Block>, Stats>();
        for (BlockStats entry : this.registry.values()) {
            for (Holder<Block> holder : entry.blocks) {
                map.putIfAbsent(holder, entry.stats);
            }
        }
        this.statsPerBlock = map;
        this.statsCacheDirty = false;
    }

    @SuppressWarnings("deprecation")
    private static Stats lookupStats(BlockState state) {
        if (INSTANCE.statsCacheDirty) {
            INSTANCE.buildStatsCache();
        }
        return INSTANCE.statsPerBlock.get(state.getBlock().builtInRegistryHolder());
    }

    /**
     * Retrieves the Eterna value for a specific block.
     * This can be provided by a stat file, or {@link BlockState#getEnchantPowerBonus}
     * 1 Eterna = +1 level in the enchanting table.
     */
    public static float getEterna(BlockState state, BlockGetter level, BlockPos pos) {
        Stats stats = lookupStats(state);
        if (stats != null) return stats.eterna;
        return state.getBlock().getEnchantPowerBonus(state, level, pos) * 2;
    }

    /**
     * Retrieves the Max Eterna value for a specific block.
     * This can be provided by a stat file, or {@link EnchantmentStatBlock#getMaxEnchantingPower}
     * 1F of Eterna = 2 Levels in the enchanting table.
     */
    public static float getMaxEterna(BlockState state, BlockGetter level, BlockPos pos) {
        Stats stats = lookupStats(state);
        if (stats != null) return stats.maxEterna;
        return ((EnchantmentStatBlock) state.getBlock()).getMaxEnchantingPower(state, level, pos);
    }

    /**
     * Retrieves the Quanta value for a specific block.
     * This can be provided by a stat file, or {@link EnchantmentStatBlock#getQuantaBonus}
     * 1F of Quanta = 1% of Quanta in the enchanting table.
     */
    public static float getQuanta(BlockState state, BlockGetter level, BlockPos pos) {
        Stats stats = lookupStats(state);
        if (stats != null) return stats.quanta;
        return ((EnchantmentStatBlock) state.getBlock()).getQuantaBonus(state, level, pos);
    }

    /**
     * Retrieves the Arcana value for a specific block.
     * This can be provided by a stat file, or {@link EnchantmentStatBlock#getArcanaBonus}
     * 1F of Arcana = 1% of Arcana in the enchanting table.
     */
    public static float getArcana(BlockState state, BlockGetter level, BlockPos pos) {
        Stats stats = lookupStats(state);
        if (stats != null) return stats.arcana;
        return ((EnchantmentStatBlock) state.getBlock()).getArcanaBonus(state, level, pos);
    }

    /**
     * Retrieves the number of bonus clues this block provides.
     * See {@link EnchantmentStatBlock#getBonusClues}
     */
    public static int getBonusClues(BlockState state, BlockGetter level, BlockPos pos) {
        Stats stats = lookupStats(state);
        if (stats != null) return stats.clues;
        return ((EnchantmentStatBlock) state.getBlock()).getBonusClues(state, level, pos);
    }

    /**
     * Data-backed enchanting stats. Objects may provide stats via this or by overriding methods in {@link EnchantmentStatBlock}.
     *
     * @Param maxEterna The max value of eterna that may be contributed to. Uses a step-ladder system.
     * @param eterna The amount of eterna provided.
     * @param quanta The amount of quanta provided.
     * @param arcana The amount of arcana provided.
     * @param clues  The number of enchanting clues provided.
     */
    public static record Stats(float maxEterna, float eterna, float quanta, float arcana, int clues) {

        public static Codec<Stats> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.FLOAT.optionalFieldOf("maxEterna", 30F).forGetter(Stats::maxEterna),
                Codec.FLOAT.optionalFieldOf("eterna", 0F).forGetter(Stats::eterna),
                Codec.FLOAT.optionalFieldOf("quanta", 0F).forGetter(Stats::quanta),
                Codec.FLOAT.optionalFieldOf("arcana", 0F).forGetter(Stats::arcana),
                Codec.INT.optionalFieldOf("clues", 0).forGetter(Stats::clues))
            .apply(inst, Stats::new));

        public static StreamCodec<FriendlyByteBuf, Stats> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, Stats::maxEterna,
            ByteBufCodecs.FLOAT, Stats::eterna,
            ByteBufCodecs.FLOAT, Stats::quanta,
            ByteBufCodecs.FLOAT, Stats::arcana,
            ByteBufCodecs.VAR_INT, Stats::clues,
            Stats::new);
    }

    public static class BlockStats {

        public static Codec<BlockStats> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(bs -> bs.blocks),
                Stats.CODEC.fieldOf("stats").forGetter(bs -> bs.stats))
            .apply(inst, BlockStats::new));

        public final HolderSet<Block> blocks;
        public final Stats stats;

        public BlockStats(HolderSet<Block> blocks, Stats stats) {
            this.blocks = blocks;
            this.stats = stats;
        }

    }

}
