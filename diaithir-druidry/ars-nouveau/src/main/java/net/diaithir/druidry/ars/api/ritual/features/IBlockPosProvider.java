package net.diaithir.druidry.ars.api.ritual.features;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public interface IBlockPosProvider {

    BlockPos computeNext();

    CompoundTag serialize(CompoundTag tag);

}
