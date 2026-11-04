package net.diaithir.hostile.hordes.procedures;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.diaithir.hostile.hordes.init.HordeHoardModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Source event (1.20.1 Forge): LivingEvent.LivingTickEvent.
 * Wired in 26.1.2 from per-loader handlers.
 *
 * Weapon-swap state machine for skeletons (intelligent_teams) and piglins
 * (intelligent_piglin). Four near-identical branches gated by current main-hand item:
 *   - BOW         + ally near target  → swap to AIR (back away)
 *   - AIR         + no ally near      → swap to BOW
 *   - CROSSBOW    + ally near target  → swap to GOLDEN_SWORD
 *   - GOLDEN_SWORD+ no ally near      → swap to CROSSBOW + (if zombified piglin) shoot
 *                                       a flaming arrow
 * Each swap plays an equip/load sfx, applies THINKING(40, 1) cooldown, swings the arm.
 *
 * NOTE on sound lookups: ForgeRegistries.SOUND_EVENTS.getValue(...) replaced by the
 *      SoundEvents.X constants (CROSSBOW_LOADING_END, ARMOR_EQUIP_GENERIC, CROSSBOW_SHOOT
 *      all verified in 26.1.2 deobf).
 *
 * NOTE on the anonymous-class arrow factory: source uses
 *      `new Object() { public Projectile getArrow(...) { ... } }.getArrow(...)`
 * which is MCreator-generated boilerplate — preserved verbatim per the no-deviations
 * rule even though a static helper would be cleaner. The setSecondsOnFire(100) inside
 * becomes igniteForSeconds(100.0F).
 *
 * NOTE on the `doo` boolean state: source toggles `doo` inside the entity-iteration
 * loop using `doo = true; break;` vs `doo = false;` (and the inverse for the AIR /
 * GOLDEN_SWORD branches). Preserved verbatim — though the inverted branches have
 * MCreator-generated logic that's idiomatic to MCreator's "any/all" detection.
 */
