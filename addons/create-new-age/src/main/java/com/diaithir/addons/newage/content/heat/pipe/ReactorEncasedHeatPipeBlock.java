package com.diaithir.addons.newage.content.heat.pipe;

import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class ReactorEncasedHeatPipeBlock extends EncasedHeatPipeBlock{

    public ReactorEncasedHeatPipeBlock(Properties properties, Supplier<Block> casing) {
        super(properties.strength(6.0f), casing);
    }

}
