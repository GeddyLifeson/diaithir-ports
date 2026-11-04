package net.diaithir.hostile.hordes.procedures;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.diaithir.hostile.hordes.init.HordeHoardModMobEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Source event (1.20.1 Forge): LivingAttackEvent.
 * Wired in 26.1.2 from per-loader handlers (NeoForge LivingIncomingDamageEvent and
 * Fabric LivingEntityMixin#hurtServer). Does not cancel — returns void.
 */
public class HordeStopMultiplyingProcedure {

    public static void execute(Entity entity) {
        if (entity == null)
            return;
        if (entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(HordeHoardConstants.MOD_ID, "hordes")))) {
            if (entity instanceof LivingEntity _entity)
                _entity.removeEffect(HordeHoardModMobEffects.THINKING);
        }
    }
}
