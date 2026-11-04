package net.diaithir.hostile.hordes.procedures;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Source event (1.20.1 Forge): LivingAttackEvent.
 * Wired in 26.1.2 from per-loader handlers (sourceentity is event.getSource().getEntity()).
 * Does not cancel.
 *
 * Iron golem AoE shockwave on attack — damages and lifts nearby mobs whose target is a
 * villager or another iron golem.
 *
 * NOTE on damage source: source built a DamageSource via
 *     new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC))
 * That public DamageSource constructor is gone in 26.1.2 — replaced by the
 * Level#damageSources() factory pattern. We additionally need a ServerLevel because
 * 26.1.2 splits damage application into Entity#hurtServer(ServerLevel, DamageSource, float),
 * so the cast guards both the factory call and the damage application.
 */
public class IronGolemSwooshProcedure {

    public static void execute(LevelAccessor world, Entity sourceentity) {
        if (sourceentity == null)
            return;
        if (sourceentity instanceof IronGolem) {
            {
                final Vec3 _center = new Vec3((sourceentity.getX()), (sourceentity.getY()), (sourceentity.getZ()));
                List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
                for (Entity entityiterator : _entfound) {
                    if ((entityiterator instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) instanceof IronGolem || (entityiterator instanceof Mob _mobEnt ? (Entity) _mobEnt.getTarget() : null) instanceof Villager) {
                        if (world instanceof ServerLevel _serverLevel) {
                            entityiterator.hurtServer(_serverLevel, _serverLevel.damageSources().generic(), 5);
                        }
                        if (!(entityiterator instanceof Ravager)) {
                            entityiterator.setDeltaMovement(new Vec3(0, 0.5, 0));
                        }
                    }
                }
            }
        }
    }
}
