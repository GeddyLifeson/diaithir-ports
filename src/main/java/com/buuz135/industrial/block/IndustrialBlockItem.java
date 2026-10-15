package com.buuz135.industrial.block;

import com.hrznstudio.titanium.module.DeferredRegistryHelper;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class IndustrialBlockItem extends BlockItem{
    private TitaniumTab group;

    public IndustrialBlockItem(Block blockIn, TitaniumTab group) {
        super(blockIn, DeferredRegistryHelper.applyItemRegistrationId(new Item.Properties().useBlockDescriptionPrefix()));
        this.group = group;
    }


    @Nullable
    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {
        return Component.translatable("itemGroup.industrialforegoing_" + this.group.getIdentifier().getPath()).getString();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (getBlock() instanceof IndustrialBlock<?> block) block.appendHoverText(stack, context, display, tooltip, flag);
    }
}
