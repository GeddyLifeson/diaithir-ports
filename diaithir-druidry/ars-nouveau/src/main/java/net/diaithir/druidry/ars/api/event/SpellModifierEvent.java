package net.diaithir.druidry.ars.api.event;

import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.diaithir.druidry.ars.api.spell.SpellStats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;

/**
 * Fired before effects are resolved. Attach any items, augments, or number modifiers to the stats builder at this phase.
 */
public class SpellModifierEvent extends Event {

    public SpellStats.Builder builder;
    public @Nullable LivingEntity caster;
    public AbstractSpellPart spellPart;
    public HitResult rayTraceResult;
    public Level world;
    public SpellContext spellContext;

    public SpellModifierEvent(LivingEntity caster, SpellStats.Builder builder, AbstractSpellPart spellPart, HitResult rayTraceResult, Level world, SpellContext spellContext) {
        this.caster = caster;
        this.builder = builder;
        this.spellPart = spellPart;
        this.rayTraceResult = rayTraceResult;
        this.world = world;
        this.spellContext = spellContext;
    }
}
