package net.diaithir.branding.mixin;

import net.diaithir.branding.TitleFont;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Every screen title, vanilla or mod, is set in Uncial Antiqua; body text keeps the game font. */
@Mixin(Screen.class)
abstract class ScreenTitleMixin {
    @ModifyVariable(method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"), argsOnly = true)
    private static Component diaithir$uncialTitle(Component title) {
        return TitleFont.apply(title);
    }
}
