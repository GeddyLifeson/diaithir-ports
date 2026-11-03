package io.github.tr100000.researcher.mixin.client;

import io.github.tr100000.researcher.ClientResearchTracker;
import io.github.tr100000.researcher.api.ClientResearchTrackerGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements ClientResearchTrackerGetter {
    @Unique
    private ClientResearchTracker researchTracker;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Minecraft minecraft, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
        researchTracker = new ClientResearchTracker();
    }

    @Override @Unique
    public ClientResearchTracker researcher$getClientTracker() {
        return researchTracker;
    }
}
