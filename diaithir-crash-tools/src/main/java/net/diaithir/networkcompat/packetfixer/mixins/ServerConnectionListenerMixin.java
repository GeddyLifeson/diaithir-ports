// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Reads limits from the feature's Limits holder instead of the upstream Config class.
package net.diaithir.networkcompat.packetfixer.mixins;

import net.diaithir.networkcompat.packetfixer.Limits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.server.network.ServerConnectionListener$1")
public class ServerConnectionListenerMixin {
    @ModifyConstant(method = "initChannel", constant = @Constant(intValue = 30))
    private static int packetfixer$newTimeout(int value) {
        return Limits.getReadTimeout();
    }
}
