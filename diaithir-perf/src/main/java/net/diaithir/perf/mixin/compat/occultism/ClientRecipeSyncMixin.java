package net.diaithir.perf.mixin.compat.occultism;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Occultism 1.256.1 subscribes JeiPlugin.ClientRecipeSync to RecipesReceivedEvent on the client without checking that JEI is
 * installed. Its handler touches a class that implements mezz.jei.api.IModPlugin, so with JEI gone every join throws
 * NoClassDefFoundError out of the neoforge:recipe_content payload, and the listeners after Occultism's (Spoiled's recipe list,
 * Reliquary, Powah and others) never run. Skip the handler when JEI is not loaded; its only job is to feed JEI.
 * Optional: if Occultism renames the handler this only turns off.
 */
@Pseudo
@Mixin(targets = "com.klikli_dev.occultism.integration.jei.impl.JeiPlugin$ClientRecipeSync", remap = false)
abstract class ClientRecipeSyncMixin {
    @Inject(method = "onRecipesReceived", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void diaithirperf$skipWithoutJei(RecipesReceivedEvent event, CallbackInfo ci) {
        if (!ModList.get().isLoaded("jei")) ci.cancel();
    }
}
