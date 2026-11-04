// Ported from Leave My Bars Alone (MPL-2.0) https://github.com/Fuzss/leavemybarsalone
// Modifications: config lookup switched to LeaveBarsConfig (ModConfigSpec); otherwise unchanged.
package net.diaithir.hudoverlays.leavebars.mixin.client;

import net.diaithir.hudoverlays.leavebars.config.LeaveBarsConfig;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
abstract class GuiMixin {

    @ModifyVariable(method = "nextContextualInfoState", at = @At("STORE"), ordinal = 1)
    private boolean diaithir$leavebars$nextContextualInfoState(boolean pickJumpInfo) {
        if (LeaveBarsConfig.EXPERIENCE_BAR.get()) {
            return pickJumpInfo && this.willPrioritizeJumpInfo();
        } else {
            return pickJumpInfo;
        }
    }

    @Shadow
    private boolean willPrioritizeJumpInfo() {
        throw new RuntimeException();
    }
}
