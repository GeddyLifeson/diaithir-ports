package net.diaithir.druidry.ars.common.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class TickableModBlock extends ModBlock implements ITickableBlock {

    public TickableModBlock() {
        this(defaultProperties());
    }

    public TickableModBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
