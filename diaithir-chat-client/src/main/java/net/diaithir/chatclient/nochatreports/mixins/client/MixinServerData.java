// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: persistence of the flag kept so SafetyState can read it on join; server-list icon rendering removed.
package net.diaithir.chatclient.nochatreports.mixins.client;

import net.diaithir.chatclient.nochatreports.core.ServerDataExtension;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerData.class)
public class MixinServerData implements ServerDataExtension {
    private boolean ncr$preventsChatReports;

    @Override
    public boolean preventsChatReports() {
        return this.ncr$preventsChatReports;
    }

    @Override
    public void setPreventsChatReports(boolean prevents) {
        this.ncr$preventsChatReports = prevents;
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void ncr$onWrite(CallbackInfoReturnable<CompoundTag> info) {
        info.getReturnValue().putBoolean("preventsChatReports", this.ncr$preventsChatReports);
    }

    @Inject(method = "read", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At("RETURN"))
    private static void ncr$onRead(CompoundTag tag, CallbackInfoReturnable<ServerData> info, ServerData data) {
        ((ServerDataExtension) data).setPreventsChatReports(tag.getBooleanOr("preventsChatReports", false));
    }
}
