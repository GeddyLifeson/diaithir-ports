package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A pallet, barrel, chest or crate as a block. Breaking it keeps the items inside the dropped item (loot table copies the container component). */
public final class LoadBlock extends Block implements EntityBlock {
    public final LoadType type;

    public LoadBlock(Properties props, LoadType type) {
        super(props);
        this.type = type;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LoadBlockEntity(pos, state); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LoadBlockEntity be) player.openMenu(be);
        return InteractionResult.SUCCESS;
    }
}
