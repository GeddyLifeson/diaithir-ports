// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: reads feature common config; logging via slf4j.
package net.diaithir.chatclient.nochatreports.mixins.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import net.diaithir.chatclient.nochatreports.config.NCRCommonConfig;
import net.diaithir.chatclient.nochatreports.core.ServerStatusCache;
import net.minecraft.network.Utf8String;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.network.codec.ByteBufCodecs$34")
public class MixinJsonByteBufCodec {
    @Shadow @Final
    private static Gson GSON;

    @Inject(method = "encode(Lio/netty/buffer/ByteBuf;Lcom/google/gson/JsonElement;)V", at = @At("HEAD"), cancellable = true)
    private void ncr$onEncode(ByteBuf buf, JsonElement element, CallbackInfo info) {
        if (!NCRCommonConfig.SPEC.isLoaded() || !NCRCommonConfig.ADD_QUERY_DATA.get()
                || !this.ncr$isServerStatusElement(element)) {
            return;
        }
        info.cancel();
        JsonObject object = element.getAsJsonObject();
        object.addProperty("preventsChatReports", true);
        Utf8String.write(buf, GSON.toJson(object), 32767);
    }

    @Inject(method = "decode(Lio/netty/buffer/ByteBuf;)Lcom/google/gson/JsonElement;", at = @At("RETURN"), cancellable = true)
    private void ncr$onDecode(ByteBuf buf, CallbackInfoReturnable<JsonElement> info) {
        JsonElement element = info.getReturnValue();
        if (!this.ncr$isServerStatusElement(element)) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        boolean prevents = object.has("preventsChatReports") && object.get("preventsChatReports").getAsBoolean();
        ServerStatusCache.setPreventsReports(prevents);
    }

    private boolean ncr$isServerStatusElement(JsonElement element) {
        if (!element.isJsonObject()) {
            return false;
        }
        JsonObject object = element.getAsJsonObject();
        if (!object.has("version") || !object.get("version").isJsonObject()) {
            return false;
        }
        JsonObject version = object.get("version").getAsJsonObject();
        return version.has("name") && version.has("protocol");
    }
}
