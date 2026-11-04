package net.diaithir.druidry.ars.common.spell.effect;

import net.diaithir.druidry.ars.api.block.IPedestalMachine;
import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.common.lib.GlyphLib;
import net.diaithir.druidry.ars.common.spell.augment.AugmentDurationDown;
import net.diaithir.druidry.ars.common.spell.augment.AugmentExtendTime;
import net.diaithir.druidry.ars.setup.registry.ModPotions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class EffectSenseMagic extends AbstractEffect implements IPotionEffect {

    public static EffectSenseMagic INSTANCE = new EffectSenseMagic(GlyphLib.EffectSenseMagicID, "Sense Magic");

    public EffectSenseMagic(String tag, String description) {
        super(tag, description);
    }

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        if (!(rayTraceResult.getEntity() instanceof LivingEntity living))
            return;
        ((IPotionEffect)this).applyConfigPotion(living, ModPotions.MAGIC_FIND_EFFECT, spellStats);
    }

    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        if (world.getBlockEntity(rayTraceResult.getBlockPos()) instanceof IPedestalMachine toHighlight){
            toHighlight.lightPedestal(world);
        }
    }

    @Override
    public String getBookDescription() {
        return "Applies Magic Find to the target, causing magical mobs to glow within 75 blocks of them. Magic Find also reveals spells on Runes.";
    }

    @Override
    public void buildConfig(ModConfigSpec.Builder builder) {
        super.buildConfig(builder);
        addPotionConfig(builder, 60);
        addExtendTimeConfig(builder, 15);
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.TWO;
    }

    @Override
    public int getDefaultManaCost() {
        return 50;
    }

    @NotNull
    @Override
    public Set<AbstractAugment> getCompatibleAugments() {
        return setOf(AugmentExtendTime.INSTANCE, AugmentDurationDown.INSTANCE);
    }

    @NotNull
    @Override
    public Set<SpellSchool> getSchools() {
        return setOf(SpellSchools.ABJURATION);
    }

    @Override
    public int getBaseDuration() {
        return POTION_TIME == null ? 30 : POTION_TIME.get();
    }

    @Override
    public int getExtendTimeDuration() {
        return EXTEND_TIME == null ? 8 : EXTEND_TIME.get();
    }
}
