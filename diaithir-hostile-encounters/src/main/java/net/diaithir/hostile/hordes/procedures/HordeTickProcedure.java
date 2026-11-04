package net.diaithir.hostile.hordes.procedures;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.diaithir.hostile.hordes.init.HordeHoardModGameRules;
import net.diaithir.hostile.hordes.init.HordeHoardModMobEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Source event (1.20.1 Forge): LivingEvent.LivingTickEvent.
 * Wired in 26.1.2 from per-loader handlers.
 *
 * Two phases gated by per-mob tags + gamerules:
 *   1) Grave-robber zombies on hidden_zombie_blocks (with a target, mobgrief on) run a
 *      THINKING-effect-driven dig animation: every 20 ticks of the effect plays the
 *      block-break levelEvent + arm swing; on duration==1 spawns a fresh zombie at
 *      pos and plays zombie.destroy_egg sfx. Non-thinking horde mobs roll Math.random()
 *      < 0.0008 to start a new THINKING(200, 10) cycle.
 *   2) HORDESTACKING: any horde-tagged mob within a 0.225-radius AABB of another
 *      gets a small upward delta-velocity kick (with slow_falling for adults), arm
 *      swing, fire-spread + movement_speed if on fire, and (mobgrief on) breaks up to
 *      4 horde_breakable blocks beneath it.
 *
 * NOTE on MobSpawnType→EntitySpawnReason: this is a 1:1 enum rename in 26.1.2.
 *      The EntityType.spawn(ServerLevel, BlockPos, EntitySpawnReason) overload
 *      preserves the original behavior.
 *
 * NOTE on sound lookup: ForgeRegistries.SOUND_EVENTS.getValue(...) replaced by
 *      SoundEvents.ZOMBIE_DESTROY_EGG (constant verified in 26.1.2).
 *
 * NOTE on horde_breakable AIR check: source has
 *      if (!Blocks.AIR.defaultBlockState().is(BlockTags.create("forge:horde_breakable")))
 *      which is functionally `if (!false)` (AIR is never in horde_breakable). Preserved
 *      verbatim per the no-deviations rule — the optimizer will fold it.
 */
