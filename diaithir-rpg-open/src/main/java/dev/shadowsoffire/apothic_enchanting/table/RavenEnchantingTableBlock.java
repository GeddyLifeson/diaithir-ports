package dev.shadowsoffire.apothic_enchanting.table;

import javax.annotation.Nullable;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * End-game table that allows manually setting eterna/quanta/arcana.
 */
public class RavenEnchantingTableBlock extends ApothEnchantingTableBlock implements BookTexturedTable {

    public static final Identifier BOOK_TEXTURE_ID = ApothicEnchanting.loc("raven_book");

    public RavenEnchantingTableBlock(Properties props) {
        super(props);
    }

    @Override
    public Identifier getBookTextureId() {
        return BOOK_TEXTURE_ID;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof EnchantingTableBlockEntity tile)) {
            return null;
        }
        Component title = ((Nameable) tile).getDisplayName();
        RavenTableStats stats = tile.getData(RavenTableStats.TYPE);
        EnchantmentTableItemHandler teInv = tile.getData(EnchantmentTableItemHandler.TYPE);
        return new MenuProvider(){

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return new RavenEnchantmentMenu(id, inv, ContainerLevelAccess.create(world, pos), teInv, pos, stats);
            }

            @Override
            public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
                buf.writeBlockPos(pos);
                buf.writeInt(stats.eterna());
                buf.writeInt(stats.quanta());
                buf.writeInt(stats.arcana());
            }
        };
    }

}
