package net.diaithir.branding.mixin;

import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** The world loading bar uses the Diaithir panel and ember colours instead of black and green. */
@Mixin(LevelLoadingScreen.class)
abstract class LevelLoadingScreenMixin {
    @ModifyConstant(method = "drawProgressBar", constant = @Constant(intValue = 0xFF000000))
    private int diaithir$track(int colour) {
        return 0xFF14161F;
    }

    @ModifyConstant(method = "drawProgressBar", constant = @Constant(intValue = 0xFF00FF00))
    private int diaithir$fill(int colour) {
        return 0xFFF59A23;
    }
}
