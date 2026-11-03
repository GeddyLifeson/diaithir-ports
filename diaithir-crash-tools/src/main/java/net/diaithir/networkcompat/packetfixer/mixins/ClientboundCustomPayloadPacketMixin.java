// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Reads limits from the feature's Limits holder instead of the upstream Config class.
package net.diaithir.networkcompat.packetfixer.mixins;

import net.diaithir.networkcompat.packetfixer.Limits;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientboundCustomPayloadPacket.class)
public class ClientboundCustomPayloadPacketMixin {
    @ModifyConstant(method = "lambda$static$0", constant = @Constant(intValue = 1048576))
    private static int packetfixer$newSize(int value) {
        return Limits.getPacketSize();
    }

    @ModifyConstant(method = "lambda$static$2", constant = @Constant(intValue = 1048576))
    private static int packetfixer$newSize$2(int value) {
        return Limits.getPacketSize();
    }
}
