// Ported from Packet Fixer (MIT) https://github.com/TonimatasDEV/PacketFixer
// Modifications: Reads limits from the feature's Limits holder instead of the upstream Config class.
package net.diaithir.networkcompat.packetfixer.mixins;

import net.diaithir.networkcompat.packetfixer.Limits;
import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NbtAccounter.class)
public class NbtAccounterMixin {
    @Shadow @Final private long quota;

    @Redirect(method = "accountBytes(J)V", at = @At(value = "FIELD", target = "Lnet/minecraft/nbt/NbtAccounter;quota:J"))
    private long packetfixer$newSize(NbtAccounter instance) {
        return Limits.isForceUnlimitedNbtEnabled() ? Limits.getNbtMaxSize() : quota;
    }

    @ModifyConstant(method = "defaultQuota", constant = @Constant(longValue = 2097152L))
    private static long packetfixer$defaultQuota$newSize(long constant) {
        return Limits.getNbtMaxSize();
    }
}
