package net.diaithir.druidry.ars.api.spell;

import net.minecraft.world.item.ItemStack;

public interface ItemCasterProvider {

    AbstractCaster<?> getSpellCaster(ItemStack stack);
}
