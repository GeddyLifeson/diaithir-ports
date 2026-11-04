package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.compat.TooltipList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * Block items for this mod's blocks. In 26.1 blocks no longer provide item tooltips, so this passes the
 * request through to {@link AbstractPneumaticCraftBlock#appendTooltip}.
 */
public class PNCBlockItem extends BlockItem {
    public PNCBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        if (getBlock() instanceof AbstractPneumaticCraftBlock b) {
            b.appendTooltip(stack, context, TooltipList.of(builder), flag);
        }
    }
}
