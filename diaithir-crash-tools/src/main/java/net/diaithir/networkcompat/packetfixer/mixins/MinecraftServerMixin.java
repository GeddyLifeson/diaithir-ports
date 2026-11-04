// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Reads limits from the feature's Limits holder instead of the upstream Config class.
package net.diaithir.networkcompat.packetfixer.mixins;

import net.diaithir.networkcompat.packetfixer.Limits;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow
    private int playerIdleTimeout;

    @Redirect(method = "playerIdleTimeout", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;playerIdleTimeout:I"))
    private int packetfixer$playerIdleTimeout(MinecraftServer server) {
        if (playerIdleTimeout <= 0) {
            return 0;
        } else {
            return Math.max(playerIdleTimeout, Limits.getPlayerIdleTimeout());
        }
    }
}
