package mcjty.lib.modules;

import net.minecraft.core.HolderLookup;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Modules {

    private final List<IModule> modules = new ArrayList<>();

    public void register(IModule module) {
        modules.add(module);
    }

    public void init(FMLCommonSetupEvent event) {
        modules.forEach(m -> m.init(event));
    }

    public void initClient(FMLClientSetupEvent event) {
        modules.forEach(m -> m.initClient(event));
    }

    public void initConfig(IEventBus bus) {
        modules.forEach(iModule -> iModule.initConfig(bus));
    }

}
