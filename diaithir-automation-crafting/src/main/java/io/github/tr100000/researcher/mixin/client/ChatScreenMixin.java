package io.github.tr100000.researcher.mixin.client;

import io.github.tr100000.researcher.ClientResearchTracker;
import io.github.tr100000.researcher.networking.StartResearchC2SPacket;
import io.github.tr100000.trutils.api.gui.GuiHelper;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        ClientResearchTracker researchTracker = minecraft.getConnection().researcher$getClientTracker();

        int y = 0;
        if (researchTracker.getCurrentResearching() != null) {
            unsetResearchIfTouching(y + 1, researchTracker.getCurrentResearchingId(), false, event);
            y += 27;
        }
        for (Identifier id : researchTracker.getPinnedResearches()) {
            unsetResearchIfTouching(y + 1, id, true, event);
            y += 27;
        }
    }

    @Unique
    private void unsetResearchIfTouching(int y, @Nullable Identifier id, boolean pin, MouseButtonEvent click) {
        if (id == null) return;

        if (GuiHelper.isMouseTouching(0, y, 150, 26, click.x(), click.y())) {
            if (pin) {
                ClientPacketDistributor.sendToServer(new StartResearchC2SPacket(StartResearchC2SPacket.Mode.UNPIN, Optional.of(id)));
            }
            else {
                ClientPacketDistributor.sendToServer(new StartResearchC2SPacket(StartResearchC2SPacket.Mode.SET_CURRENT, Optional.empty()));
            }
        }
    }
}
