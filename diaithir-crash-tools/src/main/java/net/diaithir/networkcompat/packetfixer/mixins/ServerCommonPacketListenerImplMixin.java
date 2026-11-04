// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Reads limits from the feature's Limits holder instead of the upstream Config class.
package net.diaithir.networkcompat.packetfixer.mixins;

import net.diaithir.networkcompat.packetfixer.Limits;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin {
    @ModifyConstant(method = "keepConnectionAlive", constant = @Constant(longValue = 15000L))
    private long packetfixer$keepConnectionAlive(long constant) {
        return Limits.getKeepAliveTimeout() * 1000L;
    }

    @ModifyConstant(method = "checkIfClosed", constant = @Constant(longValue = 15000L))
    private long packetfixer$checkIfClosed(long constant) {
        return Limits.getKeepAliveTimeout() * 1000L;
    }
}
