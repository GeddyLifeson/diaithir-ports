package net.diaithir.druidry.ars.common.spell.effect;

import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.api.util.DamageUtil;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.lib.GlyphLib;
import net.diaithir.druidry.ars.common.spell.augment.AugmentAmplify;
import net.diaithir.druidry.ars.common.spell.augment.AugmentDampen;
import net.diaithir.druidry.ars.common.spell.augment.AugmentRandomize;
import net.diaithir.druidry.ars.setup.registry.DamageTypesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class EffectWindshear extends AbstractEffect implements IDamageEffect {

    public static EffectWindshear INSTANCE = new EffectWindshear();

    private EffectWindshear() {
        super(GlyphLib.EffectWindshearID, "Wind Shear");
    }

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        if (!rayTraceResult.getEntity().onGround() && !(rayTraceResult.getEntity() instanceof ItemEntity)) {
            int numBlocks = 0;
            BlockPos pos = rayTraceResult.getEntity().blockPosition();
            while (!world.getBlockState(pos.below()).blocksMotion() && numBlocks <= 10) {
                pos = pos.below();
                numBlocks++;
            }

            if (attemptDamage(world, shooter, spellStats, spellContext, resolver, rayTraceResult.getEntity(), buildDamageSource(world, shooter), (float) (DAMAGE.get() + numBlocks))) {//converted DamageSource FALL into playerAttack
                Vec3 vec = rayTraceResult.getEntity().position();
                for (int i = 0; i < 10; i++) {
                    ((ServerLevel) world).sendParticles(ParticleTypes.SWEEP_ATTACK, vec.x + ParticleUtil.inRange(-0.2, 0.2), vec.y + 0.5 + ParticleUtil.inRange(-0.2, 0.2), vec.z + ParticleUtil.inRange(-0.2, 0.2), 30,
                            ParticleUtil.inRange(-0.2, 0.2), ParticleUtil.inRange(-0.2, 0.2), ParticleUtil.inRange(-0.2, 0.2), 0.3);
                }
            }
        }
    }

    @Override
    public DamageSource buildDamageSource(Level world, LivingEntity shooter) {
        return DamageUtil.source(world, DamageTypesRegistry.WINDSHEAR, shooter);
    }

    @Override
    public void buildConfig(ModConfigSpec.Builder builder) {
        super.buildConfig(builder);
        addDamageConfig(builder, 5);
        addAmpConfig(builder, 2.5f);
        addGenericDouble(builder, 0.75, "Damage per block in the air", "airDamage");
    }

    @Override
    protected void addDefaultAugmentLimits(Map<Identifier, Integer> defaults) {
        defaults.put(AugmentAmplify.INSTANCE.getRegistryName(), 2);
    }

    @Override
    public String getBookDescription() {
        return "Deals damage to targets in the air, with an increasing amount based on how high the target is off the ground, up to 10 blocks. Targets on the ground take no damage.";
    }

    @Override
    public int getDefaultManaCost() {
        return 50;
    }

    @NotNull
    @Override
    public Set<SpellSchool> getSchools() {
        return setOf(SpellSchools.ELEMENTAL_AIR);
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.TWO;
    }

    @NotNull
    @Override
    public Set<AbstractAugment> getCompatibleAugments() {
        return augmentSetOf(AugmentDampen.INSTANCE, AugmentAmplify.INSTANCE, AugmentRandomize.INSTANCE);
    }
}
