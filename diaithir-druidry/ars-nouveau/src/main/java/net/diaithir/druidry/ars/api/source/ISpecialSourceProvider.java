package net.diaithir.druidry.ars.api.source;

import net.minecraft.core.BlockPos;

public interface ISpecialSourceProvider {

    ISourceTile getSource();

    boolean isValid();

    BlockPos getCurrentPos();

}
