package org.valkyrienskies.mod.mixin.feature.ai.goal.villagers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(AssignProfessionFromJobSite.class)
public class MixinAssignProfessionFromJobSite {
    // 26.2: the Yarn-intermediary name method_46890 doesn't exist under Mojang mappings; the matching
    // declarative-behavior lambda (same param shape) is lambda$create$2. Villager also moved to the
    // npc.villager subpackage.
    @WrapOperation(method = "lambda$create$2(Lnet/minecraft/world/entity/ai/behavior/declarative/BehaviorBuilder$Instance;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/villager/Villager;J)Z", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/core/BlockPos;closerToCenterThan(Lnet/minecraft/core/Position;D)Z"))
    private static boolean onCloserToCenterThan(BlockPos instance, Position position, double v,
        Operation<Boolean> original, @Local
        Villager villager) {
        return original.call(BlockPos.containing(VSGameUtilsKt.toWorldCoordinates(villager.level(), instance)), position, v);
    }
}
