package net.diaithir.steamrails;

import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class SteamBogeyBlockEntity extends AbstractBogeyBlockEntity {
    public SteamBogeyBlockEntity(BlockPos pos, BlockState state) {
        super(SteamBlocks.BOGEY_BE.get(), pos, state);
    }

    @Override
    public BogeyStyle getDefaultStyle() {
        return ((AbstractBogeyBlock<?>) getBlockState().getBlock()).getDefaultStyle();
    }
}
