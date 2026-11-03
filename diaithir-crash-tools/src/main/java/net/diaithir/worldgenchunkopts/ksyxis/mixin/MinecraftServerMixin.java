// Ported from Ksyxis (MIT) https://github.com/VidTu/Ksyxis
// Modifications: only the informational head hook kept for 26.1.2 (spawn chunks no longer exist there); uses slf4j.
package net.diaithir.worldgenchunkopts.ksyxis.mixin;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Logs a notice when the server starts preparing levels. */
@Mixin(targets = "net.minecraft.server.MinecraftServer", remap = false)
public final class MinecraftServerMixin {
    @Unique
    private static final Logger KSYXIS_LOGGER = LogUtils.getLogger();

    private MinecraftServerMixin() {}

    @Inject(method = "prepareLevels", at = @At("HEAD"), remap = false, require = 0, expect = 0)
    private void ksyxis_prepareLevels_head(final CallbackInfo ci) {
        KSYXIS_LOGGER.info("Ksyxis: Speeding up the world loading... Delete the mod, if it got stuck after this message.");
    }
}
