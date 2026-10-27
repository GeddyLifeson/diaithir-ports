package net.diaithir.tooltips.mixin;

import net.diaithir.tooltips.CultureNames;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Nature's Compass: each biome in the list also carries its native name. Skipped when the mod is absent. */
@Pseudo
@Mixin(targets = "com.chaosthedude.naturescompass.util.BiomeUtils", remap = false)
abstract class NaturesCompassMixin {
    @Inject(method = "getBiomeNameForDisplay(Lnet/minecraft/world/level/Level;Lnet/minecraft/resources/Identifier;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private static void diaithir$native(Level level, Identifier id, CallbackInfoReturnable<String> cir) {
        String name = CultureNames.name("biome", id.toString());
        if (name != null) {
            cir.setReturnValue(cir.getReturnValue() + " · " + name);
        }
    }
}
