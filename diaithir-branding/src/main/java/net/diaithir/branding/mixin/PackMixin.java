package net.diaithir.branding.mixin;

import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Packs whose names or descriptions are hardcoded (NeoForge's "Mod Resources", vanilla's extra looks) read as Diaithir. */
@Mixin(Pack.class)
abstract class PackMixin {
    private static final Set<String> RENAMED = Set.of("mod_resources", "mod_data");
    private static final Set<String> REDESCRIBED = Set.of("programmer_art", "high_contrast");

    @Shadow
    public abstract String getId();

    @Inject(method = "getTitle", at = @At("HEAD"), cancellable = true)
    private void diaithir$title(CallbackInfoReturnable<Component> cir) {
        if (RENAMED.contains(getId())) cir.setReturnValue(Component.translatable("diaithir.pack." + getId()));
    }

    @Inject(method = "getDescription", at = @At("HEAD"), cancellable = true)
    private void diaithir$description(CallbackInfoReturnable<Component> cir) {
        if (REDESCRIBED.contains(getId())) cir.setReturnValue(Component.translatable("diaithir.pack." + getId() + ".description"));
    }
}
