package net.diaithir.classes;

import net.minecraft.world.item.Item;

/** An Ogham rune: a consumable that is pressed into a ring or charm (right-click the rune onto it in an inventory). */
public final class RuneItem extends Item {
    public RuneItem(Properties p) {
        super(p);
    }

    public String runeId() {
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath();
        return path.substring("rune_".length());
    }
}
