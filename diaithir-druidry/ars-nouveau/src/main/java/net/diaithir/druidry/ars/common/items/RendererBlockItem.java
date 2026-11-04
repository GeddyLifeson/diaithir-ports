package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.setup.registry.BlockRegistryWrapper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/** Port note: geckolib item renderer support removed; this is now a plain block item. */
public class RendererBlockItem extends BlockItem {

    public RendererBlockItem(Block block, Properties props) {
        super(block, props);
    }

    public RendererBlockItem(BlockRegistryWrapper<? extends Block> block, Properties props) {
        this(block.get(), props);
    }
}
