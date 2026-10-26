package net.diaithir.branding.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Every screen title, vanilla or mod, is set in Uncial Antiqua (UI schema section 1); body text keeps the game font. */
@Mixin(Screen.class)
abstract class ScreenTitleMixin {
    private static final FontDescription UNCIAL = new FontDescription.Resource(Identifier.fromNamespaceAndPath("diaithirbranding", "uncial"));

    @ModifyVariable(method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"), argsOnly = true)
    private static Component diaithir$uncialTitle(Component title) {
        if (title == null || title.getString().isEmpty()) return title;
        return title.copy().withStyle(style -> style.getFont() == FontDescription.DEFAULT ? style.withFont(UNCIAL) : style);
    }
}
