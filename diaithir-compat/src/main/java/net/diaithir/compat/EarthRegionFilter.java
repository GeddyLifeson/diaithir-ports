package net.diaithir.compat;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Placement filter for wild flora: on the Tellus Earth dimension a feature only places where the earth_ranges entry
 * {@code id} contains the position's latitude and longitude. Other dimensions are not filtered.
 */
public class EarthRegionFilter extends PlacementFilter {
    public static final MapCodec<EarthRegionFilter> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            com.mojang.serialization.Codec.STRING.fieldOf("id").forGetter(f -> f.id)).apply(i, EarthRegionFilter::new));
    public static final PlacementModifierType<EarthRegionFilter> TYPE = () -> CODEC;
    private final String id;

    public EarthRegionFilter(String id) {
        this.id = id;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return EarthRangeGate.allowsAt(context.generator(), this.id, pos.getX(), pos.getZ());
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
