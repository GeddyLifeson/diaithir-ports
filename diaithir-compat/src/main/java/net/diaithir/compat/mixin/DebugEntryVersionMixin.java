package net.diaithir.compat.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** The F3 screen's first line names the pack. */
@Mixin(targets = "net.minecraft.client.gui.components.debug.DebugEntryVersion")
abstract class DebugEntryVersionMixin {
    @ModifyArg(method = "display", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenDisplayer;addPriorityLine(Ljava/lang/String;)V"))
    private String diaithir$line(String line) {
        return "Diaithir · Age I (" + SharedConstants.getCurrentVersion().name() + ")";
    }
}
