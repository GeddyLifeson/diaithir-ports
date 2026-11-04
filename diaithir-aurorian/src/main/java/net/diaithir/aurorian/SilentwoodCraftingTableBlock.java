package net.diaithir.aurorian;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SilentwoodCraftingTableBlock extends CraftingTableBlock {
    public SilentwoodCraftingTableBlock(Properties p) {
        super(p);
    }

    @Override
    public MapCodec<CraftingTableBlock> codec() {
        return simpleCodec(SilentwoodCraftingTableBlock::new);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inv, pl) -> new Menu(id, inv, ContainerLevelAccess.create(level, pos), state.getBlock()), Component.translatable("container.crafting"));
    }

    /** The vanilla menu closes unless the block is a vanilla crafting table. */
    private static final class Menu extends CraftingMenu {
        private final ContainerLevelAccess access;
        private final net.minecraft.world.level.block.Block block;

        Menu(int id, Inventory inv, ContainerLevelAccess access, net.minecraft.world.level.block.Block block) {
            super(id, inv, access);
            this.access = access;
            this.block = block;
        }

        @Override
        public boolean stillValid(Player player) {
            return stillValid(access, player, block);
        }
    }
}
