// Ported from Ksyxis (MIT) https://github.com/VidTu/Ksyxis
// Modifications: Mojang-mapped 26.1 targets only, static handlers, no logging/assert helpers.
package net.diaithir.worldgenchunkopts.ksyxis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Uses a zero-level ticket and stops waiting for nearby entities when the player is spawned. */
@Mixin(targets = "net.minecraft.server.network.config.PrepareSpawnTask$Ready", remap = false)
@Pseudo
public final class PrepareSpawnTaskReadyMixin {
    private PrepareSpawnTaskReadyMixin() {}

    @ModifyConstant(method = "keepAlive()V", constant = @Constant(intValue = 3), remap = false, require = 0, expect = 0)
    private static int ksyxis_keepAlive_addTicketAndLoadWithRadius(final int ticket) {
        return 0;
    }

    @ModifyConstant(method = "spawn(Lnet/minecraft/network/Connection;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/level/ServerPlayer;", constant = @Constant(intValue = 3), remap = false, require = 0, expect = 0)
    private static int ksyxis_spawn_waitForEntities(final int ticket) {
        return 0;
    }
}
