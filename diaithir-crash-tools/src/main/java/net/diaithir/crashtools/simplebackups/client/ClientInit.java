package net.diaithir.crashtools.simplebackups.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ClientInit {

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.register(new ClientEventHandler());
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
