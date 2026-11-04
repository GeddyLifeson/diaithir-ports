// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Reads limits from the feature's Limits holder instead of the upstream Config class.
package net.diaithir.networkcompat.packetfixer.mixins;

import net.diaithir.networkcompat.packetfixer.Limits;
import net.minecraft.network.VarLong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(VarLong.class)
public class VarLongMixin {
    @ModifyConstant(method = "getByteSize", constant = @Constant(intValue = 10))
    private static int packetfixer$getByteSize$newSize(int value) {
        return Limits.getVarLong();
    }

    @ModifyConstant(method = "read", constant = @Constant(intValue = 10))
    private static int packetfixer$read$newSize(int constant) {
        return Limits.getVarLong();
    }

    @ModifyConstant(method = "read", constant = @Constant(stringValue = "VarLong too big"))
    private static String packetfixer$read$newMessage(String constant) {
        return "VarLong too big. Packet Fixer configured to " + Limits.getVarLong() + ". You can modify it in the Packet Fixer config.";
    }
}
