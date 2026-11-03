package net.diaithir.crashtools.crashassistant.client;

import net.diaithir.crashtools.crashassistant.client.event.KeybindHandler;
import net.diaithir.crashtools.crashassistant.client.event.TitleScreenHook;
import net.diaithir.crashtools.crashassistant.crash.UncaughtHandlerInstaller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientBootstrap {
    private ClientBootstrap() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(FMLClientSetupEvent.class, e -> e.enqueueWork(UncaughtHandlerInstaller::install));
        modBus.addListener(RegisterKeyMappingsEvent.class, KeybindHandler::register);
        NeoForge.EVENT_BUS.register(new TitleScreenHook());
        NeoForge.EVENT_BUS.register(new KeybindHandler());
    }
}
