package dev.shadowsoffire.apothic_enchanting.table;

import javax.annotation.Nullable;

import dev.shadowsoffire.apothic_enchanting.api.EnchantmentStatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ApothEnchantingTableBlock extends EnchantingTableBlock {

    public ApothEnchantingTableBlock(Block.Properties props) {
        super(props);
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof EnchantingTableBlockEntity tile)) {
            return null;
        }
        Component title = ((Nameable) tile).getDisplayName();
        // Custom provider rather than SimpleMenuProvider so we can hook writeClientSideData and
        // ship the table's BlockPos to the client. The client-side menu factory (R.menuWithPos)
        // reads the pos back from the buf and the screen consults it to pick the GUI book texture.
        return new MenuProvider() {

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return new ApothEnchantmentMenu(id, inv, ContainerLevelAccess.create(world, pos), tile.getData(EnchantmentTableItemHandler.TYPE), pos);
            }

            @Override
            public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
                buf.writeBlockPos(pos);
            }
        };
    }

    /**
     * Normally we'd do this in {@link BlockEntity#preRemoveSideEffects(BlockPos, BlockState)}, but since
     * we don't own the block entity and instead use an attachment, we can't.
     */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity be) {
            EnchantmentTableItemHandler handler = be.getData(EnchantmentTableItemHandler.TYPE);
            ItemResource fuel = handler.getResource(0);
            int amount = handler.getAmountAsInt(0);
            if (!fuel.isEmpty() && amount > 0) {
                Block.popResource(level, pos, fuel.toStack(amount));
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        for (BlockPos offset : BOOKSHELF_OFFSETS) {
            BlockState shelfState = level.getBlockState(pos.offset(offset));
            ((EnchantmentStatBlock) shelfState.getBlock()).spawnTableParticle(shelfState, level, rand, pos, offset);
        }
    }

    public static ResourceHandler<ItemResource> getItemHandler(EnchantingTableBlockEntity be, Direction dir) {
        return be.getData(EnchantmentTableItemHandler.TYPE);
    }

}
