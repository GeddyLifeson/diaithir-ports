package net.diaithir.druidry.ars.api.item.inv;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** The result of inserting a stack into several inventories. */
public class MultiInsertReference {
    private final ItemStack remainder;
    public final List<SlotReference> slots;

    public MultiInsertReference(ItemStack remainder, List<SlotReference> slots) {
        this.remainder = remainder;
        this.slots = slots;
    }

    public ItemStack getRemainder() {
        return remainder;
    }
}
