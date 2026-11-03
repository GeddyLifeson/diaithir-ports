package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class IntangibleAirTile extends ModdedTile implements ITickable {
    public int duration;
    public int maxLength;
    public int stateID;

    public IntangibleAirTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.INTANGIBLE_AIR_TYPE, pos, state);
    }

    @Override
    public void tick() {
        if (level.isClientSide())
            return;
        duration++;
        if (duration > maxLength) {
            level.setBlockAndUpdate(worldPosition, Block.stateById(stateID));

        }
        level.sendBlockUpdated(this.worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        stateID = tag.getIntOr("state_id", 0);
        duration = tag.getIntOr("duration", 0);
        maxLength = tag.getIntOr("max_length", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("state_id", stateID);
        tag.putInt("duration", duration);
        tag.putInt("max_length", maxLength);
    }

}
