package net.diaithir.druidry.ars.common.items.itemscrolls;

import net.diaithir.druidry.ars.common.items.ItemScroll;
import net.diaithir.druidry.ars.common.items.data.ItemScrollData;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class AllowItemScroll extends ItemScroll {

    public AllowItemScroll() {
        super();
    }

    public AllowItemScroll(Properties properties) {
        super(properties);
    }

    @Override
    public SortPref getSortPref(ItemStack stackToStore, ItemStack scrollStack, IItemHandler inventory) {
        ItemScrollData data = scrollStack.getOrDefault(DataComponentRegistry.ITEM_SCROLL_DATA, new ItemScrollData());
        return data.containsStack(stackToStore) ? SortPref.HIGHEST : SortPref.INVALID;
    }
}
