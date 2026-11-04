package net.diaithir.druidry.ars.api.item;

import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.diaithir.druidry.ars.api.spell.SpellStats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

public interface ISpellModifierItem extends ISpellModifier {

    /**
     * A helper method for when used by an item.
     */
    default SpellStats.Builder applyItemModifiers(ItemStack stack, SpellStats.Builder builder, AbstractSpellPart spellPart, HitResult rayTraceResult, Level world, @Nullable LivingEntity shooter, SpellContext spellContext) {
        return applyModifiers(builder, spellPart, rayTraceResult, world, shooter, spellContext);
    }

}
