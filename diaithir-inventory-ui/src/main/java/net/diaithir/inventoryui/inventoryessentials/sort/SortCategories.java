package net.diaithir.inventoryui.inventoryessentials.sort;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class SortCategories {
    private SortCategories() {}

    public static String categoryOf(ItemStack s) {
        if (s.has(DataComponents.FOOD)) return "food";
        if (s.has(DataComponents.EQUIPPABLE)) return "armor";
        if (s.has(DataComponents.WEAPON)) return "weapons";
        if (s.has(DataComponents.TOOL)) return "tools";
        if (s.getItem() instanceof BlockItem) return "blocks";
        return "other";
    }

    public static int rank(ItemStack s, List<? extends String> order) {
        int i = order.indexOf(categoryOf(s));
        return i < 0 ? order.size() : i;
    }
}
