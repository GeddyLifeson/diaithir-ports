package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.PoweredRailBlock;

/** A powered rail (redstone chaining from the vanilla PoweredRailBlock). The boost and the stop when unpowered are done in CartLogic, because vanilla checks for its own block. */
public class TierPoweredRail extends PoweredRailBlock implements RailSpeed {
    public static final MapCodec<PoweredRailBlock> CODEC = simpleCodec(p -> new TierPoweredRail(p, Tier.STEEL));
    private final Tier tier;

    public TierPoweredRail(Properties properties, Tier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public MapCodec<PoweredRailBlock> codec() {
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
