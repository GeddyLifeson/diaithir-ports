package net.diaithir.tooltips.mixin;

import net.diaithir.tooltips.CultureNames;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Explorer's Compass: each structure in the list also carries its native name. Skipped when the mod is absent. */
@Pseudo
@Mixin(targets = "com.chaosthedude.explorerscompass.util.StructureUtils", remap = false)
abstract class ExplorersCompassMixin {
    @Inject(method = "getStructureName(Lnet/minecraft/resources/Identifier;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
    private static void diaithir$native(Identifier id, CallbackInfoReturnable<String> cir) {
        String name = CultureNames.name("structure", id.toString());
        if (name != null) {
            cir.setReturnValue(cir.getReturnValue() + " · " + name);
        }
    }
}
