package net.diaithir.druidry.ars.common.block;

import net.diaithir.druidry.ars.common.block.tile.CreativeSourceJarTile;
import net.diaithir.druidry.ars.common.lib.LibBlockNames;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeSourceJar extends SourceJar {

    public CreativeSourceJar() {
        super(TickableModBlock.defaultProperties().noOcclusion(), LibBlockNames.CREATIVE_SOURCE_JAR);
        registerDefaultState(this.defaultBlockState().setValue(SourceJar.fill, 11));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeSourceJarTile(pos, state);
    }
}
