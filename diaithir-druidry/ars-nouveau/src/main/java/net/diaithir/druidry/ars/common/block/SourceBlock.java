package net.diaithir.druidry.ars.common.block;

import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class SourceBlock extends ModBlock implements EntityBlock {

    public SourceBlock(BlockBehaviour.Properties properties, String registry) {
        super(properties);
    }
}
