package com.zurrtum.create;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Create.MOD_ID)
public class CreateNeoForge {
    public CreateNeoForge(IEventBus modBus, ModContainer container) {
        new Create().onInitialize();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(new NeoEvents());
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(FMLClientSetupEvent.class, event -> new com.zurrtum.create.client.Create().onInitializeClient());
        }
    }
}
