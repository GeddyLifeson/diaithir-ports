package net.diaithir.butchery.nutrition.keybinding;

import net.diaithir.butchery.nutrition.network.GUITrigger;
import net.diaithir.butchery.nutrition.network.ModNetworkHandler;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ModInputHandler {
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (ModKeyBindings.keyBindings.get("nutrientgui").isDown())
        {
            ModNetworkHandler.sendToServer(new GUITrigger());
        }
    }
}
