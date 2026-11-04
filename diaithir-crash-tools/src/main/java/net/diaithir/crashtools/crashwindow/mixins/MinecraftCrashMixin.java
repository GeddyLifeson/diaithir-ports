package net.diaithir.crashtools.crashwindow.mixins;

import java.io.File;
import net.diaithir.crashtools.crashwindow.CrashWindow;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftCrashMixin {
    @Inject(method = "saveReportAndShutdownSoundManager", at = @At("RETURN"), require = 0)
    private static void diaithir$crashWindow(@Nullable Minecraft minecraft, File gameDirectory, CrashReport crash, CallbackInfoReturnable<Integer> cir) {
        CrashWindow.show(crash);
    }
}
