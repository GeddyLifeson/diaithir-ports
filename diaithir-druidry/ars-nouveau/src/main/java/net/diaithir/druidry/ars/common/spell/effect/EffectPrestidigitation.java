package net.diaithir.druidry.ars.common.spell.effect;

import net.diaithir.druidry.ars.api.ANFakePlayer;
import net.diaithir.druidry.ars.api.particle.ParticleEmitter;
import net.diaithir.druidry.ars.api.particle.timelines.PrestidigitationTimeline;
import net.diaithir.druidry.ars.api.registry.ParticleTimelineRegistry;
import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.common.block.tile.ParticleTile;
import net.diaithir.druidry.ars.common.items.data.PrestidigitationData;
import net.diaithir.druidry.ars.common.lib.GlyphLib;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketPrestidigitation;
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


    @Override
    public void onResolve(HitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        super.onResolve(rayTraceResult, world, shooter, spellStats, spellContext, resolver);

    }

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        super.onResolveEntity(rayTraceResult, world, shooter, spellStats, spellContext, resolver);
        PrestidigitationTimeline timeline = spellContext.getParticleTimeline(ParticleTimelineRegistry.PRESTIDIGITATION_TIMELINE.get());
        int ticksRemaining = (int) ((5 + 3 * spellStats.getDurationMultiplier()) * 20);
        Networking.sendToNearbyClient(world, rayTraceResult.getEntity(), new PacketPrestidigitation(rayTraceResult.getEntity(), ticksRemaining, createStaticEmitter(timeline.onTickEffect, rayTraceResult.getLocation())));
    }


    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        BlockPos pos = rayTraceResult.getBlockPos().relative(rayTraceResult.getDirection());
        Player player = getPlayer(shooter, (ServerLevel) world);
        if (world.getBlockState(pos).canBeReplaced()
                && world.isUnobstructed(BlockRegistry.PARTICLE_BLOCK.get().defaultBlockState(), pos, CollisionContext.of(ANFakePlayer.getPlayer((ServerLevel) world)))
                && world.isInWorldBounds(pos)) {

            BlockState lightBlockState = BlockRegistry.PARTICLE_BLOCK.get().defaultBlockState().setValue(WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER);
            world.setBlockAndUpdate(pos, lightBlockState);
            if (world.getBlockEntity(pos) instanceof ParticleTile tile) {
                tile.setTimeline(spellContext.getParticleTimeline(ParticleTimelineRegistry.PRESTIDIGITATION_TIMELINE.get()));
                if (!spellStats.hasBuff(AugmentAmplify.INSTANCE)) {
                    tile.isTemporary = true;
                    tile.ticksRemaining = (int) ((5 + 3 * spellStats.getDurationMultiplier()) * 20);
                }
            }
            world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 2);

        }
    }

    public static void onInventoryTick(ItemStack stack, Level level, Entity entity, int inventorySlot, boolean isCurrentItem) {
        if (level.isClientSide || !stack.has(DataComponentRegistry.PRESTIDIGITATION)) {
            return;
        }
        PrestidigitationData prestidigitationData = stack.get(DataComponentRegistry.PRESTIDIGITATION.get());
        if (prestidigitationData == null) {
            return;
        }
        // Current item, held in hand or armor slots.
        if (isCurrentItem || (inventorySlot > 35 && inventorySlot < 41)) {
            ParticleEmitter emitter = prestidigitationData.getEmitter(entity, prestidigitationData.timeline().onTickEffect);
            emitter.tick(level);
        }
    }

    @Override
    public String getBookDescription() {
        return "Places an invisible temporary block that emits particles. Amplify will make this block permanent. Particles can be configured from the Spell Style menu in the spellbook. Targeting an entity will emit particles centered on them instead.";
    }

    @Override
    public void addAugmentDescriptions(Map<AbstractAugment, String> map) {
        super.addAugmentDescriptions(map);
        map.put(AugmentExtendTime.INSTANCE, "Increases the duration.");
        map.put(AugmentDurationDown.INSTANCE, "Decreases the duration.");
        map.put(AugmentAmplify.INSTANCE, "Makes the block permanent, ignoring duration modifiers.");
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
