package net.diaithir.branding.mixin;

import net.diaithir.branding.TitleFont;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** The options header draws its own static title instead of the screen's, so it needs the same font. */
@Mixin(OptionsScreen.class)
abstract class OptionsScreenMixin {
    @ModifyArg(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/StringWidget;<init>(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/Font;)V"), index = 0)
    private Component diaithir$uncialHeader(Component title) {
        return TitleFont.apply(title);
    }
}
