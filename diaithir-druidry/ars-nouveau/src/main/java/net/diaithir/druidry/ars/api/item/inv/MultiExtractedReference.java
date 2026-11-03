package net.diaithir.druidry.ars.api.item.inv;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** The merged result of extracting from several slots. */
public class MultiExtractedReference {
    public final ItemStack extracted;
    public final List<ExtractedStack> slots;

    public MultiExtractedReference(ItemStack extracted, List<ExtractedStack> slots) {
        this.extracted = extracted;
        this.slots = slots;
    }

    public boolean isEmpty() {
        return extracted.isEmpty();
    }

    public ItemStack getExtracted() {
        return extracted;
    }
}
