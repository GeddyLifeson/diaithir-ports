package net.diaithir.druidry.ars.api.block;

import net.diaithir.druidry.ars.common.block.tile.ArcanePedestalTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface IPedestalMachine {
    default List<BlockPos> pedestalList(BlockPos blockPos, int offset, @NotNull Level level) {
        ArrayList<BlockPos> posList = new ArrayList<>();
        for (BlockPos b : BlockPos.betweenClosed(blockPos.offset(offset, -offset, offset), blockPos.offset(-offset, offset, -offset))) {
            if (level.getBlockEntity(b) instanceof ArcanePedestalTile tile) {
                posList.add(b.immutable());
            }
        }
        return posList;
    }

    void lightPedestal(Level level);
}
