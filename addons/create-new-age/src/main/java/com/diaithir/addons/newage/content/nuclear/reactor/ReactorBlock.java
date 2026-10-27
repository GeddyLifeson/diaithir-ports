package com.diaithir.addons.newage.content.nuclear.reactor;

import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ReactorBlock extends Block implements IWrenchable {
    public ReactorBlock(Properties properties) {
        super(properties.strength(6.0f));
    }

}
