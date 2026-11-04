package net.diaithir.hostile.hordes.procedures;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.diaithir.hostile.hordes.init.HordeHoardModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Source event (1.20.1 Forge): LivingAttackEvent (cancelable).
 * Wired in 26.1.2 from per-loader handlers. Returns true when the source event should
 * be cancelled (intelligent_teams friendly fire prevention).
 *
 * Also runs the "dash away" routine when the source is intelligent_teams: equips a bow
 * if empty-handed (skips wither skeletons), plays crossbow-loading sfx, applies
 * THINKING (50 ticks, amp 1) so they stop swapping for a beat, kicks delta-velocity
 * back along an inverted-and-jittered axis, emits cloud particles.
 *
 * NOTE: cancellation collapsed to boolean (see IronGolemAttackedProcedure for the
 * Event.Result.DENY-branch dead-code rationale).
 */
public class IntelligentDashAwayProcedure {

    public static boolean execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return false;
        boolean cancel = false;
        if (sourceentity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_teams"))) && entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_teams")))) {
            cancel = true;
        }
        if (sourceentity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_teams")))) {
            if (!(sourceentity instanceof WitherSkeleton)) {
                if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Blocks.AIR.asItem()) {
                    if (sourceentity instanceof LivingEntity _entity) {
                        ItemStack _setstack = new ItemStack(Items.BOW).copy();
                        _setstack.setCount(1);
                        _entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
                        if (_entity instanceof Player _player)
                            _player.getInventory().setChanged();
                    }
                    if (world instanceof Level _level) {
                        if (!_level.isClientSide()) {
                            _level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.HOSTILE, (float) 0.5, 1);
                        } else {
                            _level.playLocalSound(x, y, z, SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.HOSTILE, (float) 0.5, 1, false);
                        }
                    }
                    if (sourceentity instanceof LivingEntity _entity)
                        _entity.swing(InteractionHand.MAIN_HAND, true);
                    sourceentity.setDeltaMovement(new Vec3(((sourceentity.getDeltaMovement().x() + Mth.nextDouble(RandomSource.create(), -0.5, 0.5)) * (-1) * 2), (sourceentity.getDeltaMovement().y() + 0.4),
                            ((sourceentity.getDeltaMovement().z() + Mth.nextDouble(RandomSource.create(), -0.5, 0.5)) * (-1) * 2)));
                    if (world instanceof ServerLevel _level)
                        _level.sendParticles(ParticleTypes.CLOUD, (sourceentity.getX() + 0.5), (sourceentity.getY()), (sourceentity.getZ() + 0.5), 10, 0.5, 1, 0.5, 0.1);
                    if (sourceentity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                        _entity.addEffect(new MobEffectInstance(HordeHoardModMobEffects.THINKING, 50, 1, false, false));
                }
            }
        }
        return cancel;
    }
}
