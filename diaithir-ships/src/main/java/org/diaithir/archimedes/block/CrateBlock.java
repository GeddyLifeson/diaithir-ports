package org.diaithir.archimedes.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import org.diaithir.archimedes.ArchimedesMod;
import org.diaithir.archimedes.ShipChunk;

/** Wooden crate with 27 slots; its contents travel with the ship. */
public class CrateBlock extends BaseEntityBlock {
    public static final MapCodec<CrateBlock> CODEC = simpleCodec(CrateBlock::new);

    public CrateBlock(Properties props) { super(props); }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new Entity(pos, state); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof Entity be) player.openMenu(be);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean moved) {
        net.minecraft.world.Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    public static class Entity extends BaseContainerBlockEntity {
        public NonNullList<ItemStack> items = NonNullList.withSize(ShipChunk.CRATE_SLOTS, ItemStack.EMPTY);

        public Entity(BlockPos pos, BlockState state) { super(ArchimedesMod.CRATE_BE.get(), pos, state); }

        @Override protected Component getDefaultName() { return Component.translatable("block.archimedes.crate"); }
        @Override protected NonNullList<ItemStack> getItems() { return items; }
        @Override protected void setItems(NonNullList<ItemStack> list) { items = list; }
        @Override public int getContainerSize() { return ShipChunk.CRATE_SLOTS; }

        @Override
        protected AbstractContainerMenu createMenu(int id, Inventory inv) { return ChestMenu.threeRows(id, inv, this); }

        @Override
        protected void loadAdditional(ValueInput in) {
            super.loadAdditional(in);
            items = NonNullList.withSize(ShipChunk.CRATE_SLOTS, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(in, items);
        }

        @Override
        protected void saveAdditional(ValueOutput out) {
            super.saveAdditional(out);
            ContainerHelper.saveAllItems(out, items);
        }
    }
}
