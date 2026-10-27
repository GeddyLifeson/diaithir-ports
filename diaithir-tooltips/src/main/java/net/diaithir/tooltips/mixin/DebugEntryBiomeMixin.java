package net.diaithir.tooltips.mixin;

import net.diaithir.tooltips.CultureNames;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** F3: the biome line also carries the biome's native name in the player's culture. */
@Mixin(targets = "net.minecraft.client.gui.components.debug.DebugEntryBiome")
abstract class DebugEntryBiomeMixin {
    @Inject(method = "printBiome", at = @At("RETURN"), cancellable = true)
    private static void diaithir$native(Holder<Biome> biome, CallbackInfoReturnable<String> cir) {
        String id = biome.unwrapKey().map(k -> k.identifier().toString()).orElse(null);
        String name = id == null ? null : CultureNames.name("biome", id);
        if (name != null) {
            cir.setReturnValue(cir.getReturnValue() + " - " + name);
        }
    }
}
