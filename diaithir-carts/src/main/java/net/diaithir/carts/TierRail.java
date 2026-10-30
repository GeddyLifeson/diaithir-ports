package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.RailBlock;

/** A plain rail (curves, slopes, junction logic all from the vanilla RailBlock) with its own speed cap. */
public class TierRail extends RailBlock implements RailSpeed {
    public static final MapCodec<RailBlock> CODEC = simpleCodec(p -> new TierRail(p, Tier.STEEL));
    private final Tier tier;

    public TierRail(Properties properties, Tier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public MapCodec<RailBlock> codec() {
        return CODEC;
    }

    @Override
    public double maxSpeed() {
        return tier.maxSpeed;
    }

    public Tier tier() {
        return tier;
    }
}
