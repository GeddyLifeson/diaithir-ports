package net.diaithir.druidry.ars.common.block;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;

public class MagicLeaves extends UntintedParticleLeavesBlock {
    public MagicLeaves(Properties properties) {
        super(0.01F, ParticleTypes.CHERRY_LEAVES, properties);
    }
}
