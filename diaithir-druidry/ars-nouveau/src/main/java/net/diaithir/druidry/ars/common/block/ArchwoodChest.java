package net.diaithir.druidry.ars.common.block;

import net.diaithir.druidry.ars.common.block.tile.ArchwoodChestTile;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public class ArchwoodChest extends ChestBlock {
    public ArchwoodChest() {
        super(() -> BlockRegistry.ARCHWOOD_CHEST_TILE.get(), net.minecraft.sounds.SoundEvents.CHEST_OPEN, net.minecraft.sounds.SoundEvents.CHEST_CLOSE, net.diaithir.druidry.ars.setup.registry.BlockRegistry.of().strength(2.5F).sound(SoundType.WOOD));
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ArchwoodChestTile(pos, state);
    }

    public static class Item extends BlockItem {

        public Item(Block block, net.minecraft.world.item.Item.Properties props) {
            super(block, props);
        }
    }

}
