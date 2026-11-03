// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: none functional; greys out the 'Only Show Secure Chat' option.
package net.diaithir.chatclient.nochatreports.mixins.client;

import java.util.Optional;
import net.diaithir.chatclient.nochatreports.client.NCRClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.ChatOptionsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatOptionsScreen.class)
public abstract class MixinChatOptionsScreen extends OptionsSubScreen {

    public MixinChatOptionsScreen() {
        super(null, null, null);
        throw new IllegalStateException("Can't touch this");
    }

    @Override
    protected void init() {
        super.init();
        if (NCRClient.enabled()) {
            Optional.ofNullable(this.list.findOption(Minecraft.getInstance().options.onlyShowSecureChat()))
                    .ifPresent(option -> option.active = false);
        }
    }
}
