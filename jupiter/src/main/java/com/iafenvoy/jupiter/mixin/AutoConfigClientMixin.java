package com.iafenvoy.jupiter.mixin;

import com.iafenvoy.jupiter.compat.clothconfig.ClothConfigLoader;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.internal.JupiterSettings;
import com.iafenvoy.jupiter.render.screen.ConfigSelectScreen;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.ConfigData;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Pseudo
@Mixin(AutoConfigClient.class)
public class AutoConfigClientMixin {
    @Inject(method = "getConfigScreen", at = @At("RETURN"), cancellable = true)
    private static <T extends ConfigData> void hookConfigScreen(Class<T> configClass, Screen parent, CallbackInfoReturnable<Supplier<Screen>> cir) {
        if (!JupiterSettings.INSTANCE.general.redirectAutoConfigScreen.getValue()) return;
        AbstractConfigContainer container = ClothConfigLoader.getByClass(configClass);
        if (container != null)
            cir.setReturnValue(ConfigSelectScreen.builder(container.getTitle(), parent).common(container)::build);
    }
}
