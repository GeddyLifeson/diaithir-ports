package org.valkyrienskies.mod.mixin.feature.ai.goal.bees;

import net.minecraft.world.entity.animal.bee.Bee;
import org.spongepowered.asm.mixin.Mixin;

// TODO Phase 4: 26.2 removed the shared findNearestBlock(Predicate,double) search helper this
// mixin relied on (and the VALID_POLLINATION_BLOCKS field) - BeePollinateGoal now inlines flower
// search into a bespoke private findNearbyFlower() with no equivalent generic search hook.
// Dropped for now (bees won't search for flowers across ship hulls until this is reimplemented
// against the new private method, which likely needs a from-scratch radius search rather than
// wrapping vanilla's search).
@Mixin(Bee.BeePollinateGoal.class)
public abstract class MixinPollinateGoal {
}
