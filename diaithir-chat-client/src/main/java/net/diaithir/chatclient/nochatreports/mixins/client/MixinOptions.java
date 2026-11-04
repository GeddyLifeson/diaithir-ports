// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: none functional.
package net.diaithir.chatclient.nochatreports.mixins.client;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.diaithir.chatclient.nochatreports.client.NCRClient;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class MixinOptions {
    private static final Component SECURE_CHAT_TOOLTIP = Component.translatable("gui.nochatreports.secure_chat");
    private OptionInstance<Boolean> ncr$alternativeOption;

    @Inject(method = "onlyShowSecureChat", at = @At("RETURN"), cancellable = true)
    private void ncr$onlyShowSecureChat(CallbackInfoReturnable<OptionInstance<Boolean>> cir) {
        if (!NCRClient.enabled()) {
            return;
        }
        if (this.ncr$alternativeOption == null) {
            this.ncr$alternativeOption = new OptionInstance<>("options.onlyShowSecureChat",
                    OptionInstance.cachedConstantTooltip(SECURE_CHAT_TOOLTIP),
                    OptionInstance.BOOLEAN_TO_STRING,
                    new OptionInstance.Enum<>(ImmutableList.of(Boolean.FALSE), Codec.BOOL),
                    false, value -> {});
        }
        cir.setReturnValue(this.ncr$alternativeOption);
    }
}
