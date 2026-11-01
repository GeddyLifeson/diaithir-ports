package net.diaithir.nemesis.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lightman's Currency 26.1-1.0.0.1 builds the loot context for a mob's coin drop with the ENTITY parameter set but never sets
 * minecraft:this_entity, so every player kill (and every kill of a boss-pool mob) of a mob with a coin pool throws
 * "Missing required parameters: [this_entity]" out of its LivingDeathEvent listener and up through Mob.die. This adds the dying entity
 * to the parameters just before the set is checked. Pseudo: does nothing when the mod is absent or a later release fixed it
 * (the target call is then gone; the mixin config is not required).
 */
@Pseudo
@Mixin(targets = "io.github.lightman314.lightmanscurrency.features.loot.LootManager", remap = false)
abstract class LcmLootManagerMixin {
    @WrapOperation(method = "generateEntityContext", remap = false, require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;create(Lnet/minecraft/util/context/ContextKeySet;)Lnet/minecraft/world/level/storage/loot/LootParams;"))
    private static LootParams diaithirnemesis$thisEntity(LootParams.Builder builder, ContextKeySet set, Operation<LootParams> original, @Local(argsOnly = true) Entity dying) {
        return original.call(builder.withParameter(LootContextParams.THIS_ENTITY, dying), set);
    }
}