public class HordeTickProcedure {

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        if (!(world instanceof ServerLevel serverLevel))
            return;
        boolean skipthistick = false;
        double rand = 0;
        double yit = 0;
        double myx = 0;
        myx = x;
        if (entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "hordes")))) {
            skipthistick = false;
            if (!(entity instanceof LivingEntity _livEnt1 && _livEnt1.isBaby()) && entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "horde_grave_robbers")))) {
                if ((world.getBlockState(BlockPos.containing(x, y - 1, z))).is(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "hidden_zombie_blocks"))) && !((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == null)
                        && serverLevel.getGameRules().get(HordeHoardModGameRules.HORDEMULTIPLYING)) {
                    // Diagnostic: fires once every ~10s per qualifying entity, staggered
                    // by entity id so a 100-mob horde produces a steady stream rather than
                    // 100 lines on the same tick. If you spawn /horde and see no
                    // [horde_dig conditions met] lines within 10s, the conditions aren't
                    // firing (target/tag/block/gamerule check is failing upstream).
                    if ((serverLevel.getGameTime() + entity.getId()) % 200L == 0L) {
                        HordeHoardConstants.LOGGER.info(
                                "[horde_dig conditions met] {} #{} on {} at {} {} {}",
                                BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                                entity.getId(),
                                BuiltInRegistries.BLOCK.getKey(world.getBlockState(BlockPos.containing(x, y - 1, z)).getBlock()),
                                (int) x, (int) y, (int) z
                        );
                    }
                    if (entity instanceof LivingEntity _livEnt8 && _livEnt8.hasEffect(HordeHoardModMobEffects.THINKING)) {
                        if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(HordeHoardModMobEffects.THINKING) ? _livEnt.getEffect(HordeHoardModMobEffects.THINKING).getDuration() : 0) % 20 == 0
                                && !((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(HordeHoardModMobEffects.THINKING) ? _livEnt.getEffect(HordeHoardModMobEffects.THINKING).getDuration() : 0) == 0)) {
                            if (entity instanceof LivingEntity _entity)
                                _entity.swing(InteractionHand.MAIN_HAND, true);
                            world.levelEvent(2001, BlockPos.containing(x, y - 1, z), Block.getId((world.getBlockState(BlockPos.containing(x, y - 1, z)))));
                        }
                        if ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(HordeHoardModMobEffects.THINKING) ? _livEnt.getEffect(HordeHoardModMobEffects.THINKING).getDuration() : 0) == 1) {
                            if (world instanceof ServerLevel _level) {
                                Entity entityToSpawn = EntityType.ZOMBIE.spawn(_level, BlockPos.containing(x, y - 1, z), EntitySpawnReason.MOB_SUMMONED);
                                if (entityToSpawn != null) {
                                    entityToSpawn.setDeltaMovement(0, 0, 0);
                                    HordeHoardConstants.LOGGER.info(
                                            "[horde_dig] reinforcement zombie spawned at {} {} {} (parent={})",
                                            (int) x, (int) (y - 1), (int) z,
                                            BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())
                                    );
                                }
                            }
                            if (world instanceof Level _level) {
                                if (!_level.isClientSide()) {
                                    _level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 1, 1);
                                } else {
                                    _level.playLocalSound(x, y, z, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 1, 1, false);
                                }
                            }
                        }
                        skipthistick = true;
                        entity.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3(0.25, 0.05, 0.25));
                    } else if (Math.random() < 0.0008) {
                        HordeHoardConstants.LOGGER.info(
                                "[horde_dig] {} starting THINKING dig cycle at {} {} {}",
                                BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                                (int) x, (int) y, (int) z
                        );
                        if (entity instanceof LivingEntity _entity)
                            _entity.removeEffect(MobEffects.SPEED);
                        if (entity instanceof LivingEntity _entity)
                            _entity.removeEffect(HordeHoardModMobEffects.THINKING);
                        if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                            _entity.addEffect(new MobEffectInstance(HordeHoardModMobEffects.THINKING, 200, 10, false, false));
                        skipthistick = true;
                    }
                } else if (entity instanceof LivingEntity _livEnt21 && _livEnt21.hasEffect(HordeHoardModMobEffects.THINKING)) {
                    if (entity instanceof LivingEntity _entity)
                        _entity.removeEffect(HordeHoardModMobEffects.THINKING);
                }
            }
            if (!skipthistick && serverLevel.getGameRules().get(HordeHoardModGameRules.HORDESTACKING)) {
                {
                    final Vec3 _center = new Vec3(x, y, z);
                    List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(0.45 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
                    for (Entity entityiterator : _entfound) {
                        if (!(myx == entityiterator.getX())) {
                            if (entityiterator.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "hordes")))) {
                                rand = Math.random();
                                if (rand > 0.16) {
                                    rand = 0.16;
                                }
                                if (entity instanceof LivingEntity _livEnt26 && _livEnt26.isBaby()) {
                                    entity.setDeltaMovement(
                                            new Vec3((entity.getDeltaMovement().x() + (rand / 20) * Mth.nextInt(RandomSource.create(), -1, 1)), (rand * 2), (entity.getDeltaMovement().z() + (rand / 20) * Mth.nextInt(RandomSource.create(), -1, 1))));
                                    entity.fallDistance = 0;
                                } else {
                                    entity.setDeltaMovement(
                                            new Vec3((entity.getDeltaMovement().x() + (rand / 20) * Mth.nextInt(RandomSource.create(), -1, 1)), rand, (entity.getDeltaMovement().z() + (rand / 20) * Mth.nextInt(RandomSource.create(), -1, 1))));
                                    entity.fallDistance = 0;
                                    if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                                        _entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, false, false));
                                }
                                if (entity instanceof LivingEntity _entity)
                                    _entity.swing(InteractionHand.MAIN_HAND, true);
                                if (entity.isOnFire()) {
                                    entityiterator.igniteForSeconds(5.0F);
                                    if (!(entity instanceof LivingEntity _livEnt43 && _livEnt43.hasEffect(MobEffects.SPEED))) {
                                        if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                                            _entity.addEffect(new MobEffectInstance(MobEffects.SPEED, entity.getRemainingFireTicks(), 2, false, false));
                                    }
                                    if (!(entityiterator instanceof LivingEntity _livEnt46 && _livEnt46.hasEffect(MobEffects.SPEED))) {
                                        if (entityiterator instanceof LivingEntity _entity && !_entity.level().isClientSide())
                                            _entity.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 2, false, false));
                                    }
                                }
                                if (!Blocks.AIR.defaultBlockState().is(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "horde_breakable")))) {
                                    if (serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {
                                        yit = -1;
                                        for (int index0 = 0; index0 < 4; index0++) {
                                            if ((world.getBlockState(BlockPos.containing(x, y + yit, z))).is(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "horde_breakable")))) {
                                                {
                                                    BlockPos _pos = BlockPos.containing(x, y + yit, z);
                                                    Block.dropResources(world.getBlockState(_pos), world, BlockPos.containing(x, y + yit, z), null);
                                                    world.destroyBlock(_pos, false);
                                                }
                                            }
                                            yit = yit + 1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
