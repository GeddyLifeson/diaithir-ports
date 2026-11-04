package net.diaithir.butchery.nutrition.events;

import net.diaithir.butchery.nutrition.Config;
import net.diaithir.butchery.nutrition.gui.NutrientButton;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class EventNutrientButton {

    @SubscribeEvent
    public void openGUI (ScreenEvent.Init.Post event)
    {
        if ((event.getScreen() instanceof InventoryScreen gui) && Config.NUTRIENT_BUTTON_ENABLED.get()) {
            event.addListener(new NutrientButton(gui, Component.nullToEmpty("N")));
        }
    }
}
