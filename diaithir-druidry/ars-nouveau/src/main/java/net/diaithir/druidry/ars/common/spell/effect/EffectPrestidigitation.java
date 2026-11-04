package net.diaithir.druidry.ars.common.spell.effect;

import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.lib.GlyphLib;
import net.diaithir.druidry.ars.common.spell.augment.AugmentAmplify;
import net.diaithir.druidry.ars.common.spell.augment.AugmentDurationDown;
import net.diaithir.druidry.ars.common.spell.augment.AugmentExtendTime;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class EffectPrestidigitation extends AbstractEffect {
    public static EffectPrestidigitation INSTANCE = new EffectPrestidigitation();

    private EffectPrestidigitation() {
        super(GlyphLib.EffectPrestidigitation, "Prestidigitation");
    }


    // Port note: the particle-timeline editor (ParticleEmitter, ParticleTile) is not part of the port, so this is a plain
    // effect: it emits a burst of glow particles in the caster's colours at the target. Extend time repeats the burst
    // on the spell's own duration through the augment-scaled count.
    @Override
    public void onResolve(HitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        if (!(world instanceof ServerLevel level)) {
            return;
        }
        int bursts = 1 + (int) Math.max(0, spellStats.getDurationMultiplier());
        if (rayTraceResult instanceof EntityHitResult entityHit) {
            for (int i = 0; i < bursts; i++) {
                ParticleUtil.spawnTouchPacket(level, entityHit.getEntity().blockPosition(), spellContext.getColors());
            }
        } else if (rayTraceResult instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
            for (int i = 0; i < bursts; i++) {
                ParticleUtil.spawnTouchPacket(level, pos, spellContext.getColors());
            }
        }
    }

    public static void onInventoryTick(ItemStack stack, Level level, Entity entity, int inventorySlot, boolean isCurrentItem) {
    }

    @Override
    public String getBookDescription() {
        return "Emits a burst of glowing particles in the spell's colours at the target block or entity. Extend Time repeats the burst.";
    }

    @Override
    public void addAugmentDescriptions(Map<AbstractAugment, String> map) {
        super.addAugmentDescriptions(map);
        map.put(AugmentExtendTime.INSTANCE, "Repeats the burst more times.");
        map.put(AugmentDurationDown.INSTANCE, "Has no further effect.");
        map.put(AugmentAmplify.INSTANCE, "Has no further effect.");
    }

    @Override
    protected int getDefaultManaCost() {
        return 0;
    }

    @Override
    protected @NotNull Set<SpellSchool> getSchools() {
        return Set.of(SpellSchools.CONJURATION);
    }

    @Override
    protected @NotNull Set<AbstractAugment> getCompatibleAugments() {
        return Set.of(AugmentExtendTime.INSTANCE, AugmentDurationDown.INSTANCE, AugmentAmplify.INSTANCE);
    }
}
