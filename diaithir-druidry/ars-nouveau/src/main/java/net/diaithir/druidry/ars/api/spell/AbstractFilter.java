package net.diaithir.druidry.ars.api.spell;

import net.diaithir.druidry.ars.api.documentation.DocAssets;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Base class for interfacing with filter glyphs from addons and making your own. Based off TooManyGlyphs.
 */
public abstract class AbstractFilter extends AbstractEffect implements IFilter {

    public AbstractFilter(Identifier registryName, String name) {
        super(registryName, name);
    }

    @Override
    public int getDefaultManaCost() {
        return 0;
    }

    @NotNull
    @Override
    protected Set<AbstractAugment> getCompatibleAugments() {
        return Set.of();
    }

    @Override
    public Integer getTypeIndex() {
        return 15;
    }

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        if (!shouldResolveOnEntity(rayTraceResult, world, spellStats, spellContext, resolver)) spellContext.setCanceled(true, CancelReason.FILTER_FAILED);
    }

    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        if (!shouldResolveOnBlock(rayTraceResult, world, spellStats, spellContext, resolver)) spellContext.setCanceled(true, CancelReason.FILTER_FAILED);
    }

    @Override
    public DocAssets.BlitInfo getTypeIcon() {
        return DocAssets.NA_ICON;
    }
}
