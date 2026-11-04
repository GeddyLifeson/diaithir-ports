package net.diaithir.druidry.ars.common.spell.effect;

import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.common.lib.GlyphLib;
import net.diaithir.druidry.ars.common.spell.augment.AugmentAOE;
import net.diaithir.druidry.ars.common.spell.augment.AugmentAmplify;
import net.diaithir.druidry.ars.common.spell.augment.AugmentDampen;
import net.diaithir.druidry.ars.common.spell.augment.AugmentExtract;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class EffectExplosion extends AbstractEffect implements IDamageEffect {
    public static EffectExplosion INSTANCE = new EffectExplosion();

    private EffectExplosion() {
        super(GlyphLib.EffectExplosionID, "Explosion");
    }

    @Override
    public void onResolve(HitResult rayTraceResult, Level world, @Nullable LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        Vec3 vec = safelyGetHitPos(rayTraceResult);
        double intensity = BASE.get() + AMP_VALUE.get() * spellStats.getAmpMultiplier() + AOE_BONUS.get() * spellStats.getAoeMultiplier();
        int dampen = spellStats.getBuffCount(AugmentDampen.INSTANCE);
        intensity -= 0.5 * dampen;
        //TODO: check explosion augment functionality
        Explosion.BlockInteraction mode = dampen > 0 ? Explosion.BlockInteraction.KEEP : Explosion.BlockInteraction.DESTROY;
        mode = spellStats.hasBuff(AugmentExtract.INSTANCE) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : mode;
        explode(world, shooter, null, null, vec.x, vec.y, vec.z, (float) intensity, false, mode, spellStats.getAmpMultiplier());
    }

    public void explode(Level world, @Nullable Entity e, @Nullable DamageSource source, @Nullable ExplosionDamageCalculator context,
                        double x, double y, double z, float radius, boolean fire, Explosion.BlockInteraction interaction, double amp) {
        // Port: vanilla explosion (ANExplosion's custom damage scaling is dropped).
        Level.ExplosionInteraction mode = interaction == Explosion.BlockInteraction.KEEP ? Level.ExplosionInteraction.NONE : Level.ExplosionInteraction.BLOCK;
        world.explode(e, source, context, x, y, z, radius, fire, mode);
    }

    public ModConfigSpec.DoubleValue BASE;
    public ModConfigSpec.DoubleValue AOE_BONUS;
    public ModConfigSpec.DoubleValue AMP_DAMAGE;

    @Override
    public void buildConfig(ModConfigSpec.Builder builder) {
        super.buildConfig(builder);
        addAmpConfig(builder, 0.5);
        BASE = builder.comment("Explosion base intensity").defineInRange("base", 0.75, 0.0, 100);
        AOE_BONUS = builder.comment("AOE intensity bonus").defineInRange("aoe_bonus", 1.5, 0.0, 100);
        addDamageConfig(builder, 6.0);
        AMP_DAMAGE = builder.comment("Additional damage per amplify").defineInRange("amp_damage", 2.5, 0.0, Integer.MAX_VALUE);
    }

    @Override
    public int getDefaultManaCost() {
        return 200;
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.TWO;
    }

   @NotNull
    @Override
    public Set<AbstractAugment> getCompatibleAugments() {
        return augmentSetOf(
                AugmentAmplify.INSTANCE, AugmentDampen.INSTANCE,
                AugmentAOE.INSTANCE,
                AugmentExtract.INSTANCE
        );
    }

    @Override
    protected void addDefaultAugmentLimits(Map<Identifier, Integer> defaults) {
        defaults.put(AugmentAmplify.INSTANCE.getRegistryName(), 2);
    }

    @Override
    public String getBookDescription() {
        return "Causes an explosion at the location. Amplify increases the damage and size by a small amount, while AOE will increase the size of the explosion by a large amount, but not damage.";
    }

   @NotNull
    @Override
    public Set<SpellSchool> getSchools() {
        return setOf(SpellSchools.ELEMENTAL_FIRE);
    }
}
