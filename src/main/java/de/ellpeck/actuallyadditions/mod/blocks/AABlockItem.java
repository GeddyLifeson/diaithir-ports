package de.ellpeck.actuallyadditions.mod.blocks;

import net.minecraft.world.item.Item;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import de.ellpeck.actuallyadditions.mod.components.ActuallyComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;

public class AABlockItem extends BlockItem {
    public AABlockItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    public static class AASeedItem extends BlockItem {
        public AASeedItem(Block block, Properties properties) {
            super(block, properties);
        }
    }

    public static class BlockEntityEnergyItem extends AABlockItem {
        public BlockEntityEnergyItem(Block blockIn, Properties builder) {
            super(blockIn, builder);
        }

        @Override
        public void appendHoverText(ItemStack pStack, @Nullable Item.TooltipContext pContext, TooltipDisplay display, Consumer<Component> pTooltip, TooltipFlag pFlag) {
            super.appendHoverText(pStack, pContext, display, pTooltip, pFlag);

            if (pStack.has(ActuallyComponents.ENERGY_STORAGE)) {
                int energy = pStack.getOrDefault(ActuallyComponents.ENERGY_STORAGE, 0);
                NumberFormat format = NumberFormat.getInstance();
                pTooltip.accept(Component.translatable("misc.actuallyadditions.power_single", format.format(energy)));
            }
        }
    }
}