public class IntelligentTickProcedure {

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;
        boolean doo = false;
        if (entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_teams"))) || entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_piglin")))) {
            if (!(entity instanceof LivingEntity _livEnt2 && _livEnt2.hasEffect(HordeHoardModMobEffects.THINKING))) {
                if (!((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == null)) {
                    if (!((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).isEnchanted())) {
                        if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.BOW && !((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) instanceof IronGolem)) {
                            {
                                final Vec3 _center = new Vec3(x, y, z);
                                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4.5 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
                                for (Entity entityiterator : _entfound) {
                                    if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == entityiterator
                                            || entityiterator.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_teams"))) && !(entity.getX() + entity.getZ() == entityiterator.getX() + entityiterator.getZ())) {
                                        doo = true;
                                        break;
                                    } else {
                                        doo = false;
                                    }
                                }
                            }
                            if (doo) {
                                if (entity instanceof LivingEntity _entity) {
                                    ItemStack _setstack = new ItemStack(Blocks.AIR).copy();
                                    _setstack.setCount(1);
                                    _entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
                                    if (_entity instanceof Player _player)
                                        _player.getInventory().setChanged();
                                }
                                if (world instanceof Level _level) {
                                    if (!_level.isClientSide()) {
                                        _level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.HOSTILE, (float) 0.5, 1);
                                    } else {
                                        _level.playLocalSound(x, y, z, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.HOSTILE, (float) 0.5, 1, false);
                                    }
                                }
                                if (entity instanceof LivingEntity _entity)
                                    _entity.swing(InteractionHand.MAIN_HAND, true);
                                if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                                    _entity.addEffect(new MobEffectInstance(HordeHoardModMobEffects.THINKING, 40, 1, false, false));
                            }
                        } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Blocks.AIR.asItem()) {
                            {
                                final Vec3 _center = new Vec3(x, y, z);
                                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4.5 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
                                for (Entity entityiterator : _entfound) {
                                    if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == entityiterator
                                            || entityiterator.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_teams"))) && !(entity.getX() + entity.getZ() == entityiterator.getX() + entityiterator.getZ())) {
                                        doo = false;
                                        break;
                                    } else {
                                        doo = true;
                                    }
                                }
                            }
                            if (doo) {
                                if (entity instanceof LivingEntity _entity) {
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
                                if (entity instanceof LivingEntity _entity)
                                    _entity.swing(InteractionHand.MAIN_HAND, true);
                                if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                                    _entity.addEffect(new MobEffectInstance(HordeHoardModMobEffects.THINKING, 40, 1, false, false));
                            }
                        } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.CROSSBOW) {
                            {
                                final Vec3 _center = new Vec3(x, y, z);
                                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4.5 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
                                for (Entity entityiterator : _entfound) {
                                    if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == entityiterator || entityiterator.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_piglin")))
                                            && !(entity.getX() + entity.getZ() == entityiterator.getX() + entityiterator.getZ())) {
                                        doo = true;
                                        break;
                                    } else {
                                        doo = false;
                                    }
                                }
                            }
                            if (doo) {
                                if (entity instanceof LivingEntity _entity) {
                                    ItemStack _setstack = new ItemStack(Items.GOLDEN_SWORD).copy();
                                    _setstack.setCount(1);
                                    _entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack);
                                    if (_entity instanceof Player _player)
                                        _player.getInventory().setChanged();
                                }
                                if (world instanceof Level _level) {
                                    if (!_level.isClientSide()) {
                                        _level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.HOSTILE, (float) 0.5, 1);
                                    } else {
                                        _level.playLocalSound(x, y, z, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.HOSTILE, (float) 0.5, 1, false);
                                    }
                                }
                                if (entity instanceof LivingEntity _entity)
                                    _entity.swing(InteractionHand.MAIN_HAND, true);
                                if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                                    _entity.addEffect(new MobEffectInstance(HordeHoardModMobEffects.THINKING, 40, 1, false, false));
                            }
                        } else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.GOLDEN_SWORD) {
                            {
                                final Vec3 _center = new Vec3(x, y, z);
                                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(4.5 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
                                for (Entity entityiterator : _entfound) {
                                    if ((entity instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) == entityiterator || entityiterator.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "intelligent_piglin")))
                                            && !(entity.getX() + entity.getZ() == entityiterator.getX() + entityiterator.getZ())) {
                                        doo = false;
                                        break;
                                    } else {
                                        doo = true;
                                    }
                                }
                            }
                            if (doo) {
                                if (entity instanceof LivingEntity _entity) {
                                    ItemStack _setstack = new ItemStack(Items.CROSSBOW).copy();
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
                                if (entity instanceof LivingEntity _entity)
                                    _entity.swing(InteractionHand.MAIN_HAND, true);
                                if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                                    _entity.addEffect(new MobEffectInstance(HordeHoardModMobEffects.THINKING, 40, 1, false, false));
                                if (entity instanceof ZombifiedPiglin) {
                                    {
                                        Entity _shootFrom = entity;
                                        Level projectileLevel = _shootFrom.level();
                                        if (!projectileLevel.isClientSide()) {
                                            Projectile _entityToSpawn = new Object() {
                                                public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
                                                    AbstractArrow entityToSpawn = new Arrow(EntityType.ARROW, level);
                                                    entityToSpawn.setOwner(shooter);
                                                    entityToSpawn.setBaseDamage(damage);
                                                    // NOTE non-1:1 substitution: AbstractArrow#setKnockback(int) removed in
                                                    // 26.1.2 — arrow knockback now flows from EnchantmentHelper.modifyKnockback
                                                    // against firedFromWeapon (see AbstractArrow#doKnockback). With no weapon
                                                    // stack here, the original knockback=1 has no clean expression in the new
                                                    // API. The arrow still fires + damages + ignites; only the small extra
                                                    // knockback bump is lost. The unused `knockback` parameter is kept on the
                                                    // factory method for source-faithfulness (matches MCreator boilerplate).
                                                    int unused_knockback = knockback;
                                                    entityToSpawn.igniteForSeconds(100.0F);
                                                    return entityToSpawn;
                                                }
                                            }.getArrow(projectileLevel, entity, 5, 1);
                                            _entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
                                            _entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 1, (float) 0.2);
                                            projectileLevel.addFreshEntity(_entityToSpawn);
                                        }
                                    }
                                    if (world instanceof Level _level) {
                                        if (!_level.isClientSide()) {
                                            _level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.CROSSBOW_SHOOT, SoundSource.HOSTILE, 1, 1);
                                        } else {
                                            _level.playLocalSound(x, y, z, SoundEvents.CROSSBOW_SHOOT, SoundSource.HOSTILE, 1, 1, false);
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
