package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.common.capability.SourceStorage;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CreativeSourceJarTile extends SourceJarTile {

    @Override
    protected @NotNull SourceStorage createDefaultStorage() {
        return new SourceStorage(1000000, 1000000, 1000000, 1000000) {

            // Acts as void or infinite source, overrides method without doing checks or changes
            @Override
            public int receiveSource(int toReceive, boolean simulate) {
                return toReceive;
            }

            @Override
            public int extractSource(int toExtract, boolean simulate) {
                return toExtract;
            }

            @Override
            public int getSource() {
                return 1000000;
            }
        };
    }

    public CreativeSourceJarTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.CREATIVE_SOURCE_JAR_TILE.get(), pos, state);
    }
}
