package net.diaithir.compat.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vanilla reads the loading screen logo from its own jar only; this reads Diaithir's wordmark from the mod. */
@Mixin(targets = "net.minecraft.client.gui.screens.LoadingOverlay$LogoTexture")
abstract class LogoTextureMixin {
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath("diaithircompat", "textures/gui/loading_logo.png");

    @Inject(method = "loadContents", at = @At("HEAD"), cancellable = true)
    private void diaithir$logo(ResourceManager resourceManager, CallbackInfoReturnable<TextureContents> cir) throws IOException {
        try (InputStream in = resourceManager.open(LOGO)) {
            cir.setReturnValue(new TextureContents(NativeImage.read(in), new TextureMetadataSection(true, true, MipmapStrategy.MEAN, 0.0F)));
        }
    }
}
