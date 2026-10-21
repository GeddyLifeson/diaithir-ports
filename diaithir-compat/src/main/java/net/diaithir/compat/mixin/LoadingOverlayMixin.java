package net.diaithir.compat.mixin;

import java.util.function.IntSupplier;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The resource loading screen in Diaithir's night blue instead of Mojang red. */
@Mixin(LoadingOverlay.class)
abstract class LoadingOverlayMixin {
    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Ljava/util/function/IntSupplier;getAsInt()I"))
    private int diaithir$background(IntSupplier brandBackground) {
        return 0xFF1B1D2A;
    }
}
