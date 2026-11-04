// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Reads limits from the feature's Limits holder instead of the upstream Config class.
package net.diaithir.networkcompat.packetfixer.mixins;

import net.diaithir.networkcompat.packetfixer.Limits;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin {
    @ModifyConstant(method = "lambda$static$0", constant = @Constant(intValue = 32767))
    private static int packetfixer$newSize(int value) {
        return Limits.getPacketSize();
    }

    @ModifyConstant(method = "lambda$static$2", constant = @Constant(intValue = 32767))
    private static int packetfixer$newSize$1(int value) {
        return Limits.getPacketSize();
    }
}
