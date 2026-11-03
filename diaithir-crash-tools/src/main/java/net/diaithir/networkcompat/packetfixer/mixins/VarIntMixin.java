// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Reads limits from the feature's Limits holder instead of the upstream Config class.
package net.diaithir.networkcompat.packetfixer.mixins;

import net.diaithir.networkcompat.packetfixer.Limits;
import net.minecraft.network.VarInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(VarInt.class)
public class VarIntMixin {
    @ModifyConstant(method = "getByteSize", constant = @Constant(intValue = 5))
    private static int packetfixer$getByteSize$newSize(int value) {
        return Limits.getVarIntSize();
    }

    @ModifyConstant(method = "read", constant = @Constant(intValue = 5))
    private static int packetfixer$read$newSize(int value) {
        return Limits.getVarIntSize();
    }

    @ModifyConstant(method = "read", constant = @Constant(stringValue = "VarInt too big"))
    private static String packetfixer$read$newMessage(String constant) {
        return "VarInt too big. Packet Fixer configured to " + Limits.getVarIntSize() + ". You can modify it in the Packet Fixer config.";
    }
}
