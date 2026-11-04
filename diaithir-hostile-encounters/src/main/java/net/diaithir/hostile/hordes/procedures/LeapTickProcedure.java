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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Source event (1.20.1 Forge): LivingEvent.LivingTickEvent.
 * Wired in 26.1.2 from per-loader handlers.
 *
 * Leaping mobs (spider/skeleton/etc) jump when their target is more than 2 blocks above:
 * applies JUMP effect, +0.8 to delta-y, cloud particles, goat-jump sfx, swing.
 *
 * NOTE on sound lookup: source did
 *     ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.goat.long_jump"))
 * In 26.1.2 the constant SoundEvents.GOAT_LONG_JUMP exists and is typed as a raw
 * SoundEvent (verified). Using the constant is cleaner than the runtime registry
 * lookup and behaves identically.
 */
public class LeapTickProcedure {

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        if (entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "leaping_mobs")))) {
            if (!(entity instanceof LivingEntity _livEnt1 && _livEnt1.hasEffect(MobEffects.JUMP_BOOST)) && !((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == null)
                    && !(entity instanceof LivingEntity _livEnt4 && _livEnt4.hasEffect(HordeHoardModMobEffects.THINKING))) {
                {
                    final Vec3 _center = new Vec3(x, (y + 2.5), z);
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3.5 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
                    for (Entity entityiterator : _entfound) {
                        if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == entityiterator && entityiterator.getY() > entity.getY() + 2) {
                            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                                _entity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 60, 1, false, false));
                            entity.setDeltaMovement(new Vec3((entity.getDeltaMovement().x()), (entity.getDeltaMovement().y() + 0.8), (entity.getDeltaMovement().z())));
                            if (world instanceof ServerLevel _level)
                                _level.sendParticles(ParticleTypes.CLOUD, (x + 0.5), y, (z + 0.5), 10, 0.2, 0, 0.2, 0.05);
                            if (world instanceof Level _level) {
                                if (!_level.isClientSide()) {
                                    _level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.GOAT_LONG_JUMP, SoundSource.HOSTILE, (float) 0.5, 1);
                                } else {
                                    _level.playLocalSound(x, y, z, SoundEvents.GOAT_LONG_JUMP, SoundSource.HOSTILE, (float) 0.5, 1, false);
                                }
                            }
                            if (entity instanceof LivingEntity _entity)
                                _entity.swing(InteractionHand.MAIN_HAND, true);
                            break;
                        }
                    }
                }
            }
        }
    }
}
